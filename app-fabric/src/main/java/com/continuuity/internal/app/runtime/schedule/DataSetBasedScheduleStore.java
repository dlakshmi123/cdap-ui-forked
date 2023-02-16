package com.continuuity.internal.app.runtime.schedule;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.OperationResult;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data2.dataset.api.DataSetManager;
import com.continuuity.data2.dataset.lib.table.OrderedColumnarTable;
import com.continuuity.data2.transaction.TransactionAware;
import com.continuuity.data2.transaction.TransactionExecutor;
import com.continuuity.data2.transaction.TransactionExecutorFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.lang.SerializationUtils;
import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ScheduleStore extends from RAMJobStore and persists the trigger and schedule information into datasets.
 */
public class DataSetBasedScheduleStore extends RAMJobStore {

  private static final Logger LOG = LoggerFactory.getLogger(DataSetBasedScheduleStore.class);
  private static final String SCHEDULE_STORE_DATASET_NAME = "schedulestore";
  private static final byte[] JOB_KEY = Bytes.toBytes("jobs");
  private static final byte[] TRIGGER_KEY = Bytes.toBytes("trigger");

  private final DataSetAccessor dataSetAccessor;

  private final TransactionExecutorFactory factory;
  private OrderedColumnarTable table;

  @Inject
  public DataSetBasedScheduleStore(TransactionExecutorFactory factory, DataSetAccessor dataSetAccessor) {
    this.dataSetAccessor = dataSetAccessor;
    this.factory = factory;
  }

  @Override
  public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler schedSignaler) {
    super.initialize(loadHelper, schedSignaler);
    try {
      createDatasetIfNotExists();
      table = dataSetAccessor.getDataSetClient(SCHEDULE_STORE_DATASET_NAME,
                                               OrderedColumnarTable.class,
                                               DataSetAccessor.Namespace.SYSTEM);
      Preconditions.checkNotNull(table, "Could not get dataset client for data set: %s", SCHEDULE_STORE_DATASET_NAME);
      readSchedulesFromPersistentStore();
    } catch (Throwable th) {
      throw Throwables.propagate(th);
    }
  }

  private void createDatasetIfNotExists() throws Exception {
    DataSetManager dataSetManager = dataSetAccessor.getDataSetManager(OrderedColumnarTable.class,
                                                                      DataSetAccessor.Namespace.SYSTEM);
    if (!dataSetManager.exists(SCHEDULE_STORE_DATASET_NAME)) {
      dataSetManager.create(SCHEDULE_STORE_DATASET_NAME);
    }
  }

  @Override
  public void storeJob(JobDetail newJob, boolean replaceExisting) throws ObjectAlreadyExistsException {
    super.storeJob(newJob, replaceExisting);
    executePersist(newJob, null);
  }

  @Override
  public void storeTrigger(OperableTrigger newTrigger, boolean replaceExisting) throws JobPersistenceException {
    super.storeTrigger(newTrigger, replaceExisting);
    executePersist(null, newTrigger);
  }

  @Override
  public void storeJobsAndTriggers(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs,
                                   boolean replace) throws JobPersistenceException {
    super.storeJobsAndTriggers(triggersAndJobs, replace);
    // TODO (sree): Add persisting job and triggers
  }

  @Override
  public void storeJobAndTrigger(JobDetail newJob, OperableTrigger newTrigger)
                                 throws JobPersistenceException {
    super.storeJob(newJob, true);
    super.storeTrigger(newTrigger, true);

    executePersist(newJob, newTrigger);
  }

  @Override
  public void pauseTrigger(TriggerKey triggerKey) {
    super.pauseTrigger(triggerKey);
    executePersist(triggerKey, Trigger.TriggerState.PAUSED);
  }

  @Override
  public void resumeTrigger(TriggerKey triggerKey) {
    super.resumeTrigger(triggerKey);
    executePersist(triggerKey, Trigger.TriggerState.NORMAL);
  }


  private void executePersist(final TriggerKey triggerKey, final Trigger.TriggerState state) {
    try {
      factory.createExecutor(ImmutableList.of((TransactionAware) table))
                            .execute(new TransactionExecutor.Subroutine() {
                              @Override
                              public void apply() throws Exception {
                                if (triggerKey != null) {
                                  TriggerStatus trigger = readTrigger(triggerKey);
                                  if (trigger != null) {
                                    persistTrigger(table, trigger.trigger, state);
                                  }
                                }
                              }
                            });
    } catch (Throwable th) {
      throw Throwables.propagate(th);
    }
  }

  private void executePersist(final JobDetail newJob, final OperableTrigger newTrigger) {
    try {
      factory.createExecutor(ImmutableList.of((TransactionAware) table))
        .execute(new TransactionExecutor.Subroutine() {
          @Override
          public void apply() throws Exception {
            if (newJob != null) {
              persistJob(table, newJob);
              LOG.debug("Schedule: stored job with key {}", newJob.getKey());
            }
            if (newTrigger != null) {
              persistTrigger(table, newTrigger, Trigger.TriggerState.NORMAL);
              LOG.debug("Schedule: stored trigger with key {}", newTrigger.getKey());
            }
          }
        });

    } catch (Throwable th) {
      throw Throwables.propagate(th);
    }
  }

  // Persist the job information to dataset
  private void persistJob(OrderedColumnarTable table, JobDetail job) throws Exception {
    byte[][] cols = new byte[1][];
    byte[][] values = new byte[1][];

    cols[0] = Bytes.toBytes(job.getKey().toString());
    values[0] = SerializationUtils.serialize(job);
    table.put(JOB_KEY, cols, values);
  }

  private TriggerStatus readTrigger(TriggerKey key) throws Exception {
    byte[][] col = new byte[1][];
    col[0] = Bytes.toBytes(key.getName().toString());
    OperationResult<Map<byte[], byte[]>> result = table.get(TRIGGER_KEY, col);
    byte[] bytes = null;
    if (!result.isEmpty()){
      bytes = result.getValue().get(col[0]);
    }
    if (bytes != null){
      return (TriggerStatus) SerializationUtils.deserialize(bytes);
    } else {
      return null;
    }
  }

  // Persist the trigger information to dataset
  private void persistTrigger(OrderedColumnarTable table, OperableTrigger trigger,
                              Trigger.TriggerState state) throws Exception {

    byte[][] cols = new byte[1][];
    byte[][] values = new byte[1][];

    cols[0] = Bytes.toBytes(trigger.getKey().getName().toString());
    values[0] = SerializationUtils.serialize(new TriggerStatus(trigger, state));
    table.put(TRIGGER_KEY, cols, values);
  }

  // Get schedule information from persistent store
  private void readSchedulesFromPersistentStore() throws Exception {

    final List<JobDetail> jobs = Lists.newArrayList();
    final List<OperableTrigger> triggers = Lists.newArrayList();

    factory.createExecutor(ImmutableList.of((TransactionAware) table))
      .execute(new TransactionExecutor.Subroutine() {
        @Override
        public void apply() throws Exception {
          OperationResult<Map<byte[], byte[]>> result = table.get(JOB_KEY, null, null, -1);
          if (!result.isEmpty()) {
            for (byte[] bytes : result.getValue().values()){
              JobDetail jobDetail = (JobDetail) SerializationUtils.deserialize(bytes);
              LOG.debug("Schedule: Job with key {} found", jobDetail.getKey());
              jobs.add(jobDetail);
            }
          } else {
            LOG.debug("Schedule: No Jobs found in Job store");
          }

          result = table.get(TRIGGER_KEY, null, null, -1);
          if (!result.isEmpty()) {
            for (byte[] bytes : result.getValue().values()){
              TriggerStatus trigger = (TriggerStatus) SerializationUtils.deserialize(bytes);
              if (trigger.state.equals(Trigger.TriggerState.NORMAL)) {
                triggers.add(trigger.trigger);
                LOG.debug("Schedule: trigger with key {} added", trigger.trigger.getKey());
              } else {
                LOG.debug("Schedule: trigger with key {} and state {} skipped", trigger.trigger.getKey(),
                                                                                trigger.state.toString());
              }
            }
          } else {
            LOG.debug("Schedule: No triggers found in job store");
          }
      }
    });

    for (JobDetail job : jobs) {
      super.storeJob(job, true);
    }

    for (OperableTrigger trigger : triggers){
      super.storeTrigger(trigger, true);
    }
  }

  /**
   * Trigger and state.
   */
  private static class TriggerStatus implements Serializable {
    private OperableTrigger trigger;
    private Trigger.TriggerState state;

    private TriggerStatus(OperableTrigger trigger, Trigger.TriggerState state) {
      this.trigger = trigger;
      this.state = state;
    }
  }
}
