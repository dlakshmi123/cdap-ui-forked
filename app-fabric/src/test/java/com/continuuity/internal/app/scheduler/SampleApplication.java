package com.continuuity.internal.app.scheduler;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.data.dataset.ObjectStore;
import com.continuuity.api.schedule.Schedule;
import com.continuuity.api.workflow.AbstractWorkflowAction;
import com.continuuity.api.workflow.Workflow;
import com.continuuity.api.workflow.WorkflowSpecification;
import com.continuuity.internal.io.UnsupportedTypeException;
import com.continuuity.internal.schedule.DefaultSchedule;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Sample application to test if the scheduler has run an action.
 */
public class SampleApplication implements Application {

  @Override
  public ApplicationSpecification configure() {
    try {
      return ApplicationSpecification.Builder.with()
        .setName("SampleApp")
        .setDescription("Sample application")
        .noStream()
        .withDataSets()
          .add(new ObjectStore<String>("input", String.class))
          .add(new ObjectStore<String>("output", String.class))
        .noFlow()
        .noProcedure()
        .noBatch()
        .withWorkflow()
        .add(new SampleWorkflow())
        .build();
    } catch (UnsupportedTypeException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Sample workflow. Schedules a dummy MR job.
   */
  public static class SampleWorkflow implements Workflow {

    @Override
    public WorkflowSpecification configure() {
      return WorkflowSpecification.Builder.with()
        .setName("SampleWorkflow")
        .setDescription("SampleWorkflow description")
        .startWith(new DummyAction())
        .last(new DummyAction())
        .addSchedule(new DefaultSchedule("Schedule", "Run every 2 seconds", "0/2 * * * * ?",
                                         Schedule.Action.START))
        .build();
    }
  }

  /**
   * DummyAction
   */
  public static class DummyAction extends AbstractWorkflowAction {
    private static Logger LOG = LoggerFactory.getLogger(DummyAction.class);
    @Override
    public void run() {
      LOG.info("Ran dummy action");
    }
  }
}
