package com.continuuity.watchdog.election;

import com.continuuity.common.zookeeper.election.ElectionHandler;
import com.continuuity.common.zookeeper.election.LeaderElection;
import com.continuuity.weave.common.Cancellable;
import com.continuuity.weave.common.Threads;
import com.continuuity.weave.zookeeper.ZKClient;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Handles leader election for multiple partitions.
 */
public class MultiLeaderElection extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(MultiLeaderElection.class);
  private static final Random RANDOM = new Random(System.nanoTime());

  private final ZKClient zkClient;
  private final String name;
  private final int partitionSize;
  private final ExecutorService executor;
  private final PartitionChangeHandler handler;
  private final Set<Integer> leaderPartitions;
  private final List<Cancellable> electionCancels;

  private Future<?> handlerFuture;

  private Set<Integer> prevLeaderPartitions;
  private int leaderElectionSleepMs = 8 * 1000;

  public MultiLeaderElection(ZKClient zkClient, String name, int partitionSize, PartitionChangeHandler handler) {
    this.zkClient = zkClient;
    this.name = name;
    this.partitionSize = partitionSize;
    this.handler = handler;

    this.executor = Executors.newSingleThreadExecutor(Threads.createDaemonThreadFactory("multi-leader-election"));
    this.leaderPartitions = Sets.newCopyOnWriteArraySet();
    this.electionCancels = Lists.newArrayList();
    this.prevLeaderPartitions = ImmutableSet.of();
  }

  @Override
  protected void startUp() throws Exception {
    LOG.info("Starting leader election...");

    // Divide the set of partitions into 2 and run leader election on them separately.
    Set<Integer> partitions1 = Sets.newHashSet();
    Set<Integer> partitions2 = Sets.newHashSet();
    for (int i = 0; i < partitionSize; ++i) {
      if (RANDOM.nextBoolean()) {
        partitions1.add(i);
      } else {
        partitions2.add(i);
      }
    }

    // Wait for a random time to get even distribution of leader partitions.
    int ms = RANDOM.nextInt(leaderElectionSleepMs) + 1;
    LOG.debug("Sleeping for {} ms for partition {} before leader election", ms, partitions1);
    TimeUnit.MILLISECONDS.sleep(ms);
    runElection(partitions1);

    ms = RANDOM.nextInt(leaderElectionSleepMs) + 1;
    LOG.debug("Sleeping for {} ms for partition {} before leader election", ms, partitions2);
    TimeUnit.MILLISECONDS.sleep(ms);
    runElection(partitions2);

    LOG.info("Leader election started.");
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.info("Stopping leader election.");

    for (Cancellable cancel : electionCancels) {
      cancel.cancel();
    }

    executor.shutdown();

    LOG.info("Leader election stopped.");
  }

  public void setLeaderElectionSleepMs(int leaderElectionSleepMs) {
    this.leaderElectionSleepMs = leaderElectionSleepMs;
  }

  private void runElection(Set<Integer> partitions) throws Exception {
    for (final int partition : partitions) {
      // Start leader election.
      LeaderElection election =
        new LeaderElection(zkClient, String.format("/election/%s/part-%d", name, partition), new ElectionHandler() {
          @Override
          public void leader() {
            leaderPartitions.add(partition);
            runHandler();
          }

          @Override
          public void follower() {
            leaderPartitions.remove(partition);
            runHandler();
          }
        });
      electionCancels.add(election);
    }
  }

  private void runHandler() {
    // Cancel any previous runHandler running.
    if (handlerFuture != null) {
      handlerFuture.cancel(true);
    }

    handlerFuture = executor.submit(runHandler);
  }

  private final Runnable runHandler = new Runnable() {
    @Override
    public void run() {
      while (true) {
        try {
          Set<Integer> newLeaders = ImmutableSet.copyOf(leaderPartitions);
          if (!newLeaders.equals(prevLeaderPartitions)) {
            LOG.info("Leader partitions changed - {}", newLeaders);
            prevLeaderPartitions = newLeaders;
            handler.partitionsChanged(prevLeaderPartitions);
          }
          return;

        } catch (Throwable t) {
          LOG.error("Got exception while running leader change handler.. will retry.", t);
          try {
            TimeUnit.MILLISECONDS.sleep(200);
          } catch (InterruptedException e) {
            // okay to ignore since this is not the main thread.
            LOG.warn("Got interrupted exception: ", e);
          }
        }
      }
    }
  };
}
