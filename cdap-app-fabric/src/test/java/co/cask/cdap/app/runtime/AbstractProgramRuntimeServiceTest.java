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

package co.cask.cdap.app.runtime;

import co.cask.cdap.api.app.ApplicationSpecification;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.utils.Tasks;
import co.cask.cdap.internal.app.runtime.ProgramControllerServiceAdapter;
import co.cask.cdap.internal.app.runtime.ProgramRunnerFactory;
import co.cask.cdap.internal.app.runtime.SimpleProgramOptions;
import co.cask.cdap.internal.app.runtime.artifact.ArtifactRepository;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramLiveInfo;
import co.cask.cdap.proto.ProgramType;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import org.apache.twill.api.RunId;
import org.apache.twill.filesystem.Location;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

/**
 * Unit-test for AbstractProgramRuntimeService base functionalities.
 */
public class AbstractProgramRuntimeServiceTest {

  @ClassRule
  public static final TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

  @Test (timeout = 5000)
  public void testDeadlock() throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // This test is for testing condition in (CDAP-3579)
    // The race condition is if a program finished very fast such that inside the AbstractProgramRuntimeService is
    // still in the run method, it holds the object lock, making the callback from the listener block forever.
    ProgramRunnerFactory runnerFactory = createProgramRunnerFactory();

    ProgramRuntimeService runtimeService = new AbstractProgramRuntimeService(CConfiguration.create(),
                                                                             runnerFactory, null) {
      @Override
      public ProgramLiveInfo getLiveInfo(Id.Program programId) {
        return new ProgramLiveInfo(programId, "runtime") { };
      }
    };

    runtimeService.startAndWait();
    try {
      Program program = createDummyProgram();
      final ProgramController controller =
        runtimeService.run(program, new SimpleProgramOptions(program)).getController();
      Tasks.waitFor(ProgramController.State.COMPLETED, new Callable<ProgramController.State>() {
        @Override
        public ProgramController.State call() throws Exception {
          return controller.getState();
        }
      }, 5, TimeUnit.SECONDS, 100, TimeUnit.MILLISECONDS);

      Assert.assertTrue(runtimeService.list(ProgramType.WORKER).isEmpty());
    } finally {
      runtimeService.stopAndWait();
    }
  }

  @Test (timeout = 5000L)
  public void testUpdateDeadLock() {
    // This test is for testing (CDAP-3716)
    // Create a service to simulate an existing running app.
    Service service = new TestService();
    Id.Program programId = Id.Program.from(Id.Namespace.DEFAULT, "dummyApp", ProgramType.WORKER, "dummy");
    RunId runId = RunIds.generate();
    ProgramRuntimeService.RuntimeInfo extraInfo = createRuntimeInfo(service, programId, runId);
    service.startAndWait();

    ProgramRunnerFactory runnerFactory = createProgramRunnerFactory();
    TestProgramRuntimeService runtimeService = new TestProgramRuntimeService(CConfiguration.create(),
                                                                             runnerFactory, null, extraInfo);
    runtimeService.startAndWait();

    // The lookup will get deadlock for CDAP-3716
    Assert.assertNotNull(runtimeService.lookup(programId, runId));
    service.stopAndWait();

    runtimeService.stopAndWait();
  }

  private ProgramRunnerFactory createProgramRunnerFactory() {
    return new ProgramRunnerFactory() {
      @Override
      public ProgramRunner create(Type programType) {
        return new ProgramRunner() {
          @Override
          public ProgramController run(Program program, ProgramOptions options) {
            Service service = new FastService();
            ProgramController controller = new ProgramControllerServiceAdapter(service, program.getId(),
                                                                               RunIds.generate());
            service.start();
            return controller;
          }
        };
      }
    };
  }

  private Program createDummyProgram() throws IOException {
    return new Program() {
      @Override
      public String getMainClassName() {
        return null;
      }

      @Override
      public <T> Class<T> getMainClass() throws ClassNotFoundException {
        return null;
      }

      @Override
      public ProgramType getType() {
        return ProgramType.WORKER;
      }

      @Override
      public Id.Program getId() {
        return Id.Program.from(Id.Namespace.DEFAULT, "dummyApp", ProgramType.WORKER, "dummy");
      }

      @Override
      public String getName() {
        return getId().getId();
      }

      @Override
      public String getNamespaceId() {
        return getId().getNamespaceId();
      }

      @Override
      public String getApplicationId() {
        return getId().getApplicationId();
      }

      @Override
      public ApplicationSpecification getApplicationSpecification() {
        return null;
      }

      @Override
      public Location getJarLocation() {
        return null;
      }

      @Override
      public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
      }

      @Override
      public void close() throws IOException {
        // No-op
      }
    };
  }

  private ProgramRuntimeService.RuntimeInfo createRuntimeInfo(Service service,
                                                              final Id.Program programId, RunId runId) {
    final ProgramControllerServiceAdapter controller = new ProgramControllerServiceAdapter(service, programId, runId);
    return new ProgramRuntimeService.RuntimeInfo() {
      @Override
      public ProgramController getController() {
        return controller;
      }

      @Override
      public ProgramType getType() {
        return programId.getType();
      }

      @Override
      public Id.Program getProgramId() {
        return programId;
      }

      @Nullable
      @Override
      public RunId getTwillRunId() {
        return null;
      }
    };
  }

  /**
   * A guava service that has wil be completed immediate right after it is started.
   */
  private static final class FastService extends AbstractExecutionThreadService {

    @Override
    protected void run() throws Exception {
      // No-op
    }
  }

  /**
   * A guava service that will not terminate until stop is called.
   */
  private static final class TestService extends AbstractIdleService {

    @Override
    protected void startUp() throws Exception {
      // no-op
    }

    @Override
    protected void shutDown() throws Exception {
      // no-op
    }
  }

  /**
   * A runtime service that will insert an extra RuntimeInfo for the lookup call to simulate the situation
   * where an app is already running when the service starts.
   */
  private static final class TestProgramRuntimeService extends AbstractProgramRuntimeService {

    private final RuntimeInfo extraInfo;

    protected TestProgramRuntimeService(CConfiguration cConf, ProgramRunnerFactory programRunnerFactory,
                                        @Nullable ArtifactRepository artifactRepository, RuntimeInfo extraInfo) {
      super(cConf, programRunnerFactory, artifactRepository);
      this.extraInfo = extraInfo;
    }

    @Override
    public ProgramLiveInfo getLiveInfo(Id.Program programId) {
      return new ProgramLiveInfo(programId, "runtime") { };
    }

    @Override
    public RuntimeInfo lookup(Id.Program programId, RunId runId) {
      RuntimeInfo info = super.lookup(programId, runId);
      if (info != null) {
        return info;
      }

      updateRuntimeInfo(programId.getType(), runId, extraInfo);
      return extraInfo;
    }
  }
}
