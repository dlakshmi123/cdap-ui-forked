/*
 * Copyright 2014 Cask, Inc.
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

import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.exception.AlreadyExistsException;
import co.cask.cdap.client.exception.BadRequestException;
import co.cask.cdap.client.exception.DatasetModuleAlreadyExistsException;
import co.cask.cdap.client.exception.DatasetModuleCannotBeDeletedException;
import co.cask.cdap.client.exception.DatasetModuleNotFoundException;
import co.cask.cdap.client.util.RESTClient;
import co.cask.cdap.common.http.HttpMethod;
import co.cask.cdap.common.http.HttpRequest;
import co.cask.cdap.common.http.HttpResponse;
import co.cask.cdap.common.http.ObjectResponse;
import co.cask.cdap.proto.DatasetModuleMeta;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Provides ways to interact with Reactor Dataset modules.
 */
public class DatasetModuleClient {

  private final RESTClient restClient;
  private final ClientConfig config;

  @Inject
  public DatasetModuleClient(ClientConfig config) {
    this.config = config;
    this.restClient = RESTClient.create(config);
  }

  /**
   * Lists all dataset modules.
   *
   * @return list of {@link DatasetModuleMeta}s.
   * @throws IOException if a network error occurred
   */
  public List<DatasetModuleMeta> list() throws IOException {
    URL url = config.resolveURL("data/modules");
    return ObjectResponse.fromJsonBody(restClient.execute(HttpMethod.GET, url),
                                       new TypeToken<List<DatasetModuleMeta>>() { }).getResponseObject();
  }

  /**
   * Adds a new dataset module.
   *
   * @param moduleName name of the new dataset module
   * @param className name of the dataset module class within the moduleJarFile
   * @param moduleJarFile Jar file containing the dataset module class and dependencies
   * @throws BadRequestException if the moduleJarFile does not exist
   * @throws AlreadyExistsException if a dataset module with the same name already exists
   * @throws IOException if a network error occurred
   */
  public void add(String moduleName, String className, File moduleJarFile)
    throws BadRequestException, AlreadyExistsException, IOException {

    URL url = config.resolveURL(String.format("data/modules/%s", moduleName));
    Map<String, String> headers = ImmutableMap.of("X-Continuuity-Class-Name", className);
    HttpRequest request = HttpRequest.put(url).addHeaders(headers).withBody(moduleJarFile).build();

    HttpResponse response = restClient.upload(request, HttpURLConnection.HTTP_BAD_REQUEST,
                                              HttpURLConnection.HTTP_CONFLICT);
    if (response.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException(String.format("Module jar file does not exist: %s", moduleJarFile));
    } else if (response.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
      throw new DatasetModuleAlreadyExistsException(moduleName);
    }
  }

  /**
   * Deletes a dataset module.
   *
   * @param moduleName name of the dataset module to delete
   * @throws DatasetModuleCannotBeDeletedException if the dataset module cannot be deleted,
   * usually due to other dataset modules or dataset instances using the dataset module
   * @throws DatasetModuleNotFoundException if the dataset module with the specified name was not found
   * @throws IOException if a network error occurred
   */
  public void delete(String moduleName)
    throws DatasetModuleCannotBeDeletedException, DatasetModuleNotFoundException, IOException {

    URL url = config.resolveURL(String.format("data/modules/%s", moduleName));
    HttpResponse response = restClient.execute(HttpMethod.DELETE, url, HttpURLConnection.HTTP_CONFLICT,
                                               HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
      throw new DatasetModuleCannotBeDeletedException(moduleName);
    } else if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new DatasetModuleNotFoundException(moduleName);
    }
  }

  /**
   * Deletes all dataset modules.
   *
   * @throws DatasetModuleCannotBeDeletedException if one of the dataset modules cannot be deleted,
   * usually due to existing dataset instances using the dataset module
   * @throws IOException if a network error occurred
   */
  public void deleteAll() throws DatasetModuleCannotBeDeletedException, IOException {
    URL url = config.resolveURL("data/modules");
    HttpResponse response = restClient.execute(HttpMethod.DELETE, url, HttpURLConnection.HTTP_CONFLICT);
    if (response.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
      // TODO: exception for all modules
      throw new DatasetModuleCannotBeDeletedException(null);
    }
  }

  /**
   * Gets information about a dataset module.
   *
   * @param moduleName name of the dataset module
   * @return {@link DatasetModuleMeta} of the dataset module
   * @throws DatasetModuleNotFoundException if the dataset module with the specified name was not found
   * @throws IOException if a network error occurred
   */
  public DatasetModuleMeta get(String moduleName) throws DatasetModuleNotFoundException, IOException {
    URL url = config.resolveURL(String.format("data/modules/%s", moduleName));
    HttpResponse response = restClient.execute(HttpMethod.GET, url, HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new DatasetModuleNotFoundException(moduleName);
    }

    return ObjectResponse.fromJsonBody(response, DatasetModuleMeta.class).getResponseObject();
  }
}
