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

package co.cask.cdap.internal.app.deploy.pipeline;

import co.cask.cdap.app.store.Store;
import co.cask.cdap.pipeline.AbstractStage;
import com.google.common.reflect.TypeToken;

/**
 *
 */
public class ApplicationRegistrationStage extends AbstractStage<ApplicationWithPrograms> {
  private final Store store;

  public ApplicationRegistrationStage(Store store) {
    super(TypeToken.of(ApplicationWithPrograms.class));
    this.store = store;
  }

  @Override
  public void process(ApplicationWithPrograms input) throws Exception {
    store.addApplication(input.getId(), input.getSpecification(), input.getLocation());
    emit(input);
  }
}
