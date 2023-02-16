/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.test.internal;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.FlowletDefinition;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.Flowlet;
import com.continuuity.api.flow.flowlet.GeneratorFlowlet;
import com.continuuity.api.procedure.AbstractProcedure;
import com.continuuity.api.procedure.Procedure;
import com.continuuity.api.procedure.ProcedureSpecification;
import com.continuuity.app.Id;
import com.continuuity.app.deploy.Manager;
import com.continuuity.app.deploy.ManagerFactory;
import com.continuuity.app.program.ManifestFields;
import com.continuuity.app.services.AppFabricService;
import com.continuuity.app.services.AuthToken;
import com.continuuity.app.services.DeploymentStatus;
import com.continuuity.app.services.ResourceIdentifier;
import com.continuuity.app.services.ResourceInfo;
import com.continuuity.archive.JarFinder;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.internal.app.BufferFileInputStream;
import com.continuuity.internal.app.deploy.LocalManager;
import com.continuuity.internal.app.deploy.pipeline.ApplicationWithPrograms;
import com.continuuity.test.internal.bytecode.FlowletRewriter;
import com.continuuity.test.internal.bytecode.ProcedureRewriter;
import com.continuuity.test.internal.guice.AppFabricTestModule;
import com.continuuity.weave.filesystem.LocalLocationFactory;
import com.continuuity.weave.filesystem.Location;
import com.continuuity.weave.filesystem.LocationFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * This is a test helper for our internal test.
 * <p>
 *   <i>Note: please don't include this in the developer test</i>
 * </p>
 */
public class TestHelper {

  public static final AuthToken DUMMY_AUTH_TOKEN = new AuthToken("appFabricTest");
  public static final TempFolder TEMP_FOLDER = new TempFolder();
  public static CConfiguration configuration;
  private static Injector injector;

  public static Injector getInjector() {
    return getInjector(CConfiguration.create());
  }

  public static synchronized Injector getInjector(CConfiguration conf) {
    if (injector == null) {
      configuration = conf;
      configuration.set("app.output.dir", TEMP_FOLDER.newFolder("app").getAbsolutePath());
      configuration.set("app.tmp.dir", TEMP_FOLDER.newFolder("temp").getAbsolutePath());
      injector = Guice.createInjector(new AppFabricTestModule(configuration));
      injector.getInstance(InMemoryTransactionManager.class).init();
    }
    return injector;
  }

  /**
   * Given a class generates a manifest file with main-class as class.
   *
   * @param klass to set as Main-Class in manifest file.
   * @return An instance {@link Manifest}
   */
  public static Manifest getManifestWithMainClass(Class<?> klass) {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(ManifestFields.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(ManifestFields.MAIN_CLASS, klass.getName());
    return manifest;
  }

  /**
   * @return Returns an instance of {@link LocalManager}
   */
  public static Manager<Location, ApplicationWithPrograms> getLocalManager() {
    ManagerFactory factory = getInjector().getInstance(ManagerFactory.class);
    return factory.create();
  }

  public static void deployApplication(Class<? extends Application> application) throws Exception {
    deployApplication(application,
                      "app-" + TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS) + ".jar");
  }

  public static ApplicationWithPrograms deployApplicationWithManager(Class<? extends Application> appClass)
                                                                                                      throws Exception {
    LocalLocationFactory lf = new LocalLocationFactory();

    Location deployedJar = lf.create(
      JarFinder.getJar(appClass, TestHelper.getManifestWithMainClass(appClass))
    );
    try {
      ListenableFuture<?> p = TestHelper.getLocalManager().deploy(DefaultId.ACCOUNT, deployedJar);
      return (ApplicationWithPrograms) p.get();
    } finally {
      deployedJar.delete(true);
    }
  }

  /**
   *
   */
  public static void deployApplication(Class<? extends Application> applicationClz, String fileName) throws Exception {
    Location deployedJar =
      deployApplication(getInjector().getInstance(AppFabricService.Iface.class),
                        getInjector().getInstance(LocationFactory.class), DefaultId.ACCOUNT,
                        DUMMY_AUTH_TOKEN, "", fileName, applicationClz);
    deployedJar.delete(true);
  }

  /**
   *
   */
  public static void deployApplication(final Id.Account account, final AuthToken token,
                                       Class<? extends Application> applicationClz, String fileName) throws Exception {
    Location deployedJar =
      deployApplication(getInjector().getInstance(AppFabricService.Iface.class),
                        getInjector().getInstance(LocationFactory.class), account, token,
                        "", fileName, applicationClz);
    deployedJar.delete(true);
  }

  public static Location deployApplication(AppFabricService.Iface appFabricServer,
                                           LocationFactory locationFactory,
                                           final Id.Account account,
                                           final AuthToken token,
                                           final String applicationId,
                                           final String fileName,
                                           Class<? extends Application> applicationClz) throws Exception {
    return deployApplication(appFabricServer, locationFactory, account.getId(), token, applicationId, fileName,
                             applicationClz);
  }

    private static Location deployApplication(AppFabricService.Iface appFabricServer,
                                          LocationFactory locationFactory,
                                          final String account,
                                          final AuthToken token,
                                          final String applicationId,
                                          final String fileName,
                                          Class<? extends Application> applicationClz) throws Exception {
      Preconditions.checkNotNull(applicationClz, "Application cannot be null.");

      Application application = applicationClz.newInstance();
      ApplicationSpecification appSpec = application.configure();
      Location deployedJar = locationFactory.create(createDeploymentJar(applicationClz, appSpec).toURI());

      ResourceIdentifier id = appFabricServer.init(
        token, new ResourceInfo(account, applicationId, fileName, 0, System.currentTimeMillis()));

      // Upload the jar file to remote location.
      BufferFileInputStream is = new BufferFileInputStream(deployedJar.getInputStream(), 100 * 1024);
      try {
        byte[] chunk = is.read();
        while (chunk.length > 0) {
          appFabricServer.chunk(token, id, ByteBuffer.wrap(chunk));
          chunk = is.read();
          DeploymentStatus status = appFabricServer.dstatus(token, id);
          Preconditions.checkState(status.getOverall() == 2, "Fail to deploy app.");
        }
      } finally {
        is.close();
      }

      // Deploy the app
      appFabricServer.deploy(token, id);
      int status = appFabricServer.dstatus(token, id).getOverall();
      while (status == 3) {
        status = appFabricServer.dstatus(token, id).getOverall();
        TimeUnit.MILLISECONDS.sleep(100);
      }
      Preconditions.checkState(status == 5, "Fail to deploy app.");
      return deployedJar;
  }

  private static File createDeploymentJar(Class<?> clz, ApplicationSpecification appSpec) {
    File testAppDir;
    File tmpDir;
    testAppDir = Files.createTempDir();

    File outputDir = new File(testAppDir, "app");
    tmpDir = new File(testAppDir, "tmp");

    outputDir.mkdirs();
    tmpDir.mkdirs();

    // Creates Manifest
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(ManifestFields.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(ManifestFields.MAIN_CLASS, clz.getName());

    ClassLoader loader = clz.getClassLoader();
    Preconditions.checkArgument(loader != null, "Cannot get ClassLoader for class " + clz);
    String classFile = clz.getName().replace('.', '/') + ".class";

    // for easier testing within IDE we pick jar file first, before making this publicly available
    // we need to add code here to throw an exception if the class is in classpath twice (file and jar)
    // see ENG-2961
    try {
      // first look for jar file (in classpath) that contains class and return it
      URI fileUri = null;
      for (Enumeration<URL> itr = loader.getResources(classFile); itr.hasMoreElements(); ) {
        URI uri = itr.nextElement().toURI();
        if (uri.getScheme().equals("jar")) {
          String rawSchemeSpecificPart = uri.getRawSchemeSpecificPart();
          if (rawSchemeSpecificPart.startsWith("file:") && rawSchemeSpecificPart.contains("!")) {
            String[] parts = rawSchemeSpecificPart.substring("file:".length()).split("!");
            return new File(parts[0]);
          } else {
            return new File(uri.getPath());
          }
        } else if (uri.getScheme().equals("file")) {
          // memorize file URI in case there is no jar that contains the class
          fileUri = uri;
        }
      }
      if (fileUri != null) {
        // build jar file based on class file and return it
        File baseDir = new File(fileUri).getParentFile();

        Package appPackage = clz.getPackage();
        String packagePath = appPackage == null ? "" : appPackage.getName().replace('.', '/');
        String basePath = baseDir.getAbsolutePath();
        File relativeBase = new File(basePath.substring(0, basePath.length() - packagePath.length()));
        File jarFile = File.createTempFile(String.format("%s-%d", clz.getSimpleName(), System.currentTimeMillis()),
                                           ".jar", tmpDir);
        return jarDir(baseDir, relativeBase, manifest, jarFile, appSpec);
      } else {
        // return null if neither existing jar was found nor jar was built based on class file
        return null;
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
  /**
   * Creates a jar of a directory and rewrite Flowlet and Procedure bytecodes to emit metrics.
   *
   * @param dir Directory root for creating jar entries.
   * @param manifest The Jar manifest content.
   * @param outputFile Location of the Jar file.
   * @param appSpec The {@link com.continuuity.api.ApplicationSpecification} of the deploying application.
   */
  private static File jarDir(File dir, File relativeBase, Manifest manifest,
                             File outputFile, ApplicationSpecification appSpec) throws IOException,
    ClassNotFoundException {

    JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(outputFile), manifest);
    Queue<File> queue = Lists.newLinkedList();
    Collections.addAll(queue, dir.listFiles());

    // Find all flowlet classes (flowlet class => flowId)
    // Note: Limitation now is that the same flowlet class can be used in one flow only (can have multiple names)
    Map<String, String> flowletClassNames = Maps.newHashMap();
    for (FlowSpecification flowSpec : appSpec.getFlows().values()) {
      for (FlowletDefinition flowletDef : flowSpec.getFlowlets().values()) {
        String className = flowletDef.getFlowletSpec().getClassName();
        // Walk up class hierachy and put all the parent classes that are Flowlet type into the map
        for (TypeToken<?> type : TypeToken.of(Class.forName(className)).getTypes().classes()) {
          if (Flowlet.class.isAssignableFrom(type.getRawType()) && !type.getRawType().equals(AbstractFlowlet.class)) {
            flowletClassNames.put(type.getRawType().getName(), flowSpec.getName());
          }
        }
      }
    }

    // Find all procedure classes
    Set<String> procedureClassNames = Sets.newHashSet();
    for (ProcedureSpecification procedureSpec : appSpec.getProcedures().values()) {
      String className = procedureSpec.getClassName();
      for (TypeToken<?> type : TypeToken.of(Class.forName(className)).getTypes().classes()) {
        if (Procedure.class.isAssignableFrom(type.getRawType()) && !type.getRawType().equals(AbstractProcedure.class)) {
          procedureClassNames.add(type.getRawType().getName());
        }
      }
    }

    FlowletRewriter flowletRewriter = new FlowletRewriter(appSpec.getName(), false);
    FlowletRewriter generatorRewriter = new FlowletRewriter(appSpec.getName(), true);
    ProcedureRewriter procedureRewriter = new ProcedureRewriter(appSpec.getName());

    URI basePath = relativeBase.toURI();
    while (!queue.isEmpty()) {
      File file = queue.remove();
      String entryName = basePath.relativize(file.toURI()).toString();
      jarOut.putNextEntry(new JarEntry(entryName));

      if (file.isFile()) {
        InputStream is = new FileInputStream(file);
        try {
          byte[] bytes = ByteStreams.toByteArray(is);
          String className = pathToClassName(entryName);
          if (flowletClassNames.containsKey(className)) {
            if (GeneratorFlowlet.class.isAssignableFrom(Class.forName(className))) {
              jarOut.write(generatorRewriter.generate(bytes, flowletClassNames.get(className)));
            } else {
              jarOut.write(flowletRewriter.generate(bytes, flowletClassNames.get(className)));
            }
          } else if (procedureClassNames.contains(className)) {
            jarOut.write(procedureRewriter.generate(bytes));
          } else {
            jarOut.write(bytes);
          }
        } finally {
          is.close();
        }
      } else {
        Collections.addAll(queue, file.listFiles());
      }
      jarOut.closeEntry();
    }

    jarOut.close();

    return outputFile;
  }
  private static String pathToClassName(String path) {
    return path.replace('/', '.').substring(0, path.length() - ".class".length());
  }
}
