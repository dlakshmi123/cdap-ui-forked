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

package co.cask.cdap.explore.service;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hive.service.auth.HiveAuthFactory;
import org.apache.tez.dag.api.TezConfiguration;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 */
public class ExploreServiceUtilsTest {

  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void testHiveVersion() throws Exception {
    // This would throw an exception if it didn't pass
    ExploreServiceUtils.checkHiveSupport(getClass().getClassLoader());
  }

  @Test
  public void hijackConfFileTest() throws Exception {
    Configuration conf = new Configuration(false);
    conf.set("foo", "bar");
    Assert.assertEquals(1, conf.size());

    File tempDir = tmpFolder.newFolder();

    File confFile = tmpFolder.newFile("hive-site.xml");

    try (FileOutputStream os = new FileOutputStream(confFile)) {
      conf.writeXml(os);
    }

    Set<File> cdapJars = new LinkedHashSet<>();
    cdapJars.add(new File(tmpFolder.newFolder(), "cdap-common-3.5.1-SNAPSHOT.jar"));
    cdapJars.add(new File(tmpFolder.newFolder(), "cdap-app-fabric-3.5.1-SNAPSHOT.jar"));

    File newConfFile = ExploreServiceUtils.updateConfFileForExplore(confFile, tempDir, cdapJars);

    conf = new Configuration(false);
    conf.addResource(newConfFile.toURI().toURL());

    Assert.assertEquals(3, conf.size());
    Assert.assertEquals("false", conf.get(Job.MAPREDUCE_JOB_USER_CLASSPATH_FIRST));
    Assert.assertEquals("false", conf.get(Job.MAPREDUCE_JOB_CLASSLOADER));
    Assert.assertEquals("bar", conf.get("foo"));

    // check yarn-site changes
    confFile = tmpFolder.newFile("yarn-site.xml");
    conf = new YarnConfiguration();

    try (FileOutputStream os = new FileOutputStream(confFile)) {
      conf.writeXml(os);
    }

    String yarnApplicationClassPath = "$PWD/cdap-common-3.5.1-SNAPSHOT.jar,$PWD/cdap-app-fabric-3.5.1-SNAPSHOT.jar," +
      "$PWD/*," + conf.get(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                           Joiner.on(",").join(YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH));

    newConfFile = ExploreServiceUtils.updateConfFileForExplore(confFile, tempDir, cdapJars);

    conf = new Configuration(false);
    conf.addResource(newConfFile.toURI().toURL());

    Assert.assertEquals(yarnApplicationClassPath, conf.get(YarnConfiguration.YARN_APPLICATION_CLASSPATH));

    // check mapred-site changes
    confFile = tmpFolder.newFile("mapred-site.xml");
    conf = new YarnConfiguration();

    try (FileOutputStream os = new FileOutputStream(confFile)) {
      conf.writeXml(os);
    }

    String mapredApplicationClassPath = "$PWD/cdap-common-3.5.1-SNAPSHOT.jar,$PWD/cdap-app-fabric-3.5.1-SNAPSHOT.jar,"
      + "$PWD/*," + conf.get(MRJobConfig.MAPREDUCE_APPLICATION_CLASSPATH,
                             MRJobConfig.DEFAULT_MAPREDUCE_APPLICATION_CLASSPATH);


    newConfFile = ExploreServiceUtils.updateConfFileForExplore(confFile, tempDir, cdapJars);

    conf = new Configuration(false);
    conf.addResource(newConfFile.toURI().toURL());

    Assert.assertEquals(mapredApplicationClassPath, conf.get(MRJobConfig.MAPREDUCE_APPLICATION_CLASSPATH));

    // check tez-site changes
    confFile = tmpFolder.newFile("tez-site.xml");
    conf = new TezConfiguration();
    conf.set(TezConfiguration.TEZ_CLUSTER_ADDITIONAL_CLASSPATH_PREFIX, "somepath:anotherpath");

    try (FileOutputStream os = new FileOutputStream(confFile)) {
      conf.writeXml(os);
    }

    String tezClassPath = "$PWD/cdap-common-3.5.1-SNAPSHOT.jar:$PWD/cdap-app-fabric-3.5.1-SNAPSHOT.jar:"
      + conf.get(TezConfiguration.TEZ_CLUSTER_ADDITIONAL_CLASSPATH_PREFIX);


    newConfFile = ExploreServiceUtils.updateConfFileForExplore(confFile, tempDir, cdapJars);

    conf = new Configuration(false);
    conf.addResource(newConfFile.toURI().toURL());

    Assert.assertEquals(tezClassPath, conf.get(TezConfiguration.TEZ_CLUSTER_ADDITIONAL_CLASSPATH_PREFIX));

    // Ensure conf files that are not hive-site.xml/mapred-site.xml/yarn-site.xml are unchanged
    confFile = tmpFolder.newFile("core-site.xml");
    Assert.assertEquals(confFile, ExploreServiceUtils.updateConfFileForExplore(confFile, tempDir, cdapJars));
  }

  @Test
  public void testRewriteHiveAuthFactory() throws Exception {
    URL hiveAuthURL = getClass().getClassLoader().getResource(
      HiveAuthFactory.class.getName().replace('.', '/') + ".class");

    String hiveJarFilePath = hiveAuthURL.getPath();
    File hiveJarFile = new File(URI.create(hiveJarFilePath.substring(0, hiveJarFilePath.indexOf("!/"))));

    File targetJar = ExploreServiceUtils.rewriteHiveAuthFactory(hiveJarFile,
                                                                new File(tmpFolder.newFolder(), "hive.jar"));

    try (
      TestHiveAuthFactoryClassLoader cl = new TestHiveAuthFactoryClassLoader(targetJar, getClass().getClassLoader())
    ) {
      Class<?> hiveAuthFactoryClass = cl.loadClass(HiveAuthFactory.class.getName());
      Method loginFromKeytab = hiveAuthFactoryClass.getMethod("loginFromKeytab", HiveConf.class);

      // If the write is not successful, this will throw exception
      loginFromKeytab.invoke(null, new Object[] { null });
    }
  }

  /**
   * A class loader which tries to load from the given jarFile first rather than the parent.
   */
  private static final class TestHiveAuthFactoryClassLoader extends ClassLoader implements Closeable {

    private final JarFile jarFile;

    public TestHiveAuthFactoryClassLoader(File jarFile, ClassLoader parent) throws IOException {
      super(parent);
      this.jarFile = new JarFile(jarFile);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      return loadClass(name, true);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      // Try to load from itself first
      String fileName = name.replace('.', '/') + ".class";
      JarEntry jarEntry = jarFile.getJarEntry(fileName);
      if (jarEntry == null) {
        return super.loadClass(name, resolve);
      }

      try (InputStream input = jarFile.getInputStream(jarEntry)) {
        byte[] bytes = ByteStreams.toByteArray(input);
        Class<?> cls = defineClass(name, bytes, 0, bytes.length);

        if (resolve) {
          resolveClass(cls);
        }
        return cls;

      } catch (IOException e) {
        throw new ClassNotFoundException("Class " + name + " not found.", e);
      }
    }

    @Override
    public void close() throws IOException {
      jarFile.close();
    }
  }
}
