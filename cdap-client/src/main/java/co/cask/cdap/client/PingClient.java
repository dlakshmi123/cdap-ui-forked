/*
 * Copyright © 2014 Cask Data, Inc.
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
import co.cask.cdap.client.exception.UnAuthorizedAccessTokenException;
import co.cask.cdap.client.util.RESTClient;
import co.cask.cdap.common.http.HttpMethod;

import java.io.IOException;
import javax.inject.Inject;

/**
 * Provides ways to ping CDAP instances.
 */
public class PingClient {

  private final RESTClient restClient;
  private final ClientConfig config;

  @Inject
  public PingClient(ClientConfig config) {
    this.config = config;
    this.restClient = RESTClient.create(config);
  }

  public void ping() throws IOException, UnAuthorizedAccessTokenException {
    restClient.execute(HttpMethod.GET, config.resolveURL("ping"), config.getAccessToken());
  }

}
