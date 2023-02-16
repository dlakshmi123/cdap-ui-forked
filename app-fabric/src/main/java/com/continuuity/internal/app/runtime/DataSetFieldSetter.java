/*
 * Copyright 2012-2014 Continuuity, Inc.
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
package com.continuuity.internal.app.runtime;

import com.continuuity.api.annotation.UseDataSet;
import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.DataSetContext;
import com.continuuity.api.dataset.Dataset;
import com.continuuity.internal.lang.FieldVisitor;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;

/**
 * A {@link FieldVisitor} that inject DataSet instance into fields marked with {@link UseDataSet}.
 */
public final class DataSetFieldSetter extends FieldVisitor {

  private final DataSetContext dataSetContext;

  public DataSetFieldSetter(DataSetContext dataSetContext) {
    this.dataSetContext = dataSetContext;
  }

  @Override
  public void visit(Object instance, TypeToken<?> inspectType, TypeToken<?> declareType, Field field) throws Exception {
    if (DataSet.class.isAssignableFrom(field.getType()) || Dataset.class.isAssignableFrom(field.getType())) {
      UseDataSet useDataSet = field.getAnnotation(UseDataSet.class);
      if (useDataSet != null && !useDataSet.value().isEmpty()) {
        field.set(instance, dataSetContext.getDataSet(useDataSet.value()));
      }
    }
  }
}
