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

package co.cask.cdap.gateway.handlers.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.common.logging.ApplicationLoggingContext;
import co.cask.cdap.common.logging.LoggingContext;
import co.cask.cdap.internal.app.store.DefaultStore;
import co.cask.cdap.logging.context.FlowletLoggingContext;
import co.cask.cdap.logging.context.LoggingContextHelper;
import co.cask.cdap.logging.context.MapReduceLoggingContext;
import co.cask.cdap.logging.context.UserServiceLoggingContext;
import co.cask.cdap.logging.context.WorkflowLoggingContext;
import co.cask.cdap.logging.filter.Filter;
import co.cask.cdap.logging.read.Callback;
import co.cask.cdap.logging.read.LogEvent;
import co.cask.cdap.logging.read.LogOffset;
import co.cask.cdap.logging.read.LogReader;
import co.cask.cdap.logging.read.ReadRange;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.RunRecord;
import co.cask.cdap.test.SlowTests;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.twill.api.RunId;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
* Mock LogReader for testing.
*/
@Category(SlowTests.class)
public class MockLogReader implements LogReader {
  private static final Logger LOG = LoggerFactory.getLogger(MockLogReader.class);

  public static final String TEST_NAMESPACE = "testNamespace";
  private static final int MAX = 80;

  private final DefaultStore store;
  private final List<LogEvent> logEvents = Lists.newArrayList();
  private final Map<Id, RunRecord> runRecordMap = Maps.newHashMap();

  @Inject
  MockLogReader(DefaultStore store) {
    this.store = store;
  }

  public void generateLogs() throws InterruptedException {
    // Add logs for app testApp2, flow testFlow1
    generateLogs(new FlowletLoggingContext(Id.Namespace.DEFAULT.getId(),
                                           "testApp2", "testFlow1", "testFlowlet1", "", ""),
                 Id.Program.from(Id.Namespace.DEFAULT.getId(), "testApp2", ProgramType.FLOW, "testFlow1"),
                 ProgramRunStatus.RUNNING);

    // Add logs for app testApp3, mapreduce testMapReduce1
    generateLogs(new MapReduceLoggingContext(Id.Namespace.DEFAULT.getId(),
                                             "testApp3", "testMapReduce1", ""),
                 Id.Program.from(Id.Namespace.DEFAULT.getId(), "testApp3", ProgramType.MAPREDUCE, "testMapReduce1"),
                 ProgramRunStatus.SUSPENDED);

    // Add logs for app testApp1, service testService1
    generateLogs(new UserServiceLoggingContext(Id.Namespace.DEFAULT.getId(),
                                               "testApp4", "testService1", "test1", "", ""),
                 Id.Program.from(Id.Namespace.DEFAULT.getId(), "testApp4", ProgramType.SERVICE, "testService1"),
                 ProgramRunStatus.RUNNING);

    // Add logs for app testApp1, mapreduce testMapReduce1
    generateLogs(new MapReduceLoggingContext(TEST_NAMESPACE,
                                             "testTemplate1", "testMapReduce1", ""),
                 Id.Program.from(TEST_NAMESPACE, "testTemplate1", ProgramType.MAPREDUCE, "testMapReduce1"),
                 ProgramRunStatus.COMPLETED);

    // Add logs for app testApp1, flow testFlow1 in testNamespace
    generateLogs(new FlowletLoggingContext(TEST_NAMESPACE,
                                           "testApp1", "testFlow1", "testFlowlet1", "", ""),
                 Id.Program.from(TEST_NAMESPACE, "testApp1", ProgramType.FLOW, "testFlow1"),
                 ProgramRunStatus.COMPLETED);

    // Add logs for app testApp1, service testService1 in testNamespace
    generateLogs(new UserServiceLoggingContext(TEST_NAMESPACE,
                                               "testApp4", "testService1", "test1", "", ""),
                 Id.Program.from(TEST_NAMESPACE, "testApp4", ProgramType.SERVICE, "testService1"),
                 ProgramRunStatus.KILLED);

    // Add logs for testWorkflow1 in testNamespace
    generateLogs(new WorkflowLoggingContext(TEST_NAMESPACE,
                                            "testTemplate1", "testWorkflow1", "testRun1"),
                 Id.Program.from(TEST_NAMESPACE, "testTemplate1", ProgramType.WORKFLOW, "testWorkflow1"),
                 ProgramRunStatus.COMPLETED);
    // Add logs for testWorkflow1 in default namespace
    generateLogs(new WorkflowLoggingContext(Id.Namespace.DEFAULT.getId(),
                                            "testTemplate1", "testWorkflow1", "testRun2"),
                 Id.Program.from(Id.Namespace.DEFAULT.getId(), "testTemplate1", ProgramType.WORKFLOW, "testWorkflow1"),
                 ProgramRunStatus.COMPLETED);
  }

  public RunRecord getRunRecord(Id id) {
    return runRecordMap.get(id);
  }

  @Override
  public void getLogNext(LoggingContext loggingContext, ReadRange readRange, int maxEvents, Filter filter,
                         Callback callback) {
    if (readRange.getKafkaOffset() < 0) {
      getLogPrev(loggingContext, readRange, maxEvents, filter, callback);
      return;
    }

    Filter contextFilter = LoggingContextHelper.createFilter(loggingContext);

    callback.init();
    try {
      int count = 0;
      for (LogEvent logLine : logEvents) {
        if (logLine.getOffset().getKafkaOffset() >= readRange.getKafkaOffset()) {
          long logTime = logLine.getLoggingEvent().getTimeStamp();
          if (!contextFilter.match(logLine.getLoggingEvent()) || logTime < readRange.getFromMillis() ||
            logTime >= readRange.getToMillis()) {
            continue;
          }

          if (++count > maxEvents) {
            break;
          }

          if (filter != Filter.EMPTY_FILTER && logLine.getOffset().getKafkaOffset() % 2 != 0) {
            continue;
          }

          callback.handle(logLine);
        }
      }
    } catch (Throwable e) {
      LOG.error("Got exception", e);
    } finally {
      callback.close();
    }
  }

  @Override
  public void getLogPrev(LoggingContext loggingContext, ReadRange readRange, int maxEvents, Filter filter,
                         Callback callback) {
    if (readRange.getKafkaOffset() < 0) {
      readRange = new ReadRange(readRange.getFromMillis(), readRange.getToMillis(), MAX);
    }

    Filter contextFilter = LoggingContextHelper.createFilter(loggingContext);

    callback.init();
    try {
      int count = 0;
      long startOffset = readRange.getKafkaOffset() - maxEvents;
      for (LogEvent logLine : logEvents) {
        long logTime = logLine.getLoggingEvent().getTimeStamp();
        if (!contextFilter.match(logLine.getLoggingEvent()) || logTime < readRange.getFromMillis() ||
          logTime >= readRange.getToMillis()) {
          continue;
        }

        if (logLine.getOffset().getKafkaOffset() >= startOffset &&
          logLine.getOffset().getKafkaOffset() < readRange.getKafkaOffset()) {
          if (++count > maxEvents) {
            break;
          }

          if (filter != Filter.EMPTY_FILTER && logLine.getOffset().getKafkaOffset() % 2 != 0) {
            continue;
          }

          callback.handle(logLine);
        }
      }
    } catch (Throwable e) {
      LOG.error("Got exception", e);
    } finally {
      callback.close();
    }
  }

  @Override
  public void getLog(LoggingContext loggingContext, long fromTimeMs, long toTimeMs, Filter filter,
                     Callback callback) {
    long fromOffset = getOffset(fromTimeMs / 1000);
    long toOffset = getOffset(toTimeMs / 1000);
    getLogNext(loggingContext, new ReadRange(fromTimeMs, toTimeMs, fromOffset),
               (int) (toOffset - fromOffset), filter, callback);
  }

  private static final Function<LoggingContext.SystemTag, String> TAG_TO_STRING_FUNCTION =
    new Function<LoggingContext.SystemTag, String>() {
      @Override
      public String apply(LoggingContext.SystemTag input) {
        return input.getValue();
      }
    };

  private void generateLogs(LoggingContext loggingContext, Id.Program id, ProgramRunStatus runStatus)
    throws InterruptedException {
    String entityId = LoggingContextHelper.getEntityId(loggingContext).getValue();
    RunId runId = null;
    Long stopTs = null;
    for (int i = 0; i < MAX; ++i) {
      // Setup run id for event with ids >= 20
      if (i == 20) {
        runId = RunIds.generate(TimeUnit.SECONDS.toMillis(getMockTimeSecs(i)));
      } else if (i == 60 && runStatus != ProgramRunStatus.RUNNING && runStatus != ProgramRunStatus.SUSPENDED) {
        // Record stop time for run for 60th event, but still continue to record run in the other logging events.
        stopTs = getMockTimeSecs(i);
      }

      LoggingEvent event =
        new LoggingEvent("co.cask.Test",
                         (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME),
                         i % 2 == 0 ? Level.ERROR : Level.WARN, entityId + "<img>-" + i, null, null);
      event.setTimeStamp(TimeUnit.SECONDS.toMillis(getMockTimeSecs(i)));

      // Add runid to logging context
      Map<String, String> tagMap = Maps.newHashMap(Maps.transformValues(loggingContext.getSystemTagsMap(),
                                                                         TAG_TO_STRING_FUNCTION));
      if (runId != null && i % 2 == 0) {
        tagMap.put(ApplicationLoggingContext.TAG_RUNID_ID, runId.getId());
      }
      event.setMDCPropertyMap(tagMap);
      logEvents.add(new LogEvent(event, new LogOffset(i, i)));
    }

    long startTs = RunIds.getTime(runId, TimeUnit.SECONDS);
    if (id != null) {
      //noinspection ConstantConditions
      runRecordMap.put(id, new RunRecord(runId.getId(), startTs, stopTs, runStatus, null));
      store.setStart(id, runId.getId(), startTs);
      if (stopTs != null) {
        store.setStop(id, runId.getId(), stopTs, runStatus);
      }
    }
  }

  public static long getMockTimeSecs(int offset) {
    return offset * 10;
  }

  private static long getOffset(long mockTimeSecs) {
    return mockTimeSecs / 10;
  }
 }
