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

package co.cask.cdap.test;

import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.RunRecord;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Abstract implementation of {@link ProgramManager} that includes common functionality for all implementations.
 * @param <T> The type of ProgramManager
 */
public abstract class AbstractProgramManager<T extends ProgramManager> implements ProgramManager<T> {
  protected final Id.Program programId;
  private final ApplicationManager applicationManager;

  public AbstractProgramManager(Id.Program programId, ApplicationManager applicationManager) {
    this.applicationManager = applicationManager;
    this.programId = programId;
  }

  @Override
  public T start() {
    return start(ImmutableMap.<String, String>of());
  }

  @Override
  public T start(Map<String, String> arguments) {
    applicationManager.startProgram(programId, arguments);
    // this cast is fine as long as the derived classes extend AbstractProgramManager with the
    // template (<T>) declared as its own class
    return (T) this;
  }

  @Override
  public void stop() {
    applicationManager.stopProgram(programId);
  }

  @Override
  public boolean isRunning() {
    return applicationManager.isRunning(programId);
  }

  @Override
  public void waitForFinish(long timeout, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException {
    // Min sleep time is 10ms, max sleep time is 1 seconds
    long sleepMillis = Math.max(10, Math.min(timeoutUnit.toMillis(timeout) / 10, TimeUnit.SECONDS.toMillis(1)));
    waitForStatus(false, sleepMillis, timeoutUnit.toMillis(timeout), TimeUnit.MILLISECONDS);
  }

  @Override
  public void waitForStatus(boolean status) throws InterruptedException {
    waitForStatus(status, 50, 5000, TimeUnit.MILLISECONDS);
  }

  @Override
  public void waitForStatus(boolean status, int retries, int timeout) throws InterruptedException {
    // The API is not good. The "timeout" parameter is actually sleep time
    // We calculate the actual timeout by multiplying retries with sleep time
    long sleepTimeMillis = TimeUnit.SECONDS.toMillis(timeout);
    long timeoutMillis = sleepTimeMillis * retries;
    waitForStatus(status, sleepTimeMillis, timeoutMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Waits for the program status.
   *
   * @param status true to wait for start, false for stop
   * @param sleepTime time to sleep in between polling
   * @param timeout timeout for the polling if the status doesn't match
   * @param timeoutUnit unit for both sleepTime and timeout
   */
  protected void waitForStatus(boolean status, long sleepTime,
                               long timeout, TimeUnit timeoutUnit) throws InterruptedException {
    long timeoutMillis = timeoutUnit.toMillis(timeout);
    boolean statusMatched = status == isRunning();
    long startTime = System.currentTimeMillis();
    while (!statusMatched && (System.currentTimeMillis() - startTime) < timeoutMillis) {
      timeoutUnit.sleep(sleepTime);
      statusMatched = status == isRunning();
    }

    if (!statusMatched) {
      throw new IllegalStateException(String.format("Program state for '%s' not as expected. Expected '%s'.",
                                                    programId, status));
    }
  }

  @Override
  public List<RunRecord> getHistory() {
    return getHistory(ProgramRunStatus.ALL);
  }

  @Override
  public List<RunRecord> getHistory(ProgramRunStatus status) {
    return applicationManager.getHistory(programId, status);
  }
}
