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

package co.cask.cdap.client;

import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.api.artifact.ArtifactScope;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.plugin.PluginClass;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.util.RESTClient;
import co.cask.cdap.common.ArtifactAlreadyExistsException;
import co.cask.cdap.common.ArtifactNotFoundException;
import co.cask.cdap.common.ArtifactRangeNotFoundException;
import co.cask.cdap.common.BadRequestException;
import co.cask.cdap.common.NotFoundException;
import co.cask.cdap.common.UnauthorizedException;
import co.cask.cdap.internal.io.SchemaTypeAdapter;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.artifact.ApplicationClassInfo;
import co.cask.cdap.proto.artifact.ApplicationClassSummary;
import co.cask.cdap.proto.artifact.ArtifactInfo;
import co.cask.cdap.proto.artifact.ArtifactRange;
import co.cask.cdap.proto.artifact.ArtifactSummary;
import co.cask.cdap.proto.artifact.PluginInfo;
import co.cask.cdap.proto.artifact.PluginSummary;
import co.cask.common.http.HttpMethod;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpResponse;
import co.cask.common.http.ObjectResponse;
import com.google.common.base.Joiner;
import com.google.common.io.InputSupplier;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Provides ways to interact with CDAP Artifacts.
 */
@Beta
public class ArtifactClient {

  private static final Type ARTIFACT_SUMMARIES_TYPE = new TypeToken<List<ArtifactSummary>>() { }.getType();
  private static final Type APPCLASS_SUMMARIES_TYPE = new TypeToken<List<ApplicationClassSummary>>() { }.getType();
  private static final Type APPCLASS_INFOS_TYPE = new TypeToken<List<ApplicationClassInfo>>() { }.getType();
  private static final Type EXTENSIONS_TYPE = new TypeToken<List<String>>() { }.getType();
  private static final Type PLUGIN_SUMMARIES_TYPE = new TypeToken<List<PluginSummary>>() { }.getType();
  private static final Type PLUGIN_INFOS_TYPE = new TypeToken<List<PluginInfo>>() { }.getType();
  private static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(Schema.class, new SchemaTypeAdapter())
    .create();

  private final RESTClient restClient;
  private final ClientConfig config;

  @Inject
  public ArtifactClient(ClientConfig config, RESTClient restClient) {
    this.config = config;
    this.restClient = restClient;
  }

  public ArtifactClient(ClientConfig config) {
    this(config, new RESTClient(config));
  }

  /**
   * Lists all artifacts in the given namespace, including all system artifacts.
   *
   * @param namespace the namespace to list artifacts in
   * @return list of {@link ArtifactSummary}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   * @throws NotFoundException if the namespace could not be found
   */
  public List<ArtifactSummary> list(Id.Namespace namespace)
    throws IOException, UnauthorizedException, NotFoundException {
    return list(namespace, null);
  }

  /**
   * Lists all artifacts in the given namespace, optionally including system artifacts.
   *
   * @param namespace the namespace to list artifacts in
   * @param scope the scope of the artifacts to get. If null, both user and system artifacts are listed
   * @return list of {@link ArtifactSummary}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   * @throws NotFoundException if the namespace could not be found
   */
  public List<ArtifactSummary> list(Id.Namespace namespace, @Nullable ArtifactScope scope)
    throws IOException, UnauthorizedException, NotFoundException {

    URL url = scope == null ? config.resolveNamespacedURLV3(namespace, "artifacts") :
      config.resolveNamespacedURLV3(namespace, String.format("artifacts?scope=%s", scope.name()));
    HttpResponse response =
      restClient.execute(HttpMethod.GET, url, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);

    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new NotFoundException(namespace);
    }
    return ObjectResponse.<List<ArtifactSummary>>fromJsonBody(response, ARTIFACT_SUMMARIES_TYPE).getResponseObject();
  }

  /**
   * Lists all versions of the given artifact in the given namespace.
   *
   * @param namespace the namespace to list artifact versions in
   * @param artifactName the name of the artifact
   * @return list of {@link ArtifactSummary}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   * @throws ArtifactNotFoundException if the given artifact does not exist
   */
  public List<ArtifactSummary> listVersions(Id.Namespace namespace, String artifactName)
    throws UnauthorizedException, IOException, ArtifactNotFoundException {
    return listVersions(namespace, artifactName, null);
  }

  /**
   * Lists all versions of the given artifact in the given namespace.
   *
   * @param namespace the namespace to list artifact versions in
   * @param artifactName the name of the artifact
   * @param scope the scope of artifacts to get. If none is given, the scope defaults to the user scope
   * @return list of {@link ArtifactSummary}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   * @throws ArtifactNotFoundException if the given artifact does not exist
   */
  public List<ArtifactSummary> listVersions(Id.Namespace namespace, String artifactName, @Nullable ArtifactScope scope)
    throws UnauthorizedException, IOException, ArtifactNotFoundException {

    URL url = scope == null ? config.resolveNamespacedURLV3(namespace, String.format("artifacts/%s", artifactName)) :
      config.resolveNamespacedURLV3(namespace, String.format("artifacts/%s?scope=%s", artifactName, scope.name()));

    HttpResponse response = restClient.execute(
      HttpMethod.GET, url, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new ArtifactNotFoundException(namespace, artifactName);
    }
    return ObjectResponse.<List<ArtifactSummary>>fromJsonBody(response, ARTIFACT_SUMMARIES_TYPE).getResponseObject();
  }

  /**
   * Gets information about a specific artifact version.
   *
   * @param artifactId the id of the artifact to get
   * @return information about the given artifact
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   * @throws ArtifactNotFoundException if the given artifact does not exist
   */
  public ArtifactInfo getArtifactInfo(Id.Artifact artifactId)
    throws IOException, UnauthorizedException, ArtifactNotFoundException {
    return getArtifactInfo(artifactId, ArtifactScope.USER);
  }

  /**
   * Gets information about a specific artifact version.
   *
   * @param artifactId the id of the artifact to get
   * @param scope the scope of the artifact
   * @return information about the given artifact
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   * @throws ArtifactNotFoundException if the given artifact does not exist
   */
  public ArtifactInfo getArtifactInfo(Id.Artifact artifactId, ArtifactScope scope)
    throws IOException, UnauthorizedException, ArtifactNotFoundException {

    String path = String.format("artifacts/%s/versions/%s?scope=%s",
      artifactId.getName(), artifactId.getVersion().getVersion(), scope.name());
    URL url = config.resolveNamespacedURLV3(artifactId.getNamespace(), path);

    HttpResponse response =
      restClient.execute(HttpMethod.GET, url, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new ArtifactNotFoundException(artifactId);
    }
    return ObjectResponse.fromJsonBody(response, ArtifactInfo.class, GSON).getResponseObject();
  }

  /**
   * Get summaries of all application classes in the given namespace, including classes from system artifacts.
   *
   * @param namespace the namespace to list application classes from
   * @return summaries of all application classes in the given namespace, including classes from system artifacts
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<ApplicationClassSummary> getApplicationClasses(Id.Namespace namespace)
    throws IOException, UnauthorizedException {
    return getApplicationClasses(namespace, (ArtifactScope) null);
  }

  /**
   * Get summaries of all application classes in the given namespace,
   * optionally including classes from system artifacts.
   *
   * @param namespace the namespace to list application classes from
   * @param scope the scope to list application classes in. If null, classes from all scopes are returned
   * @return summaries of all application classes in the given namespace, including classes from system artifacts
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<ApplicationClassSummary> getApplicationClasses(Id.Namespace namespace,
                                                             @Nullable ArtifactScope scope)
    throws IOException, UnauthorizedException {

    String path = scope == null ? "classes/apps" : String.format("classes/apps?scope=%s", scope.name());
    URL url = config.resolveNamespacedURLV3(namespace, path);

    HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
    return ObjectResponse.<List<ApplicationClassSummary>>fromJsonBody(
      response, APPCLASS_SUMMARIES_TYPE).getResponseObject();
  }

  /**
   * Get information about all application classes in the specified namespace, of the specified class name.
   *
   * @param namespace the namespace to list application classes from
   * @return summaries of all application classes in the given namespace, including classes from system artifacts
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<ApplicationClassInfo> getApplicationClasses(Id.Namespace namespace, String className)
    throws IOException, UnauthorizedException {
    return getApplicationClasses(namespace, className, ArtifactScope.USER);
  }

  /**
   * Get information about all application classes in the specified namespace, of the specified class name.
   *
   * @param namespace the namespace to list application classes from
   * @param scope the scope to list application classes in
   * @return summaries of all application classes in the given namespace, including classes from system artifacts
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<ApplicationClassInfo> getApplicationClasses(Id.Namespace namespace, String className, ArtifactScope scope)
    throws IOException, UnauthorizedException {

    String path = String.format("classes/apps/%s?scope=%s", className, scope.name());
    URL url = config.resolveNamespacedURLV3(namespace, path);

    HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
    return ObjectResponse.<List<ApplicationClassInfo>>fromJsonBody(
      response, APPCLASS_INFOS_TYPE, GSON).getResponseObject();
  }

  /**
   * Gets all the plugin types available to a specific artifact.
   *
   * @param artifactId the id of the artifact to get
   * @return list of plugin types available to the given artifact.
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<String> getPluginTypes(Id.Artifact artifactId) throws IOException, UnauthorizedException {
    return getPluginTypes(artifactId, ArtifactScope.USER);
  }

  /**
   * Gets all the plugin types available to a specific artifact.
   *
   * @param artifactId the id of the artifact to get
   * @param scope the scope of the artifact
   * @return list of plugin types available to the given artifact.
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<String> getPluginTypes(Id.Artifact artifactId,
                                     ArtifactScope scope) throws IOException, UnauthorizedException {

    String path = String.format("artifacts/%s/versions/%s/extensions?scope=%s",
      artifactId.getName(), artifactId.getVersion().getVersion(), scope.name());
    URL url = config.resolveNamespacedURLV3(artifactId.getNamespace(), path);

    HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
    return ObjectResponse.<List<String>>fromJsonBody(response, EXTENSIONS_TYPE).getResponseObject();
  }

  /**
   * Gets all the plugins of the given type available to the given artifact.
   *
   * @param artifactId the id of the artifact to get
   * @param pluginType the type of plugins to get
   * @return list of {@link PluginSummary}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<PluginSummary> getPluginSummaries(Id.Artifact artifactId,
                                                String pluginType) throws IOException, UnauthorizedException {
    return getPluginSummaries(artifactId, pluginType, ArtifactScope.USER);
  }

  /**
   * Gets all the plugins of the given type available to the given artifact.
   *
   * @param artifactId the id of the artifact to get
   * @param pluginType the type of plugins to get
   * @param scope the scope of the artifact
   * @return list of {@link PluginSummary}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<PluginSummary> getPluginSummaries(Id.Artifact artifactId, String pluginType,
                                                ArtifactScope scope) throws IOException, UnauthorizedException {

    String path = String.format("artifacts/%s/versions/%s/extensions/%s?scope=%s",
      artifactId.getName(), artifactId.getVersion().getVersion(), pluginType, scope.name());
    URL url = config.resolveNamespacedURLV3(artifactId.getNamespace(), path);

    HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
    return ObjectResponse.<List<PluginSummary>>fromJsonBody(response, PLUGIN_SUMMARIES_TYPE).getResponseObject();
  }

  /**
   * Gets all the plugins of the given type and name available to the given artifact.
   *
   * @param artifactId the id of the artifact to get
   * @param pluginType the type of plugins to get
   * @param pluginName the name of the plugins to get
   * @return list of {@link PluginInfo}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<PluginInfo> getPluginInfo(Id.Artifact artifactId, String pluginType,
                                        String pluginName) throws IOException, UnauthorizedException {
    return getPluginInfo(artifactId, pluginType, pluginName, ArtifactScope.USER);
  }

  /**
   * Gets all the plugins of the given type and name available to the given artifact.
   *
   * @param artifactId the id of the artifact to get
   * @param pluginType the type of plugins to get
   * @param pluginName the name of the plugins to get
   * @param scope the scope of the artifact
   * @return list of {@link PluginInfo}
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public List<PluginInfo> getPluginInfo(Id.Artifact artifactId, String pluginType, String pluginName,
                                        ArtifactScope scope) throws IOException, UnauthorizedException {

    String path = String.format("artifacts/%s/versions/%s/extensions/%s/plugins/%s?scope=%s",
      artifactId.getName(), artifactId.getVersion().getVersion(), pluginType, pluginName, scope.name());
    URL url = config.resolveNamespacedURLV3(artifactId.getNamespace(), path);

    HttpResponse response = restClient.execute(HttpMethod.GET, url, config.getAccessToken());
    return ObjectResponse.<List<PluginInfo>>fromJsonBody(response, PLUGIN_INFOS_TYPE).getResponseObject();
  }

  /**
   * Add an artifact.
   *
   * @param artifactId the id of the artifact to add
   * @param parentArtifacts the set of artifacts this artifact extends
   * @param artifactContents an input supplier for the contents of the artifact
   * @throws ArtifactAlreadyExistsException if the artifact already exists
   * @throws BadRequestException if the request is invalid. For example, if the artifact name or version is invalid
   * @throws ArtifactRangeNotFoundException if the parent artifacts do not exist
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public void add(Id.Artifact artifactId, @Nullable Set<ArtifactRange> parentArtifacts,
                  InputSupplier<? extends InputStream> artifactContents)
    throws UnauthorizedException, BadRequestException, ArtifactRangeNotFoundException,
    ArtifactAlreadyExistsException, IOException {

    add(artifactId.getNamespace(), artifactId.getName(), artifactContents,
        artifactId.getVersion().getVersion(), parentArtifacts);
  }

  /**
   * Add an artifact.
   *
   * @param namespace the namespace to add the artifact to
   * @param artifactName the name of the artifact to add
   * @param artifactContents an input supplier for the contents of the artifact
   * @param artifactVersion the version of the artifact to add. If null, the version will be derived from the
   *                        manifest of the artifact.
   * @throws ArtifactAlreadyExistsException if the artifact already exists
   * @throws BadRequestException if the request is invalid. For example, if the artifact name or version is invalid
   * @throws ArtifactRangeNotFoundException if the parent artifacts do not exist
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public void add(Id.Namespace namespace, String artifactName,
                  InputSupplier<? extends InputStream> artifactContents,
                  @Nullable String artifactVersion)
    throws ArtifactAlreadyExistsException, BadRequestException, IOException,
    UnauthorizedException, ArtifactRangeNotFoundException {

    add(namespace, artifactName, artifactContents, artifactVersion, null, null);
  }

  /**
   * Add an artifact.
   *
   * @param namespace the namespace to add the artifact to
   * @param artifactName the name of the artifact to add
   * @param artifactContents an input supplier for the contents of the artifact
   * @param artifactVersion the version of the artifact to add. If null, the version will be derived from the
   *                        manifest of the artifact
   * @param parentArtifacts the set of artifacts this artifact extends
   * @throws ArtifactAlreadyExistsException if the artifact already exists
   * @throws BadRequestException if the request is invalid. For example, if the artifact name or version is invalid
   * @throws ArtifactRangeNotFoundException if the parent artifacts do not exist
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public void add(Id.Namespace namespace, String artifactName, InputSupplier<? extends InputStream> artifactContents,
                  @Nullable String artifactVersion, @Nullable Set<ArtifactRange> parentArtifacts)
    throws ArtifactAlreadyExistsException, BadRequestException, IOException,
    UnauthorizedException, ArtifactRangeNotFoundException {

    add(namespace, artifactName, artifactContents, artifactVersion, parentArtifacts, null);
  }

  /**
   * Add an artifact.
   *
   * @param namespace the namespace to add the artifact to
   * @param artifactName the name of the artifact to add
   * @param artifactContents an input supplier for the contents of the artifact
   * @param artifactVersion the version of the artifact to add. If null, the version will be derived from the
   *                        manifest of the artifact
   * @param parentArtifacts the set of artifacts this artifact extends
   * @param additionalPlugins the set of plugins contained in the artifact that cannot be determined
   *                          through jar inspection. This set should include any classes that are plugins but could
   *                          not be annotated as such. For example, 3rd party classes like jdbc drivers fall into
   *                          this category.
   * @throws ArtifactAlreadyExistsException if the artifact already exists
   * @throws BadRequestException if the request is invalid. For example, if the artifact name or version is invalid
   * @throws ArtifactRangeNotFoundException if the parent artifacts do not exist
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public void add(Id.Namespace namespace, String artifactName,
                  InputSupplier<? extends InputStream> artifactContents,
                  @Nullable String artifactVersion,
                  @Nullable Set<ArtifactRange> parentArtifacts,
                  @Nullable Set<PluginClass> additionalPlugins)
    throws ArtifactAlreadyExistsException, BadRequestException, IOException,
    UnauthorizedException, ArtifactRangeNotFoundException {

    URL url = config.resolveNamespacedURLV3(namespace, String.format("artifacts/%s", artifactName));
    HttpRequest.Builder requestBuilder = HttpRequest.post(url);
    if (artifactVersion != null) {
      requestBuilder.addHeader("Artifact-Version", artifactVersion);
    }
    if (parentArtifacts != null) {
      requestBuilder.addHeader("Artifact-Extends", Joiner.on('/').join(parentArtifacts));
    }
    if (additionalPlugins != null) {
      requestBuilder.addHeader("Artifact-Plugins", GSON.toJson(additionalPlugins));
    }
    HttpRequest request = requestBuilder.withBody(artifactContents).build();

    HttpResponse response = restClient.execute(request, config.getAccessToken(),
      HttpURLConnection.HTTP_CONFLICT, HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_NOT_FOUND);

    int responseCode = response.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
      throw new ArtifactAlreadyExistsException(response.getResponseBodyAsString());
    } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException(response.getResponseBodyAsString());
    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new ArtifactRangeNotFoundException(parentArtifacts);
    }
  }

  /**
   * Delete an artifact.
   *
   * @param artifactId the artifact to delete
   *
   * @throws BadRequestException if the request is invalid. For example, if the artifact name or version is invalid
   * @throws IOException if a network error occurred
   * @throws UnauthorizedException if the request is not authorized successfully in the gateway server
   */
  public void delete(Id.Artifact artifactId) throws IOException, UnauthorizedException, BadRequestException {
    URL url = config.resolveNamespacedURLV3(artifactId.getNamespace(),
      String.format("artifacts/%s/versions/%s", artifactId.getName(), artifactId.getVersion().getVersion()));

    HttpRequest request = HttpRequest.delete(url).build();

    HttpResponse response = restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_BAD_REQUEST);

    int responseCode = response.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException(response.getResponseBodyAsString());
    }
  }

}
