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

package co.cask.cdap.etl.common;

import co.cask.cdap.etl.api.Emitter;
import co.cask.cdap.etl.api.InvalidEntry;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * Default implementation of {@link Emitter}. Has methods to get the values and errors emitted.
 *
 * @param <T> the type of object to emit
 */
public class DefaultEmitter<T> implements Emitter<T> {
  private final List<T> entryList;
  private final List<InvalidEntry<T>> errorList;

  public DefaultEmitter() {
    this.entryList = Lists.newArrayList();
    this.errorList = Lists.newArrayList();
  }

  @Override
  public void emit(T value) {
    entryList.add(value);
  }

  @Override
  public void emitError(InvalidEntry<T> value) {
    errorList.add(value);
  }

  public Collection<T> getEntries() {
    return entryList;
  }

  public Collection<InvalidEntry<T>> getErrors() {
    return errorList;
  }

  public void reset() {
    entryList.clear();
    errorList.clear();
  }
}
