package com.continuuity.explore.guice;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.runtime.RuntimeModule;
import com.continuuity.data2.datafabric.dataset.client.DatasetServiceClient;
import com.continuuity.data2.util.hbase.HBaseTableUtilFactory;
import com.continuuity.explore.executor.ExploreExecutorHttpHandler;
import com.continuuity.explore.executor.ExploreExecutorService;
import com.continuuity.explore.executor.ExplorePingHandler;
import com.continuuity.explore.executor.QueryExecutorHttpHandler;
import com.continuuity.explore.service.ExploreService;
import com.continuuity.explore.service.hive.Hive12ExploreService;
import com.continuuity.explore.service.hive.Hive13ExploreService;
import com.continuuity.gateway.handlers.PingHandler;
import com.continuuity.hive.datasets.DatasetStorageHandler;
import com.continuuity.http.HttpHandler;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.mapreduce.MRConfig;
import org.apache.twill.internal.utils.Dependencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Guice runtime module for the explore functionality.
 */
public class ExploreRuntimeModule extends RuntimeModule {
  private static final Logger LOG = LoggerFactory.getLogger(ExploreRuntimeModule.class);

  @Override
  public Module getInMemoryModules() {
    return Modules.combine(new ExploreExecutorModule(), new ExploreLocalModule(true));
  }

  @Override
  public Module getSingleNodeModules() {
    return Modules.combine(new ExploreExecutorModule(), new ExploreLocalModule(false));
  }

  @Override
  public Module getDistributedModules() {
    return Modules.combine(new ExploreExecutorModule(), new ExploreDistributedModule());
  }

  private static final class ExploreExecutorModule extends PrivateModule {

    @Override
    protected void configure() {
      Named exploreSeriveName = Names.named(Constants.Service.EXPLORE_HTTP_USER_SERVICE);
      Multibinder<HttpHandler> handlerBinder =
          Multibinder.newSetBinder(binder(), HttpHandler.class, exploreSeriveName);
      handlerBinder.addBinding().to(QueryExecutorHttpHandler.class);
      handlerBinder.addBinding().to(ExploreExecutorHttpHandler.class);
      handlerBinder.addBinding().to(ExplorePingHandler.class);
      handlerBinder.addBinding().to(PingHandler.class);

      bind(ExploreExecutorService.class).in(Scopes.SINGLETON);
      expose(ExploreExecutorService.class);
    }
  }

  private static final class ExploreLocalModule extends PrivateModule {
    private final boolean isInMemory;

    public ExploreLocalModule(boolean isInMemory) {
      this.isInMemory = isInMemory;
    }

    @Override
    protected void configure() {
      // Current version of hive used in Singlenode is Hive 13
      bind(ExploreService.class).annotatedWith(Names.named("explore.service.impl")).to(Hive13ExploreService.class);
      bind(ExploreService.class).toProvider(ExploreServiceProvider.class).in(Scopes.SINGLETON);
      expose(ExploreService.class);

      bind(boolean.class).annotatedWith(Names.named("explore.inmemory")).toInstance(isInMemory);
    }

    @Singleton
    private static final class ExploreServiceProvider implements Provider<ExploreService> {

      private final CConfiguration cConf;
      private final ExploreService exploreService;
      private final boolean isInMemory;

      @Inject
      public ExploreServiceProvider(CConfiguration cConf,
                                    @Named("explore.service.impl") ExploreService exploreService,
                                    @Named("explore.inmemory") boolean isInMemory) {
        this.exploreService = exploreService;
        this.cConf = cConf;
        this.isInMemory = isInMemory;
      }

      private static final long seed = System.currentTimeMillis();
      @Override
      public ExploreService get() {
        File warehouseDir = new File(cConf.get(Constants.Explore.CFG_LOCAL_DATA_DIR), "warehouse");
        File databaseDir = new File(cConf.get(Constants.Explore.CFG_LOCAL_DATA_DIR), "database");

        if (isInMemory) {
          // This seed is required to make all tests pass when launched together, and when several of them
          // start a hive metastore / hive server.
          // TODO try to remove once maven is there - REACTOR-267
          warehouseDir = new File(warehouseDir, Long.toString(seed));
          databaseDir = new File(databaseDir, Long.toString(seed));
        }

        LOG.debug("Setting {} to {}",
                  HiveConf.ConfVars.METASTOREWAREHOUSE.toString(), warehouseDir.getAbsoluteFile());
        System.setProperty(HiveConf.ConfVars.METASTOREWAREHOUSE.toString(), warehouseDir.getAbsolutePath());

        // Set derby log location
        System.setProperty("derby.stream.error.file",
                           cConf.get(Constants.Explore.CFG_LOCAL_DATA_DIR) + File.separator + "derby.log");

        String connectUrl = String.format("jdbc:derby:;databaseName=%s;create=true", databaseDir.getAbsoluteFile());
        LOG.debug("Setting {} to {}", HiveConf.ConfVars.METASTORECONNECTURLKEY.toString(), connectUrl);
        System.setProperty(HiveConf.ConfVars.METASTORECONNECTURLKEY.toString(), connectUrl);

        // Some more local mode settings
        System.setProperty(HiveConf.ConfVars.LOCALMODEAUTO.toString(), "true");
        System.setProperty(HiveConf.ConfVars.SUBMITVIACHILD.toString(), "false");
        System.setProperty(MRConfig.FRAMEWORK_NAME, "local");

        // Disable security
        // TODO: verify if auth=NOSASL is really needed - REACTOR-267
        System.setProperty(HiveConf.ConfVars.HIVE_SERVER2_AUTHENTICATION.toString(), "NOSASL");
        System.setProperty(HiveConf.ConfVars.HIVE_SERVER2_ENABLE_DOAS.toString(), "false");
        System.setProperty(HiveConf.ConfVars.METASTORE_USE_THRIFT_SASL.toString(), "false");

        return exploreService;
      }
    }
  }

  private static final class ExploreDistributedModule extends PrivateModule {
    private static final Logger LOG = LoggerFactory.getLogger(ExploreDistributedModule.class);

    @Override
    protected void configure() {
      try {
        // Current version of hive used in distributed (with loom) is Hive 12
        bind(ExploreService.class).to(Hive12ExploreService.class).in(Scopes.SINGLETON);
        expose(ExploreService.class);

        setupClasspath();

        // Set local tmp dir to an absolute location in the twill runnable otherwise Hive complains
        System.setProperty(HiveConf.ConfVars.LOCALSCRATCHDIR.toString(),
            new File(HiveConf.ConfVars.LOCALSCRATCHDIR.defaultVal).getAbsolutePath());
        LOG.info("Setting {} to {}", HiveConf.ConfVars.LOCALSCRATCHDIR.toString(),
            System.getProperty(HiveConf.ConfVars.LOCALSCRATCHDIR.toString()));
      } catch (Throwable e) {
        throw Throwables.propagate(e);
      }
    }
  }

  private static void setupClasspath() throws IOException {
    // Here we find the transitive dependencies and remove all paths that come from the boot class path -
    // those paths are not needed because the new JVM will have them in its boot class path.
    // It could even be wrong to keep them because in the target container, the boot class path may be different
    // (for example, if Hadoop uses a different Java version than Reactor).

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (String classpath : Splitter.on(File.pathSeparatorChar).split(System.getProperty("sun.boot.class.path"))) {
      File file = new File(classpath);
      builder.add(file.getAbsolutePath());
      try {
        builder.add(file.getCanonicalPath());
      } catch (IOException e) {
        LOG.warn("Could not add canonical path to aux class path for file {}", file.toString(), e);
      }
    }

    Set<String> bootstrapClassPaths = builder.build();

    Set<String> hBaseTableDeps = traceDependencies(new HBaseTableUtilFactory().get().getClass().getCanonicalName(),
                                                   bootstrapClassPaths);

    // Note the order of dependency jars is important so that HBase jars come first in the classpath order
    // LinkedHashSet maintains insertion order while removing duplicate entries.
    Set<String> orderedDependencies = new LinkedHashSet<String>();
    orderedDependencies.addAll(hBaseTableDeps);
    orderedDependencies.addAll(traceDependencies(DatasetServiceClient.class.getCanonicalName(), bootstrapClassPaths));
    orderedDependencies.addAll(traceDependencies(DatasetStorageHandler.class.getCanonicalName(), bootstrapClassPaths));

    System.setProperty(HiveConf.ConfVars.HIVEAUXJARS.toString(), Joiner.on(',').join(orderedDependencies));
    LOG.info("Setting {} to {}", HiveConf.ConfVars.HIVEAUXJARS.toString(),
             System.getProperty(HiveConf.ConfVars.HIVEAUXJARS.toString()));

    // Setup HADOOP_CLASSPATH hack, more info on why this is needed - REACTOR-325
    LocalMapreduceClasspathSetter classpathSetter =
      new LocalMapreduceClasspathSetter(new HiveConf(), System.getProperty("java.io.tmpdir"));
    for (String jar : hBaseTableDeps) {
      classpathSetter.accept(jar);
    }
    classpathSetter.setupClasspathScript();
  }

  private static Set<String> traceDependencies(String className, final Set<String> bootstrapClassPaths)
    throws IOException {
    final Set<String> jarFiles = Sets.newHashSet();

    Dependencies.findClassDependencies(
      ExploreRuntimeModule.class.getClassLoader(),
      new Dependencies.ClassAcceptor() {
        @Override
        public boolean accept(String className, URL classUrl, URL classPathUrl) {
          if (bootstrapClassPaths.contains(classPathUrl.getFile())) {
            return false;
          }

          // Note: the class path entries need to be prefixed with "file://" for the jars to work when
          // Hive starts local map-reduce job.
          jarFiles.add("file://" + classPathUrl.getFile());
          return true;
        }
      },
      className
    );

    return jarFiles;
  }
}
