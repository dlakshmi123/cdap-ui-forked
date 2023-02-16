/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.metadata;

import co.cask.cdap.common.BadRequestException;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.utils.TimeMathParser;
import co.cask.cdap.data2.metadata.lineage.Lineage;
import co.cask.cdap.data2.metadata.lineage.LineageSerializer;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.codec.NamespacedIdCodec;
import co.cask.cdap.proto.metadata.MetadataRecord;
import co.cask.cdap.proto.metadata.lineage.LineageRecord;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * HttpHandler for lineage.
 */
@Path(Constants.Gateway.API_VERSION_3)
public class LineageHandler extends AbstractHttpHandler {
  private static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(Id.NamespacedId.class, new NamespacedIdCodec())
    .create();
  private static final Type SET_METADATA_RECORD_TYPE = new TypeToken<Set<MetadataRecord>>() { }.getType();

  private final LineageAdmin lineageAdmin;

  @Inject
  LineageHandler(LineageAdmin lineageAdmin) {
    this.lineageAdmin = lineageAdmin;
  }

  @GET
  @Path("/namespaces/{namespace-id}/datasets/{dataset-id}/lineage")
  public void datasetLineage(HttpRequest request, HttpResponder responder,
                             @PathParam("namespace-id") String namespaceId,
                             @PathParam("dataset-id") String datasetId,
                             @QueryParam("start") String startStr,
                             @QueryParam("end") String endStr,
                             @QueryParam("levels") @DefaultValue("10") int levels) throws Exception {

    checkLevels(levels);
    TimeRange range = parseRange(startStr, endStr);

    Id.DatasetInstance datasetInstance = Id.DatasetInstance.from(namespaceId, datasetId);
    Lineage lineage = lineageAdmin.computeLineage(datasetInstance, range.getStart(), range.getEnd(), levels);
    responder.sendJson(HttpResponseStatus.OK,
                       LineageSerializer.toLineageRecord(TimeUnit.MILLISECONDS.toSeconds(range.getStart()),
                                                         TimeUnit.MILLISECONDS.toSeconds(range.getEnd()),
                                                         lineage),
                       LineageRecord.class, GSON);
  }

  @GET
  @Path("/namespaces/{namespace-id}/streams/{stream-id}/lineage")
  public void streamLineage(HttpRequest request, HttpResponder responder,
                            @PathParam("namespace-id") String namespaceId,
                            @PathParam("stream-id") String stream,
                            @QueryParam("start") String startStr,
                            @QueryParam("end") String endStr,
                            @QueryParam("levels") @DefaultValue("10") int levels) throws Exception {

    checkLevels(levels);
    TimeRange range = parseRange(startStr, endStr);

    Id.Stream streamId = Id.Stream.from(namespaceId, stream);
    Lineage lineage = lineageAdmin.computeLineage(streamId, range.getStart(), range.getEnd(), levels);
    responder.sendJson(HttpResponseStatus.OK,
                       LineageSerializer.toLineageRecord(TimeUnit.MILLISECONDS.toSeconds(range.getStart()),
                                                         TimeUnit.MILLISECONDS.toSeconds(range.getEnd()),
                                                         lineage),
                       LineageRecord.class, GSON);
  }

  @GET
  @Path("/namespaces/{namespace-id}/apps/{app-id}/{program-type}/{program-id}/runs/{run-id}/metadata")
  public void getAccessesForRun(HttpRequest request, HttpResponder responder,
                                @PathParam("namespace-id") String namespaceId,
                                @PathParam("app-id") String appId,
                                @PathParam("program-type") String programType,
                                @PathParam("program-id") String programId,
                                @PathParam("run-id") String runId) throws Exception {
    Id.Run run = new Id.Run(
      Id.Program.from(namespaceId, appId, ProgramType.valueOfCategoryName(programType), programId),
      runId);
    responder.sendJson(HttpResponseStatus.OK, lineageAdmin.getMetadataForRun(run), SET_METADATA_RECORD_TYPE, GSON);
  }

  private void checkLevels(int levels) throws BadRequestException {
    if (levels < 1) {
      throw new BadRequestException(String.format("Invalid levels (%d), should be greater than 0.", levels));
    }
  }

  // TODO: CDAP-3715 This is a fairly common operation useful in various handlers
  private TimeRange parseRange(String startStr, String endStr) throws BadRequestException {
    if (startStr == null) {
      throw new BadRequestException("Start time is required.");
    }
    if (endStr == null) {
      throw new BadRequestException("End time is required.");
    }

    long now = TimeMathParser.nowInSeconds();
    long start;
    long end;
    try {
      // start and end are specified in seconds from HTTP request,
      // but specified in milliseconds to the LineageGenerator
      start = TimeUnit.SECONDS.toMillis(TimeMathParser.parseTimeInSeconds(now, startStr));
      end = TimeUnit.SECONDS.toMillis(TimeMathParser.parseTimeInSeconds(now, endStr));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(e);
    }

    if (start < 0) {
      throw new BadRequestException(String.format("Invalid start time (%s -> %d), should be >= 0.", startStr, start));
    }
    if (end < 0) {
      throw new BadRequestException(String.format("Invalid end time (%s -> %d), should be >= 0.", endStr, end));
    }
    if (start > end) {
      throw new BadRequestException(String.format("Start time (%s -> %d) should be lesser than end time (%s -> %d).",
                                                  startStr, start, endStr, end));
    }

    return new TimeRange(start, end);
  }

  private static class TimeRange {
    private final long start;
    private final long end;

    private TimeRange(long start, long end) {
      this.start = start;
      this.end = end;
    }

    public long getStart() {
      return start;
    }

    public long getEnd() {
      return end;
    }
  }
}
