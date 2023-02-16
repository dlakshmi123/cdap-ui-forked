/*
 * Copyright 2014 Cask, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.explore.executor;

import co.cask.cdap.api.data.batch.RecordScannable;
import co.cask.cdap.api.dataset.Dataset;
import co.cask.cdap.api.dataset.DatasetDefinition;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.explore.client.DatasetExploreFacade;
import co.cask.cdap.explore.service.ExploreService;
import co.cask.cdap.hive.objectinspector.ObjectInspectorFactory;
import co.cask.cdap.hive.objectinspector.ReflectionStructObjectInspector;
import co.cask.cdap.internal.io.UnsupportedTypeException;
import co.cask.cdap.proto.QueryHandle;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Handler that implements internal explore APIs.
 */
@Path(Constants.Gateway.GATEWAY_VERSION)
public class ExploreExecutorHttpHandler extends AbstractHttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(QueryExecutorHttpHandler.class);

  private final ExploreService exploreService;
  private final DatasetFramework datasetFramework;

  @Inject
  public ExploreExecutorHttpHandler(ExploreService exploreService, DatasetFramework datasetFramework) {
    this.exploreService = exploreService;
    this.datasetFramework = datasetFramework;
  }

  /**
   * Enable ad-hoc exploration of a dataset instance.
   */
  @POST
  @Path("data/explore/datasets/{dataset}/enable")
  public void enableExplore(@SuppressWarnings("UnusedParameters") HttpRequest request, HttpResponder responder,
                            @PathParam("dataset") final String datasetName) {
    try {
      Dataset dataset;
      try {
        dataset = datasetFramework.getDataset(datasetName, DatasetDefinition.NO_ARGUMENTS, null);
      } catch (Exception e) {
        String className = isClassNotFoundException(e);
        if (className == null) {
          throw e;
        }
        LOG.info("Cannot load dataset {} because class {} cannot be found. This is probably because class {} is a " +
                   "type parameter of dataset {} that is not present in the dataset's jar file. See the developer " +
                   "guide for more information.", datasetName, className, className, datasetName);
        JsonObject json = new JsonObject();
        json.addProperty("handle", QueryHandle.NO_OP.getHandle());
        responder.sendJson(HttpResponseStatus.OK, json);
        return;
      }
      if (dataset == null) {
        responder.sendError(HttpResponseStatus.NOT_FOUND, "Cannot load dataset " + datasetName);
        return;
      }

      if (!(dataset instanceof RecordScannable)) {
        // It is not an error to get non-RecordScannable datasets, since the type of dataset may not be known where this
        // call originates from.
        LOG.debug("Dataset {} does not implement {}", datasetName, RecordScannable.class.getName());
        JsonObject json = new JsonObject();
        json.addProperty("handle", QueryHandle.NO_OP.getHandle());
        responder.sendJson(HttpResponseStatus.OK, json);
        return;
      }

      LOG.debug("Enabling explore for dataset instance {}", datasetName);
      RecordScannable<?> scannable = (RecordScannable) dataset;
      String createStatement;
      try {
        createStatement = DatasetExploreFacade.generateCreateStatement(datasetName, scannable);
      } catch (UnsupportedTypeException e) {
        LOG.error("Exception while generating create statement for dataset {}", datasetName, e);
        responder.sendError(HttpResponseStatus.BAD_REQUEST, e.getMessage());
        return;
      }

      LOG.debug("Running create statement for dataset {} with row scannable {} - {}",
                datasetName,
                dataset.getClass().getName(),
                createStatement);

      QueryHandle handle = exploreService.execute(createStatement);
      JsonObject json = new JsonObject();
      json.addProperty("handle", handle.getHandle());
      responder.sendJson(HttpResponseStatus.OK, json);
    } catch (Throwable e) {
      LOG.error("Got exception:", e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private String isClassNotFoundException(Throwable e) {
    if (e instanceof ClassNotFoundException) {
      return e.getMessage();
    }
    if (e.getCause() != null) {
      return isClassNotFoundException(e.getCause());
    }
    return null;
  }

  /**
   * Disable ad-hoc exploration of a dataset instance.
   */
  @POST
  @Path("data/explore/datasets/{dataset}/disable")
  public void disableExplore(@SuppressWarnings("UnusedParameters") HttpRequest request, HttpResponder responder,
                             @PathParam("dataset") final String datasetName) {
    try {
      LOG.debug("Disabling explore for dataset instance {}", datasetName);

      Dataset dataset = datasetFramework.getDataset(datasetName, DatasetDefinition.NO_ARGUMENTS, null);
      if (dataset == null) {
        responder.sendError(HttpResponseStatus.NOT_FOUND, "Cannot load dataset " + datasetName);
        return;
      }

      if (!(dataset instanceof RecordScannable)) {
        // It is not an error to get non-RecordScannable datasets, since the type of dataset may not be known where this
        // call originates from.
        LOG.debug("Dataset {} does not implement {}", datasetName, RecordScannable.class.getName());
        JsonObject json = new JsonObject();
        json.addProperty("handle", QueryHandle.NO_OP.getHandle());
        responder.sendJson(HttpResponseStatus.OK, json);
        return;
      }

      String deleteStatement = DatasetExploreFacade.generateDeleteStatement(datasetName);
      LOG.debug("Running delete statement for dataset {} - {}", datasetName, deleteStatement);

      QueryHandle handle = exploreService.execute(deleteStatement);
      JsonObject json = new JsonObject();
      json.addProperty("handle", handle.getHandle());
      responder.sendJson(HttpResponseStatus.OK, json);
    } catch (Throwable e) {
      LOG.error("Got exception:", e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
    * Get the schema of a dataset.
    */
  @GET
  @Path("/data/explore/datasets/{dataset}/schema")
  public void getDatasetSchema(@SuppressWarnings("UnusedParameters") HttpRequest request, HttpResponder responder,
                               @PathParam("dataset") final String datasetName) {
    try {
      LOG.trace("Retrieving Explore schema for dataset {}", datasetName);

      Dataset dataset = datasetFramework.getDataset(datasetName, DatasetDefinition.NO_ARGUMENTS, null);
      if (dataset == null) {
        responder.sendError(HttpResponseStatus.NOT_FOUND, "Cannot find dataset " + datasetName);
        return;
      }

      if (!(dataset instanceof RecordScannable)) {
        LOG.debug("Dataset {} does not implement {}", datasetName, RecordScannable.class.getName());
        responder.sendError(HttpResponseStatus.BAD_REQUEST,
                            String.format("Dataset %s does not implement %s",
                                          datasetName, RecordScannable.class.getName()));
        return;
      }

      RecordScannable recordScannable = (RecordScannable) dataset;

      ObjectInspector oi = ObjectInspectorFactory.getReflectionObjectInspector(recordScannable.getRecordType());
      if (!(oi instanceof ReflectionStructObjectInspector)) {
        LOG.debug("Record type {} for dataset {} is not a RECORD.",
                  recordScannable.getRecordType().getClass().getName(), datasetName);
        responder.sendError(HttpResponseStatus.BAD_REQUEST,
                            String.format("Record type %s for dataset %s is not a RECORD.",
                                          recordScannable.getRecordType().getClass().getName(), datasetName));
        return;
      }

      ImmutableMap.Builder builder = ImmutableMap.builder();
      for (StructField structField : ((ReflectionStructObjectInspector) oi).getAllStructFieldRefs()) {
        ObjectInspector tmp = structField.getFieldObjectInspector();
        builder.put(structField.getFieldName(), tmp.getTypeName());
      }
      responder.sendJson(HttpResponseStatus.OK, builder.build());
    } catch (Throwable e) {
      LOG.error("Got exception:", e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
