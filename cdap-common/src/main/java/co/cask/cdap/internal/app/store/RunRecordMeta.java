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
package co.cask.cdap.internal.app.store;

import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.RunRecord;
import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import javax.annotation.Nullable;

/**
 * Store the meta information about program runs in CDAP. Extends {@link RunRecord} with additional
 * information about Apache Twill run id.
 */
public final class RunRecordMeta extends RunRecord {
  @SerializedName("twillrunid")
  private final String twillRunId;

  public RunRecordMeta(String pid, long startTs, @Nullable Long stopTs, ProgramRunStatus status,
                       @Nullable Map<String, String> properties,
                       @Nullable String twillRunId) {
    super(pid, startTs, stopTs, status, properties);
    this.twillRunId = twillRunId;
  }

  public RunRecordMeta(String pid, long startTs, @Nullable Long stopTs, ProgramRunStatus status) {
    this(pid, startTs, stopTs, status, null, null);
  }

  public RunRecordMeta(RunRecordMeta started, @Nullable Long stopTs, ProgramRunStatus status) {
    this(started.getPid(), started.getStartTs(), stopTs, status, started.getProperties(),
         started.getTwillRunId());
  }

  public RunRecordMeta(RunRecordMeta existing, Map<String, String> updatedProperties) {
    this(existing.getPid(), existing.getStartTs(), existing.getStopTs(), existing.getStatus(),
         updatedProperties, existing.getTwillRunId());
  }

  @Nullable
  public String getTwillRunId() {
    return twillRunId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RunRecordMeta that = (RunRecordMeta) o;
    return Objects.equal(this.getPid(), that.getPid()) &&
      Objects.equal(this.getStartTs(), that.getStartTs()) &&
      Objects.equal(this.getStopTs(), that.getStopTs()) &&
      Objects.equal(this.getStatus(), that.getStatus()) &&
      Objects.equal(this.getProperties(), that.getProperties()) &&
      Objects.equal(this.getTwillRunId(), that.getTwillRunId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(twillRunId, getPid(), getStartTs(), getStopTs(), getStatus(), getProperties());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("pid", getPid())
      .add("startTs", getStartTs())
      .add("stopTs", getStopTs())
      .add("status", getStatus())
      .add("twillrunid", twillRunId)
      .add("properties", getProperties())
      .toString();
  }
}
