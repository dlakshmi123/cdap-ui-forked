package com.continuuity.internal.app.runtime.procedure;

import com.continuuity.api.data.DataSet;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.api.procedure.ProcedureContext;
import com.continuuity.api.procedure.ProcedureSpecification;
import com.continuuity.app.program.Program;
import com.continuuity.app.runtime.Arguments;
import com.continuuity.data.dataset.DataSetContext;
import com.continuuity.data.dataset.DataSetInstantiationBase;
import com.continuuity.internal.app.runtime.DataFabricFacade;
import com.continuuity.internal.app.runtime.DataSets;
import com.continuuity.weave.api.RunId;

import java.util.Map;

/**
 * Private interface to help creating {@link ProcedureContext}.
 */
final class BasicProcedureContextFactory {

  private final Program program;
  private final RunId runId;
  private final int instanceId;
  private final Arguments userArguments;
  private final ProcedureSpecification procedureSpec;
  private final MetricsCollectionService collectionService;

  BasicProcedureContextFactory(Program program, RunId runId, int instanceId,
                               Arguments userArguments, ProcedureSpecification procedureSpec,
                               MetricsCollectionService collectionService) {
    this.program = program;
    this.runId = runId;
    this.instanceId = instanceId;
    this.userArguments = userArguments;
    this.procedureSpec = procedureSpec;
    this.collectionService = collectionService;
  }

  BasicProcedureContext create(DataFabricFacade dataFabricFacade) {
    DataSetContext dataSetContext = dataFabricFacade.getDataSetContext();
    Map<String, DataSet> dataSets = DataSets.createDataSets(dataSetContext,
                                                            procedureSpec.getDataSets());
    BasicProcedureContext context = new BasicProcedureContext(program, runId, instanceId,
                                                                            dataSets,
                                                                            userArguments, procedureSpec,
                                                                            collectionService);

    // hack for propagating metrics collector to datasets
    if (dataSetContext instanceof DataSetInstantiationBase) {
      ((DataSetInstantiationBase) dataSetContext).setMetricsCollector(collectionService,
                                                                      context.getSystemMetrics());
    }
    return context;
  }
}
