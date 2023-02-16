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

package co.cask.cdap.internal.app.services.http.handlers;

import co.cask.cdap.ConfigTestApp;
import co.cask.cdap.WordCountApp;
import co.cask.cdap.api.artifact.ApplicationClass;
import co.cask.cdap.api.artifact.ArtifactClasses;
import co.cask.cdap.api.artifact.ArtifactScope;
import co.cask.cdap.api.artifact.ArtifactVersion;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.plugin.PluginPropertyField;
import co.cask.cdap.app.program.ManifestFields;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.io.Locations;
import co.cask.cdap.gateway.handlers.ArtifactHttpHandler;
import co.cask.cdap.internal.app.runtime.artifact.ArtifactRepository;
import co.cask.cdap.internal.app.runtime.artifact.plugin.Plugin1;
import co.cask.cdap.internal.app.runtime.artifact.plugin.Plugin2;
import co.cask.cdap.internal.app.services.http.AppFabricTestBase;
import co.cask.cdap.internal.io.ReflectionSchemaGenerator;
import co.cask.cdap.internal.io.SchemaTypeAdapter;
import co.cask.cdap.internal.test.AppJarHelper;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.artifact.ArtifactInfo;
import co.cask.cdap.proto.artifact.ArtifactRange;
import co.cask.cdap.proto.artifact.ArtifactSummary;
import co.cask.cdap.proto.artifact.PluginInfo;
import co.cask.cdap.proto.artifact.PluginSummary;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import javax.ws.rs.HttpMethod;

/**
 * Tests for {@link ArtifactHttpHandler}
 */
public class ArtifactHttpHandlerTest extends AppFabricTestBase {
  private static final ReflectionSchemaGenerator schemaGenerator = new ReflectionSchemaGenerator(false);
  private static final Type ARTIFACTS_TYPE = new TypeToken<Set<ArtifactSummary>>() { }.getType();
  private static final Type PLUGIN_SUMMARIES_TYPE = new TypeToken<Set<PluginSummary>>() { }.getType();
  private static final Type PLUGIN_INFOS_TYPE = new TypeToken<Set<PluginInfo>>() { }.getType();
  private static final Type PLUGIN_TYPES_TYPE = new TypeToken<Set<String>>() { }.getType();
  private static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(Schema.class, new SchemaTypeAdapter())
    .create();
  private static ArtifactRepository artifactRepository;
  private static LocationFactory locationFactory;

  @BeforeClass
  public static void setup() throws IOException {
    artifactRepository = getInjector().getInstance(ArtifactRepository.class);
    locationFactory = new LocalLocationFactory(tmpFolder.newFolder());
  }

  @After
  public void wipeData() throws IOException {
    artifactRepository.clear(Id.Namespace.DEFAULT);
    artifactRepository.clear(Id.Namespace.SYSTEM);
  }

  // test deploying an application artifact that has a non-application as its main class
  @Test
  public void testAddBadApp() throws Exception {
    Id.Artifact artifactId = Id.Artifact.from(Id.Namespace.DEFAULT, "wordcount", "1.0.0");
    Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.getCode(),
                        addAppArtifact(artifactId, ArtifactSummary.class).getStatusLine().getStatusCode());
  }

  @Test
  public void testNotFound() throws IOException, URISyntaxException {
    Assert.assertTrue(getArtifacts(Id.Namespace.DEFAULT).isEmpty());
    Assert.assertNull(getArtifacts(Id.Namespace.DEFAULT, "wordcount"));
    Assert.assertNull(getArtifact(Id.Artifact.from(Id.Namespace.DEFAULT, "wordcount", "1.0.0")));
  }

  @Test
  public void testAddAndGet() throws Exception {
    // add 2 versions of the same app that doesn't use config
    Id.Artifact wordcountId1 = Id.Artifact.from(Id.Namespace.DEFAULT, "wordcount", "1.0.0");
    Id.Artifact wordcountId2 = Id.Artifact.from(Id.Namespace.DEFAULT, "wordcount", "2.0.0");
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                        addAppArtifact(wordcountId1, WordCountApp.class).getStatusLine().getStatusCode());
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                        addAppArtifact(wordcountId2, WordCountApp.class).getStatusLine().getStatusCode());
    // and 1 version of another app that uses a config
    Id.Artifact configTestAppId = Id.Artifact.from(Id.Namespace.DEFAULT, "cfgtest", "1.0.0");
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                        addAppArtifact(configTestAppId, ConfigTestApp.class).getStatusLine().getStatusCode());

    // test get /artifacts endpoint
    Set<ArtifactSummary> expectedArtifacts = Sets.newHashSet(
      new ArtifactSummary("wordcount", "1.0.0"),
      new ArtifactSummary("wordcount", "2.0.0"),
      new ArtifactSummary("cfgtest", "1.0.0")
    );
    Set<ArtifactSummary> actualArtifacts = getArtifacts(Id.Namespace.DEFAULT);
    Assert.assertEquals(expectedArtifacts, actualArtifacts);

    // test get /artifacts/wordcount endpoint
    expectedArtifacts = Sets.newHashSet(
      new ArtifactSummary("wordcount", "1.0.0"),
      new ArtifactSummary("wordcount", "2.0.0")
    );
    actualArtifacts = getArtifacts(Id.Namespace.DEFAULT, "wordcount");
    Assert.assertEquals(expectedArtifacts, actualArtifacts);

    // test get /artifacts/cfgtest/versions/1.0.0 endpoint
    Schema appConfigSchema = schemaGenerator.generate(ConfigTestApp.ConfigClass.class);
    ArtifactClasses classes = ArtifactClasses.builder()
      .addApp(new ApplicationClass(ConfigTestApp.class.getName(), "", appConfigSchema))
      .build();
    ArtifactInfo expectedInfo = new ArtifactInfo("cfgtest", "1.0.0", ArtifactScope.USER,
                                                 classes, ImmutableMap.<String, String>of());
    ArtifactInfo actualInfo = getArtifact(configTestAppId);
    Assert.assertEquals(expectedInfo, actualInfo);
  }

  @Test
  public void testSystemArtifacts() throws Exception {
    // add the app in the default namespace
    Id.Artifact defaultId = Id.Artifact.from(Id.Namespace.DEFAULT, "wordcount", "1.0.0");
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                        addAppArtifact(defaultId, WordCountApp.class).getStatusLine().getStatusCode());
    // add a system artifact. currently can't do this through the rest api (by design)
    // so bypass it and use the repository directly
    Id.Artifact systemId = Id.Artifact.from(Id.Namespace.SYSTEM, "wordcount", "1.0.0");
    File systemArtifact = buildAppArtifact(WordCountApp.class, "wordcount-1.0.0.jar");
    artifactRepository.addArtifact(systemId, systemArtifact, Sets.<ArtifactRange>newHashSet());

    // test get /artifacts
    Set<ArtifactSummary> expectedArtifacts = Sets.newHashSet(
      new ArtifactSummary("wordcount", "1.0.0", ArtifactScope.USER),
      new ArtifactSummary("wordcount", "1.0.0", ArtifactScope.SYSTEM)
    );
    Set<ArtifactSummary> actualArtifacts = getArtifacts(Id.Namespace.DEFAULT);
    Assert.assertEquals(expectedArtifacts, actualArtifacts);

    // test get /artifacts?scope=system
    expectedArtifacts = Sets.newHashSet(
      new ArtifactSummary("wordcount", "1.0.0", ArtifactScope.SYSTEM)
    );
    actualArtifacts = getArtifacts(Id.Namespace.DEFAULT, ArtifactScope.SYSTEM);
    Assert.assertEquals(expectedArtifacts, actualArtifacts);

    // test get /artifacts?scope=user
    expectedArtifacts = Sets.newHashSet(
      new ArtifactSummary("wordcount", "1.0.0", ArtifactScope.USER)
    );
    actualArtifacts = getArtifacts(Id.Namespace.DEFAULT, ArtifactScope.USER);
    Assert.assertEquals(expectedArtifacts, actualArtifacts);

    // test get /artifacts/wordcount?scope=user
    expectedArtifacts = Sets.newHashSet(
      new ArtifactSummary("wordcount", "1.0.0", ArtifactScope.USER)
    );
    actualArtifacts = getArtifacts(Id.Namespace.DEFAULT, "wordcount", ArtifactScope.USER);
    Assert.assertEquals(expectedArtifacts, actualArtifacts);

    // test get /artifacts/wordcount?scope=system
    expectedArtifacts = Sets.newHashSet(
      new ArtifactSummary("wordcount", "1.0.0", ArtifactScope.SYSTEM)
    );
    actualArtifacts = getArtifacts(Id.Namespace.DEFAULT, "wordcount", ArtifactScope.SYSTEM);
    Assert.assertEquals(expectedArtifacts, actualArtifacts);

    // test get /artifacts/wordcount/versions/1.0.0?scope=user
    ArtifactClasses classes = ArtifactClasses.builder()
      .addApp(new ApplicationClass(WordCountApp.class.getName(), "", null))
      .build();
    ArtifactInfo expectedInfo = new ArtifactInfo("wordcount", "1.0.0", ArtifactScope.USER,
                                                 classes, ImmutableMap.<String, String>of());
    ArtifactInfo actualInfo = getArtifact(defaultId, ArtifactScope.USER);
    Assert.assertEquals(expectedInfo, actualInfo);

    // test get /artifacts/wordcount/versions/1.0.0?scope=system
    expectedInfo = new ArtifactInfo("wordcount", "1.0.0", ArtifactScope.SYSTEM,
                                    classes, ImmutableMap.<String, String>of());
    actualInfo = getArtifact(defaultId, ArtifactScope.SYSTEM);
    Assert.assertEquals(expectedInfo, actualInfo);
  }

  @Test
  public void testPluginNamespaceIsolation() throws Exception {
    // add a system artifact. currently can't do this through the rest api (by design)
    // so bypass it and use the repository directly
    Id.Artifact systemId = Id.Artifact.from(Id.Namespace.SYSTEM, "wordcount", "1.0.0");
    File systemArtifact = buildAppArtifact(WordCountApp.class, "wordcount-1.0.0.jar");
    artifactRepository.addArtifact(systemId, systemArtifact, Sets.<ArtifactRange>newHashSet());

    Set<ArtifactRange> parents = Sets.newHashSet(new ArtifactRange(
      systemId.getNamespace(), systemId.getName(), systemId.getVersion(), true, systemId.getVersion(), true));

    Id.Namespace namespace1 = Id.Namespace.from("ns1");
    Id.Namespace namespace2 = Id.Namespace.from("ns2");
    createNamespace(namespace1.getId());
    createNamespace(namespace2.getId());

    try {
      // add some plugins in namespace1. Will contain Plugin1 and Plugin2
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(ManifestFields.EXPORT_PACKAGE, Plugin1.class.getPackage().getName());
      Id.Artifact pluginsId1 = Id.Artifact.from(namespace1, "plugins1", "1.0.0");
      Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                          addPluginArtifact(pluginsId1, Plugin1.class, manifest, parents)
                            .getStatusLine().getStatusCode());

      // add some plugins in namespace2. Will contain Plugin1 and Plugin2
      manifest = new Manifest();
      manifest.getMainAttributes().put(ManifestFields.EXPORT_PACKAGE, Plugin1.class.getPackage().getName());
      Id.Artifact pluginsId2 = Id.Artifact.from(namespace2, "plugins2", "1.0.0");
      Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                          addPluginArtifact(pluginsId2, Plugin1.class, manifest, parents)
                            .getStatusLine().getStatusCode());

      ArtifactSummary artifact1 =
        new ArtifactSummary(pluginsId1.getName(), pluginsId1.getVersion().getVersion(), ArtifactScope.USER);
      ArtifactSummary artifact2 =
        new ArtifactSummary(pluginsId2.getName(), pluginsId2.getVersion().getVersion(), ArtifactScope.USER);

      PluginSummary summary1Namespace1 =
        new PluginSummary("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), artifact1);
      PluginSummary summary2Namespace1 =
        new PluginSummary("Plugin2", "callable", "Just returns the configured integer",
                          Plugin2.class.getName(), artifact1);
      PluginSummary summary1Namespace2 =
        new PluginSummary("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), artifact2);
      PluginSummary summary2Namespace2 =
        new PluginSummary("Plugin2", "callable", "Just returns the configured integer",
                          Plugin2.class.getName(), artifact2);

      PluginInfo info1Namespace1 =
        new PluginInfo("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), artifact1,
                       ImmutableMap.of(
                         "x", new PluginPropertyField("x", "", "int", true),
                         "stuff", new PluginPropertyField("stuff", "", "string", true)
                       ));
      PluginInfo info2Namespace1 =
        new PluginInfo("Plugin2", "callable", "Just returns the configured integer",
                       Plugin2.class.getName(), artifact1,
                       ImmutableMap.of(
                         "v", new PluginPropertyField("v", "value to return when called", "int", true)
                       ));
      PluginInfo info1Namespace2 =
        new PluginInfo("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), artifact2,
                       ImmutableMap.of(
                         "x", new PluginPropertyField("x", "", "int", true),
                         "stuff", new PluginPropertyField("stuff", "", "string", true)
                       ));
      PluginInfo info2Namespace2 =
        new PluginInfo("Plugin2", "callable", "Just returns the configured integer",
                       Plugin2.class.getName(), artifact2,
                       ImmutableMap.of(
                         "v", new PluginPropertyField("v", "value to return when called", "int", true)
                       ));

      Id.Artifact namespace1Artifact = Id.Artifact.from(namespace1, systemId.getName(), systemId.getVersion());
      Id.Artifact namespace2Artifact = Id.Artifact.from(namespace2, systemId.getName(), systemId.getVersion());

      // should see same types in both namespaces
      Assert.assertEquals(ImmutableSet.of("dummy", "callable"),
                          getPluginTypes(namespace1Artifact, ArtifactScope.SYSTEM));
      Assert.assertEquals(ImmutableSet.of("dummy", "callable"),
                          getPluginTypes(namespace2Artifact, ArtifactScope.SYSTEM));

      // should see that plugins in namespace1 come only from the namespace1 artifact
      Assert.assertEquals(ImmutableSet.of(summary1Namespace1),
                          getPluginSummaries(namespace1Artifact, "dummy", ArtifactScope.SYSTEM));
      Assert.assertEquals(ImmutableSet.of(summary2Namespace1),
                          getPluginSummaries(namespace1Artifact, "callable", ArtifactScope.SYSTEM));

      Assert.assertEquals(ImmutableSet.of(info1Namespace1),
                          getPluginInfos(namespace1Artifact, "dummy", "Plugin1", ArtifactScope.SYSTEM));
      Assert.assertEquals(ImmutableSet.of(info2Namespace1),
                          getPluginInfos(namespace1Artifact, "callable", "Plugin2", ArtifactScope.SYSTEM));

      // should see that plugins in namespace2 come only from the namespace2 artifact
      Assert.assertEquals(ImmutableSet.of(summary1Namespace2),
                          getPluginSummaries(namespace2Artifact, "dummy", ArtifactScope.SYSTEM));
      Assert.assertEquals(ImmutableSet.of(summary2Namespace2),
                          getPluginSummaries(namespace2Artifact, "callable", ArtifactScope.SYSTEM));

      Assert.assertEquals(ImmutableSet.of(info1Namespace2),
                          getPluginInfos(namespace2Artifact, "dummy", "Plugin1", ArtifactScope.SYSTEM));
      Assert.assertEquals(ImmutableSet.of(info2Namespace2),
                          getPluginInfos(namespace2Artifact, "callable", "Plugin2", ArtifactScope.SYSTEM));
    } finally {
      deleteNamespace("iso1");
      deleteNamespace("iso2");
    }
  }

  @Test
  public void testGetPlugins() throws Exception {
    // add an app for plugins to extend
    Id.Artifact wordCount1Id = Id.Artifact.from(Id.Namespace.DEFAULT, "wordcount", "1.0.0");
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                        addAppArtifact(wordCount1Id, WordCountApp.class).getStatusLine().getStatusCode());
    Id.Artifact wordCount2Id = Id.Artifact.from(Id.Namespace.DEFAULT, "wordcount", "2.0.0");
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
                        addAppArtifact(wordCount2Id, WordCountApp.class).getStatusLine().getStatusCode());

    // add some plugins.
    // plugins-1.0.0 extends wordcount[1.0.0,2.0.0)
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(ManifestFields.EXPORT_PACKAGE, Plugin1.class.getPackage().getName());
    Id.Artifact pluginsId1 = Id.Artifact.from(Id.Namespace.DEFAULT, "plugins", "1.0.0");
    Set<ArtifactRange> plugins1Parents = Sets.newHashSet(new ArtifactRange(
      Id.Namespace.DEFAULT, "wordcount", new ArtifactVersion("1.0.0"), new ArtifactVersion("2.0.0")));
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
      addPluginArtifact(pluginsId1, Plugin1.class, manifest, plugins1Parents).getStatusLine().getStatusCode());

    // plugin-2.0.0 extends wordcount[1.0.0,3.0.0)
    Id.Artifact pluginsId2 = Id.Artifact.from(Id.Namespace.DEFAULT, "plugins", "2.0.0");
    Set<ArtifactRange> plugins2Parents = Sets.newHashSet(new ArtifactRange(
      Id.Namespace.DEFAULT, "wordcount", new ArtifactVersion("1.0.0"), new ArtifactVersion("3.0.0")));
    Assert.assertEquals(HttpResponseStatus.OK.getCode(),
      addPluginArtifact(pluginsId2, Plugin1.class, manifest, plugins2Parents).getStatusLine().getStatusCode());

    ArtifactSummary plugins1Artifact = new ArtifactSummary("plugins", "1.0.0");
    ArtifactSummary plugins2Artifact = new ArtifactSummary("plugins", "2.0.0");

    // get plugin types, should be the same for both
    Set<String> expectedTypes = Sets.newHashSet("dummy", "callable");
    Set<String> actualTypes = getPluginTypes(wordCount1Id);
    Assert.assertEquals(expectedTypes, actualTypes);
    actualTypes = getPluginTypes(wordCount2Id);
    Assert.assertEquals(expectedTypes, actualTypes);

    // get plugin summaries. wordcount1 should see plugins from both plugin artifacts
    Set<PluginSummary> expectedSummaries = Sets.newHashSet(
      new PluginSummary("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), plugins1Artifact),
      new PluginSummary("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), plugins2Artifact)
    );
    Set<PluginSummary> actualSummaries = getPluginSummaries(wordCount1Id, "dummy");
    Assert.assertEquals(expectedSummaries, actualSummaries);

    expectedSummaries = Sets.newHashSet(
      new PluginSummary(
        "Plugin2", "callable", "Just returns the configured integer", Plugin2.class.getName(), plugins1Artifact),
      new PluginSummary(
        "Plugin2", "callable", "Just returns the configured integer", Plugin2.class.getName(), plugins2Artifact)
    );
    actualSummaries = getPluginSummaries(wordCount1Id, "callable");
    Assert.assertEquals(expectedSummaries, actualSummaries);

    // wordcount2 should only see plugins from plugins2 artifact
    expectedSummaries = Sets.newHashSet(
      new PluginSummary("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), plugins2Artifact)
    );
    actualSummaries = getPluginSummaries(wordCount2Id, "dummy");
    Assert.assertEquals(expectedSummaries, actualSummaries);

    expectedSummaries = Sets.newHashSet(
      new PluginSummary(
        "Plugin2", "callable", "Just returns the configured integer", Plugin2.class.getName(), plugins2Artifact)
    );
    actualSummaries = getPluginSummaries(wordCount2Id, "callable");
    Assert.assertEquals(expectedSummaries, actualSummaries);

    // get plugin info. Again, wordcount1 should see plugins from both artifacts
    Map<String, PluginPropertyField> p1Properties = ImmutableMap.of(
      "x", new PluginPropertyField("x", "", "int", true),
      "stuff", new PluginPropertyField("stuff", "", "string", true)
    );
    Map<String, PluginPropertyField> p2Properties = ImmutableMap.of(
      "v", new PluginPropertyField("v", "value to return when called", "int", true)
    );

    Set<PluginInfo> expectedInfos = Sets.newHashSet(
      new PluginInfo("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), plugins1Artifact, p1Properties),
      new PluginInfo("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), plugins2Artifact, p1Properties)
    );
    Assert.assertEquals(expectedInfos, getPluginInfos(wordCount1Id, "dummy", "Plugin1"));

    expectedInfos = Sets.newHashSet(
      new PluginInfo("Plugin2", "callable", "Just returns the configured integer",
                     Plugin2.class.getName(), plugins1Artifact, p2Properties),
      new PluginInfo("Plugin2", "callable", "Just returns the configured integer",
                     Plugin2.class.getName(), plugins2Artifact, p2Properties)
    );
    Assert.assertEquals(expectedInfos, getPluginInfos(wordCount1Id, "callable", "Plugin2"));

    // while wordcount2 should only see plugins from plugins2 artifact
    expectedInfos = Sets.newHashSet(
      new PluginInfo("Plugin1", "dummy", "This is plugin1", Plugin1.class.getName(), plugins2Artifact, p1Properties)
    );
    Assert.assertEquals(expectedInfos, getPluginInfos(wordCount2Id, "dummy", "Plugin1"));

    expectedInfos = Sets.newHashSet(
      new PluginInfo("Plugin2", "callable", "Just returns the configured integer",
        Plugin2.class.getName(), plugins2Artifact, p2Properties)
    );
    Assert.assertEquals(expectedInfos, getPluginInfos(wordCount2Id, "callable", "Plugin2"));
  }

  private File buildAppArtifact(Class cls, String name) throws IOException {
    Location appJar = AppJarHelper.createDeploymentJar(locationFactory, cls, new Manifest());
    File destination = new File(tmpFolder.newFolder(), name);
    Files.copy(Locations.newInputSupplier(appJar), destination);
    return destination;
  }

  private Set<ArtifactSummary> getArtifacts(Id.Namespace namespace) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format(
      "%s/namespaces/%s/artifacts", Constants.Gateway.API_VERSION_3, namespace.getId())).toURL();
    return getResults(endpoint, ARTIFACTS_TYPE);
  }

  private Set<ArtifactSummary> getArtifacts(Id.Namespace namespace, ArtifactScope scope)
    throws URISyntaxException, IOException {

    if (scope == null) {
      return getArtifacts(namespace);
    }
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts?scope=%s",
      Constants.Gateway.API_VERSION_3, namespace.getId(), scope.name())).toURL();
    return getResults(endpoint, ARTIFACTS_TYPE);
  }

  private Set<ArtifactSummary> getArtifacts(Id.Namespace namespace, String name)
    throws URISyntaxException, IOException {

    URL endpoint = getEndPoint(String.format(
      "%s/namespaces/%s/artifacts/%s", Constants.Gateway.API_VERSION_3, namespace.getId(), name)).toURL();
    return getResults(endpoint, ARTIFACTS_TYPE);
  }

  private Set<ArtifactSummary> getArtifacts(Id.Namespace namespace, String name, ArtifactScope scope)
    throws URISyntaxException, IOException {

    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s?scope=%s",
      Constants.Gateway.API_VERSION_3, namespace.getId(), name, scope.name())).toURL();
    return getResults(endpoint, ARTIFACTS_TYPE);
  }

  // get /artifacts/{name}/versions/{version}
  private ArtifactInfo getArtifact(Id.Artifact artifactId) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s/versions/%s",
      Constants.Gateway.API_VERSION_3, artifactId.getNamespace().getId(),
      artifactId.getName(), artifactId.getVersion().getVersion()))
      .toURL();

    return getResults(endpoint, ArtifactInfo.class);
  }

  // get /artifacts/{name}/versions/{version}?scope={scope}
  private ArtifactInfo getArtifact(Id.Artifact artifactId, ArtifactScope scope) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s/versions/%s?scope=%s",
      Constants.Gateway.API_VERSION_3, artifactId.getNamespace().getId(),
      artifactId.getName(), artifactId.getVersion().getVersion(), scope.name()))
      .toURL();

    return getResults(endpoint, ArtifactInfo.class);
  }

  // get /artifacts/{name}/versions/{version}/extensions
  private Set<String> getPluginTypes(Id.Artifact artifactId) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s/versions/%s/extensions",
                                             Constants.Gateway.API_VERSION_3,
                                             artifactId.getNamespace().getId(),
                                             artifactId.getName(),
                                             artifactId.getVersion().getVersion()))
      .toURL();

    return getResults(endpoint, PLUGIN_TYPES_TYPE);
  }

  // get /artifacts/{name}/versions/{version}/extensions?scope={scope}
  private Set<String> getPluginTypes(Id.Artifact artifactId,
                                     ArtifactScope scope) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s/versions/%s/extensions?scope=%s",
                                             Constants.Gateway.API_VERSION_3,
                                             artifactId.getNamespace().getId(),
                                             artifactId.getName(),
                                             artifactId.getVersion().getVersion(),
                                             scope.name())).toURL();

    return getResults(endpoint, PLUGIN_TYPES_TYPE);
  }

  // get /artifacts/{name}/versions/{version}/extensions/{plugin-type}
  private Set<PluginSummary> getPluginSummaries(Id.Artifact artifactId,
                                                String pluginType) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s/versions/%s/extensions/%s",
                                             Constants.Gateway.API_VERSION_3,
                                             artifactId.getNamespace().getId(),
                                             artifactId.getName(),
                                             artifactId.getVersion().getVersion(),
                                             pluginType))
      .toURL();

    return getResults(endpoint, PLUGIN_SUMMARIES_TYPE);
  }

  // get /artifacts/{name}/versions/{version}/extensions/{plugin-type}?scope={scope}
  private Set<PluginSummary> getPluginSummaries(Id.Artifact artifactId,
                                                String pluginType,
                                                ArtifactScope scope) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s/versions/%s/extensions/%s?scope=%s",
                                             Constants.Gateway.API_VERSION_3,
                                             artifactId.getNamespace().getId(),
                                             artifactId.getName(),
                                             artifactId.getVersion().getVersion(),
                                             pluginType,
                                             scope.name()))
      .toURL();

    return getResults(endpoint, PLUGIN_SUMMARIES_TYPE);
  }

  // get /artifacts/{name}/versions/{version}/extensions/{plugin-type}/plugins/{plugin-name}
  private Set<PluginInfo> getPluginInfos(Id.Artifact artifactId,
                                         String pluginType, String pluginName) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(String.format("%s/namespaces/%s/artifacts/%s/versions/%s/extensions/%s/plugins/%s",
      Constants.Gateway.API_VERSION_3, artifactId.getNamespace().getId(),
      artifactId.getName(), artifactId.getVersion().getVersion(), pluginType, pluginName))
      .toURL();

    return getResults(endpoint, PLUGIN_INFOS_TYPE);
  }

  // get /artifacts/{name}/versions/{version}/extensions/{plugin-type}/plugins/{plugin-name}?scope={scope}
  private Set<PluginInfo> getPluginInfos(Id.Artifact artifactId,
                                         String pluginType,
                                         String pluginName,
                                         ArtifactScope scope) throws URISyntaxException, IOException {
    URL endpoint = getEndPoint(
      String.format("%s/namespaces/%s/artifacts/%s/versions/%s/extensions/%s/plugins/%s?scope=%s",
                    Constants.Gateway.API_VERSION_3,
                    artifactId.getNamespace().getId(),
                    artifactId.getName(),
                    artifactId.getVersion().getVersion(),
                    pluginType,
                    pluginName,
                    scope.name()))
      .toURL();

    return getResults(endpoint, PLUGIN_INFOS_TYPE);
  }

  // gets the contents from doing a get on the given url as the given type, or null if a 404 is returned
  private <T> T getResults(URL endpoint, Type type) throws IOException {
    HttpURLConnection urlConn = (HttpURLConnection) endpoint.openConnection();
    urlConn.setRequestMethod(HttpMethod.GET);

    int responseCode = urlConn.getResponseCode();
    if (responseCode == HttpResponseStatus.NOT_FOUND.getCode()) {
      return null;
    }
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), responseCode);

    String responseStr = new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    urlConn.disconnect();
    return GSON.fromJson(responseStr, type);
  }
}
