package com.continuuity.internal.app.scheduler;

import com.continuuity.api.data.OperationException;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data.metadata.MetaDataStore;
import com.continuuity.data.operation.executor.OperationExecutor;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.data2.transaction.TransactionExecutorFactory;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.internal.app.runtime.schedule.DataSetBasedScheduleStore;
import com.continuuity.internal.io.UnsupportedTypeException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobStore;

import java.util.List;

/**
*
*/
public class SchedulerTest {

  private static OperationExecutor opex;
  private static MetaDataStore mds;
  private static Injector injector;
  private static Scheduler scheduler;
  private static TransactionExecutorFactory factory;
  private static DataSetAccessor accessor;

  @BeforeClass
  public static void setup() throws Exception {
    CConfiguration config = CConfiguration.create();
    injector = Guice.createInjector (new DataFabricModules().getInMemoryModules());
    injector.getInstance(InMemoryTransactionManager.class).init();
    accessor = injector.getInstance(DataSetAccessor.class);
    factory = injector.getInstance(TransactionExecutorFactory.class);
  }

  public static void schedulerSetup(boolean enablePersistence, String schedulerName)
    throws SchedulerException, UnsupportedTypeException, OperationException {
    JobStore js;
    if (enablePersistence) {
      js = new DataSetBasedScheduleStore(factory, accessor);
    } else {
      js = new RAMJobStore();
    }

    SimpleThreadPool threadPool = new SimpleThreadPool(10, Thread.NORM_PRIORITY);
    threadPool.initialize();
    DirectSchedulerFactory.getInstance().createScheduler(schedulerName, "1", threadPool, js);

    scheduler = DirectSchedulerFactory.getInstance().getScheduler(schedulerName);
    scheduler.start();
  }

  public static void schedulerTearDown() throws SchedulerException {
    scheduler.shutdown();
  }

  @Test
  public void testSchedulerWithoutPersistence() throws SchedulerException, UnsupportedTypeException,
                                                      OperationException {
    String schedulerName = "NonPersistentScheduler";
    //start scheduler without enabling persistence.
    schedulerSetup(false, schedulerName);
    JobDetail job = JobBuilder.newJob(LogPrintingJob.class)
                              .withIdentity("developer:application1:mapreduce1")
                              .build();

    Trigger trigger  = TriggerBuilder.newTrigger()
                                     .withIdentity("g1")
                                     .startNow()
                                     .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
                                     .build();

    JobKey key =  job.getKey();

    //Schedule job
    scheduler.scheduleJob(job, trigger);

    //Get the job stored.
    JobDetail jobStored = scheduler.getJobDetail(job.getKey());
    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(job.getKey());

    Assert.assertEquals(jobStored.getKey().getName(), key.getName());
    Assert.assertEquals(1, triggers.size());

    //Shutdown scheduler.
    schedulerTearDown();

    //restart scheduler.
    schedulerSetup(false, schedulerName);

   //read the job
    jobStored = scheduler.getJobDetail(job.getKey());
    // The job with old job key should not exist since it is not persisted.
    Assert.assertNull(jobStored);
    schedulerTearDown();
  }

  @Test
  public void testSchedulerWithPersistence() throws SchedulerException,
                                                    UnsupportedTypeException, OperationException {
    String schedulerName = "persistentScheduler";
    //start scheduler enabling persistence.
    schedulerSetup(true, schedulerName);
    JobDetail job = JobBuilder.newJob(LogPrintingJob.class)
      .withIdentity("developer:application1:mapreduce2")
      .build();

    Trigger trigger  = TriggerBuilder.newTrigger()
      .withIdentity("p1")
      .startNow()
      .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
      .build();

    JobKey key =  job.getKey();

    //Schedule job
    scheduler.scheduleJob(job, trigger);

    //Get the job stored.
    JobDetail jobStored = scheduler.getJobDetail(job.getKey());
    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(job.getKey());

    Assert.assertEquals(jobStored.getKey().getName(), key.getName());
    Assert.assertEquals(1, triggers.size());

    //Shutdown scheduler.
    schedulerTearDown();

    //restart scheduler.
    schedulerSetup(true, schedulerName);

    //read the job
    jobStored = scheduler.getJobDetail(job.getKey());
    // The job with old job key should exist since it is persisted in Dataset
    Assert.assertNotNull(jobStored);
    Assert.assertEquals(jobStored.getKey().getName(), key.getName());

    triggers = scheduler.getTriggersOfJob(job.getKey());
    Assert.assertEquals(1, triggers.size());

    schedulerTearDown();
  }

  @AfterClass
  public static void cleanup() throws SchedulerException, InterruptedException {
    schedulerTearDown();
    Thread.sleep(10000);
  }
}
