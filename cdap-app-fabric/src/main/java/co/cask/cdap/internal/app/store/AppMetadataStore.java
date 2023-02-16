/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

package co.cask.cdap.internal.app.store;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.data.stream.StreamSpecification;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.app.ApplicationSpecification;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.data2.dataset2.lib.table.MDSKey;
import co.cask.cdap.data2.dataset2.lib.table.MetadataStoreDataset;
import co.cask.cdap.internal.app.ApplicationSpecificationAdapter;
import co.cask.cdap.internal.app.DefaultApplicationSpecification;
import co.cask.cdap.internal.app.runtime.adapter.AdapterStatus;
import co.cask.cdap.proto.AdapterSpecification;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.NamespaceMeta;
import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.RunRecord;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Store for application metadata
 */
public class AppMetadataStore extends MetadataStoreDataset {
  private static final Logger LOG = LoggerFactory.getLogger(AppMetadataStore.class);

  private static final Gson GSON = ApplicationSpecificationAdapter.addTypeAdapters(new GsonBuilder()).create();

  public static final String TYPE_APP_META = "appMeta";
  public static final String TYPE_STREAM = "stream";
  public static final String TYPE_RUN_RECORD_STARTED = "runRecordStarted";
  public static final String TYPE_RUN_RECORD_COMPLETED = "runRecordCompleted";
  public static final String TYPE_PROGRAM_ARGS = "programArgs";
  private static final String TYPE_NAMESPACE = "namespace";
  private static final String TYPE_ADAPTER = "adapter";

  public AppMetadataStore(Table table) {
    super(table);
  }

  @Override
  protected <T> byte[] serialize(T value) {
    return Bytes.toBytes(GSON.toJson(value));
  }

  @Override
  protected <T> T deserialize(byte[] serialized, Class<T> classOfT) {
    return GSON.fromJson(Bytes.toString(serialized), classOfT);
  }

  @Nullable
  public ApplicationMeta getApplication(String namespaceId, String appId) {
    return get(new MDSKey.Builder().add(TYPE_APP_META, namespaceId, appId).build(), ApplicationMeta.class);
  }

  public List<ApplicationMeta> getAllApplications(String namespaceId) {
    return list(new MDSKey.Builder().add(TYPE_APP_META, namespaceId).build(), ApplicationMeta.class);
  }

  public void writeApplication(String namespaceId, String appId, ApplicationSpecification spec,
                               String archiveLocation) {
    // NOTE: we use Gson underneath to do serde, as it doesn't serialize inner classes (which we use everywhere for
    //       specs - see forwarding specs), we want to wrap spec with DefaultApplicationSpecification
    spec = DefaultApplicationSpecification.from(spec);
    write(new MDSKey.Builder().add(TYPE_APP_META, namespaceId, appId).build(),
          new ApplicationMeta(appId, spec, archiveLocation));
  }

  public void deleteApplication(String namespaceId, String appId) {
    deleteAll(new MDSKey.Builder().add(TYPE_APP_META, namespaceId, appId).build());
  }

  public void deleteApplications(String namespaceId) {
    deleteAll(new MDSKey.Builder().add(TYPE_APP_META, namespaceId).build());
  }

  // todo: do we need appId? may be use from appSpec?
  public void updateAppSpec(String namespaceId, String appId, ApplicationSpecification spec) {
    // NOTE: we use Gson underneath to do serde, as it doesn't serialize inner classes (which we use everywhere for
    //       specs - see forwarding specs), we want to wrap spec with DefaultApplicationSpecification
    spec = DefaultApplicationSpecification.from(spec);
    LOG.trace("App spec to be updated: id: {}: spec: {}", appId, GSON.toJson(spec));
    MDSKey key = new MDSKey.Builder().add(TYPE_APP_META, namespaceId, appId).build();
    ApplicationMeta existing = get(key, ApplicationMeta.class);
    if (existing == null) {
      String msg = String.format("No meta for namespace %s app %s exists", namespaceId, appId);
      LOG.error(msg);
      throw new IllegalArgumentException(msg);
    }

    LOG.trace("Application exists in mds: id: {}, spec: {}", existing);
    ApplicationMeta updated = ApplicationMeta.updateSpec(existing, spec);
    write(key, updated);

    for (StreamSpecification stream : spec.getStreams().values()) {
      writeStream(namespaceId, stream);
    }
  }

  public void recordProgramStart(Id.Program program, String pid, long startTs) {
      write(new MDSKey.Builder().add(TYPE_RUN_RECORD_STARTED)
              .add(program.getNamespaceId())
              .add(program.getApplicationId())
              .add(program.getType().name())
              .add(program.getId())
              .add(pid)
              .build(),
            new RunRecord(pid, startTs, null, ProgramRunStatus.RUNNING));
  }

  public void recordProgramStop(Id.Program program, String pid, long stopTs, ProgramRunStatus runStatus) {
    MDSKey key = new MDSKey.Builder()
      .add(TYPE_RUN_RECORD_STARTED)
      .add(program.getNamespaceId())
      .add(program.getApplicationId())
      .add(program.getType().name())
      .add(program.getId())
      .add(pid)
      .build();
    RunRecord started = get(key, RunRecord.class);
    if (started == null) {
      String msg = String.format("No meta for started run record for namespace %s app %s program type %s " +
                                 "program %s pid %s exists",
                                 program.getNamespaceId(), program.getApplicationId(), program.getType().name(),
                                 program.getId(), pid);
      LOG.error(msg);
      throw new IllegalArgumentException(msg);
    }

    deleteAll(key);

    key = new MDSKey.Builder()
      .add(TYPE_RUN_RECORD_COMPLETED)
      .add(program.getNamespaceId())
      .add(program.getApplicationId())
      .add(program.getType().name())
      .add(program.getId())
      .add(getInvertedTsKeyPart(started.getStartTs()))
      .add(pid).build();
    write(key, new RunRecord(started, stopTs, runStatus));
  }

  public List<RunRecord> getRuns(Id.Program program, ProgramRunStatus status,
                                 long startTime, long endTime, int limit) {
    if (status.equals(ProgramRunStatus.ALL)) {
      List<RunRecord> resultRecords = Lists.newArrayList();
      resultRecords.addAll(getActiveRuns(program, startTime, endTime, limit));
      resultRecords.addAll(getHistoricalRuns(program, status, startTime, endTime, limit));
      return resultRecords;
    } else if (status.equals(ProgramRunStatus.RUNNING)) {
      return getActiveRuns(program, startTime, endTime, limit);
    } else {
      return getHistoricalRuns(program, status, startTime, endTime, limit);
    }
  }

  private List<RunRecord> getActiveRuns(Id.Program program, final long startTime, final long endTime, int limit) {
    MDSKey activeKey = new MDSKey.Builder()
      .add(TYPE_RUN_RECORD_STARTED)
      .add(program.getNamespaceId())
      .add(program.getApplicationId())
      .add(program.getType().name())
      .add(program.getId())
      .build();
    MDSKey start = new MDSKey.Builder(activeKey).add(getInvertedTsKeyPart(endTime)).build();
    MDSKey stop = new MDSKey.Builder(activeKey).add(getInvertedTsKeyPart(startTime)).build();
    return list(start, stop, RunRecord.class, limit, Predicates.<RunRecord>alwaysTrue());
  }

  private List<RunRecord> getHistoricalRuns(Id.Program program, ProgramRunStatus status,
                                            final long startTime, final long endTime, int limit) {
    MDSKey historyKey = new MDSKey.Builder().add(TYPE_RUN_RECORD_COMPLETED,
                                                 program.getNamespaceId(),
                                                 program.getApplicationId(),
                                                 program.getType().name(),
                                                 program.getId()).build();
    MDSKey start = new MDSKey.Builder(historyKey).add(getInvertedTsKeyPart(endTime)).build();
    MDSKey stop = new MDSKey.Builder(historyKey).add(getInvertedTsKeyPart(startTime)).build();
    if (status.equals(ProgramRunStatus.ALL)) {
      //return all records (successful and failed)
      return list(start, stop, RunRecord.class, limit, Predicates.<RunRecord>alwaysTrue());
    }
    if (status.equals(ProgramRunStatus.COMPLETED)) {
      return list(start, stop, RunRecord.class, limit, getPredicate(ProgramController.State.COMPLETED));
    }
    if (status.equals(ProgramRunStatus.KILLED)) {
      return list(start, stop, RunRecord.class, limit, getPredicate(ProgramController.State.KILLED));
    }
    return list(start, stop, RunRecord.class, limit, getPredicate(ProgramController.State.ERROR));
  }

  private Predicate<RunRecord> getPredicate(final ProgramController.State state) {
    return new Predicate<RunRecord>() {
      @Override
      public boolean apply(RunRecord record) {
        return record.getStatus().equals(state.getRunStatus());
      }
    };
  }

  private long getInvertedTsKeyPart(long endTime) {
    return Long.MAX_VALUE - endTime;
  }

  public void writeStream(String namespaceId, StreamSpecification spec) {
    write(new MDSKey.Builder().add(TYPE_STREAM, namespaceId, spec.getName()).build(), spec);
  }

  public StreamSpecification getStream(String namespaceId, String name) {
    return get(new MDSKey.Builder().add(TYPE_STREAM, namespaceId, name).build(), StreamSpecification.class);
  }

  public List<StreamSpecification> getAllStreams(String namespaceId) {
    return list(new MDSKey.Builder().add(TYPE_STREAM, namespaceId).build(), StreamSpecification.class);
  }

  public void deleteAllStreams(String namespaceId) {
    deleteAll(new MDSKey.Builder().add(TYPE_STREAM, namespaceId).build());
  }

  public void deleteStream(String namespaceId, String name) {
    deleteAll(new MDSKey.Builder().add(TYPE_STREAM, namespaceId, name).build());
  }

  public void writeProgramArgs(Id.Program program, Map<String, String> args) {
    write(new MDSKey.Builder()
            .add(TYPE_PROGRAM_ARGS)
            .add(program.getNamespaceId())
            .add(program.getApplicationId())
            .add(program.getType().name())
            .add(program.getId())
            .build(), new ProgramArgs(args));
  }

  public ProgramArgs getProgramArgs(Id.Program program) {
    return get(new MDSKey.Builder()
                 .add(TYPE_PROGRAM_ARGS)
                 .add(program.getNamespaceId())
                 .add(program.getApplicationId())
                 .add(program.getType().name())
                 .add(program.getId())
                 .build(), ProgramArgs.class);
  }

  public void deleteProgramArgs(Id.Program program) {
    deleteAll(new MDSKey.Builder().add(TYPE_PROGRAM_ARGS)
                .add(program.getNamespaceId())
                .add(program.getApplicationId())
                .add(program.getType().name())
                .add(program.getId())
                .build());
  }

  public void deleteProgramArgs(String namespaceId, String appId) {
    deleteAll(new MDSKey.Builder().add(TYPE_PROGRAM_ARGS, namespaceId, appId).build());
  }

  public void deleteProgramArgs(String namespaceId) {
    deleteAll(new MDSKey.Builder().add(TYPE_PROGRAM_ARGS, namespaceId).build());
  }

  public void deleteProgramHistory(String namespaceId, String appId) {
    deleteAll(new MDSKey.Builder().add(TYPE_RUN_RECORD_STARTED, namespaceId, appId).build());
    deleteAll(new MDSKey.Builder().add(TYPE_RUN_RECORD_COMPLETED, namespaceId, appId).build());
  }

  public void deleteProgramHistory(String namespaceId) {
    deleteAll(new MDSKey.Builder().add(TYPE_RUN_RECORD_STARTED, namespaceId).build());
    deleteAll(new MDSKey.Builder().add(TYPE_RUN_RECORD_COMPLETED, namespaceId).build());
  }

  public void createNamespace(NamespaceMeta metadata) {
    write(getNamespaceKey(metadata.getName()), metadata);
  }

  public NamespaceMeta getNamespace(Id.Namespace id) {
    return get(getNamespaceKey(id.getId()), NamespaceMeta.class);
  }

  public void deleteNamespace(Id.Namespace id) {
    deleteAll(getNamespaceKey(id.getId()));
  }

  public List<NamespaceMeta> listNamespaces() {
    return list(getNamespaceKey(null), NamespaceMeta.class);
  }

  public void writeAdapter(Id.Namespace id, AdapterSpecification adapterSpec, AdapterStatus adapterStatus) {
    write(new MDSKey.Builder().add(TYPE_ADAPTER, id.getId(), adapterSpec.getName()).build(),
          new AdapterMeta(adapterSpec, adapterStatus));
  }


  @Nullable
  public AdapterSpecification getAdapter(Id.Namespace id, String name) {
    AdapterMeta adapterMeta = getAdapterMeta(id, name);
    return adapterMeta == null ?  null : adapterMeta.getSpec();
  }

  @Nullable
  public AdapterStatus getAdapterStatus(Id.Namespace id, String name) {
    AdapterMeta adapterMeta = getAdapterMeta(id, name);
    return adapterMeta == null ?  null : adapterMeta.getStatus();
  }

  @Nullable
  public AdapterStatus setAdapterStatus(Id.Namespace id, String name, AdapterStatus status) {
    AdapterMeta adapterMeta = getAdapterMeta(id, name);
    if (adapterMeta == null) {
      return null;
    }
    AdapterStatus previousStatus = adapterMeta.getStatus();
    writeAdapter(id, adapterMeta.getSpec(), status);
    return previousStatus;
  }

  private AdapterMeta getAdapterMeta(Id.Namespace id, String name) {
    return get(new MDSKey.Builder().add(TYPE_ADAPTER, id.getId(), name).build(), AdapterMeta.class);
  }

  public List<AdapterSpecification> getAllAdapters(Id.Namespace id) {
    List<AdapterSpecification> adapterSpecs = Lists.newArrayList();
    List<AdapterMeta> adapterMetas = list(new MDSKey.Builder().add(TYPE_ADAPTER, id.getId()).build(),
                                          AdapterMeta.class);
    for (AdapterMeta adapterMeta : adapterMetas) {
      adapterSpecs.add(adapterMeta.getSpec());
    }
    return adapterSpecs;
  }

  public void deleteAdapter(Id.Namespace id, String name) {
    deleteAll(new MDSKey.Builder().add(TYPE_ADAPTER, id.getId(), name).build());
  }

  public void deleteAllAdapters(Id.Namespace id) {
    deleteAll(new MDSKey.Builder().add(TYPE_ADAPTER, id.getId()).build());
  }

  private MDSKey getNamespaceKey(@Nullable String name) {
    MDSKey.Builder builder = new MDSKey.Builder().add(TYPE_NAMESPACE);
    if (null != name) {
      builder.add(name);
    }
    return builder.build();
  }
}
