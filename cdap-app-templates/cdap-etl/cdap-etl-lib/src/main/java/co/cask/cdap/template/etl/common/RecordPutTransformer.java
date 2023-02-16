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

package co.cask.cdap.template.etl.common;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.data.format.StructuredRecord;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.dataset.table.Put;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;

/**
 * Transforms records into Puts.
 */
public class RecordPutTransformer {
  // Not final for an optimization - When rowField is case-insensitive, we may have to look for it in the schema
  // provided to the toPut method. When there isn't an exact match but a case-insensitive match, we have to loop over
  // all fields in the schema to find the case-insensitive match. Making this non-final allows us to do this look up
  // once and cache the value.
  private String rowField;
  private final boolean rowFieldCaseSensitive;
  private final Schema outputSchema;

  @VisibleForTesting
  RecordPutTransformer(String rowField) {
    this(rowField, null, true);
  }

  @VisibleForTesting
  RecordPutTransformer(String rowField, Schema outputSchema) {
    this(rowField, outputSchema, true);
  }

  public RecordPutTransformer(String rowField, Schema outputSchema, boolean rowFieldCaseSensitive) {
    this.rowField = rowField;
    this.rowFieldCaseSensitive = rowFieldCaseSensitive;
    this.outputSchema = outputSchema;
  }

  public Put toPut(StructuredRecord record) {
    Schema recordSchema = record.getSchema();
    Preconditions.checkArgument(recordSchema.getType() == Schema.Type.RECORD, "input must be a record.");

    Schema.Field keyField = getKeyField(recordSchema);
    Preconditions.checkArgument(keyField != null, "Could not find key field in record.");

    Put output = createPut(record, keyField);

    for (Schema.Field field : recordSchema.getFields()) {
      if (field.getName().equals(keyField.getName())) {
        continue;
      }

      // Skip fields that are not present in the Output Schema
      if (outputSchema != null && outputSchema.getField(field.getName()) == null) {
        continue;
      }
      setField(output, field, record.get(field.getName()));
    }
    return output;
  }

  @SuppressWarnings("ConstantConditions")
  private void setField(Put put, Schema.Field field, Object val) {
    // have to handle nulls differently. In a Put object, it's only valid to use the add(byte[], byte[])
    // for null values, as the other add methods take boolean vs Boolean, int vs Integer, etc.
    if (field.getSchema().isNullable() && val == null) {
      put.add(field.getName(), (byte[]) null);
      return;
    }

    Schema.Type type = validateAndGetType(field);

    switch (type) {
      case BOOLEAN:
        put.add(field.getName(), (Boolean) val);
        break;
      case INT:
        put.add(field.getName(), (Integer) val);
        break;
      case LONG:
        put.add(field.getName(), (Long) val);
        break;
      case FLOAT:
        put.add(field.getName(), (Float) val);
        break;
      case DOUBLE:
        put.add(field.getName(), (Double) val);
        break;
      case BYTES:
        if (val instanceof ByteBuffer) {
          put.add(field.getName(), Bytes.toBytes((ByteBuffer) val));
        } else {
          put.add(field.getName(), (byte[]) val);
        }
        break;
      case STRING:
        put.add(field.getName(), (String) val);
        break;
      default:
        throw new IllegalArgumentException("Field " + field.getName() + " is of unsupported type " + type);
    }
  }

  // get the non-nullable type of the field and check that it's a simple type.
  private Schema.Type validateAndGetType(Schema.Field field) {
    Schema.Type type;
    if (field.getSchema().isNullable()) {
      type = field.getSchema().getNonNullable().getType();
    } else {
      type = field.getSchema().getType();
    }
    Preconditions.checkArgument(type.isSimpleType(),
        "only simple types are supported (boolean, int, long, float, double, bytes).");
    return type;
  }

  @SuppressWarnings("ConstantConditions")
  private Put createPut(StructuredRecord record, Schema.Field keyField) {
    String keyFieldName = keyField.getName();
    Object val = record.get(keyFieldName);
    Preconditions.checkArgument(val != null, "Row key cannot be null.");

    Schema.Type keyType = validateAndGetType(keyField);
    switch (keyType) {
      case BOOLEAN:
        return new Put(Bytes.toBytes((Boolean) val));
      case INT:
        return new Put(Bytes.toBytes((Integer) val));
      case LONG:
        return new Put(Bytes.toBytes((Long) val));
      case FLOAT:
        return new Put(Bytes.toBytes((Float) val));
      case DOUBLE:
        return new Put(Bytes.toBytes((Double) val));
      case BYTES:
        if (val instanceof ByteBuffer) {
          return new Put(Bytes.toBytes((ByteBuffer) val));
        } else {
          return new Put((byte[]) val);
        }
      case STRING:
        return new Put(Bytes.toBytes((String) record.get(keyFieldName)));
      default:
        throw new IllegalArgumentException("Row key is of unsupported type " + keyType);
    }
  }

  @Nullable
  private Schema.Field getKeyField(Schema recordSchema) {
    Schema.Field field = recordSchema.getField(rowField);
    if (field == null && !rowFieldCaseSensitive) {
      Iterable<Schema.Field> filtered = Iterables.filter(recordSchema.getFields(), new Predicate<Schema.Field>() {
        @Override
        public boolean apply(Schema.Field input) {
          return input.getName().equalsIgnoreCase(rowField);
        }
      });
      if (!Iterables.isEmpty(filtered)) {
        Preconditions.checkArgument(Iterables.size(filtered) == 1,
                                    "Cannot have multiple fields in the schema that match %s in a case-insensitive " +
                                      "manner when the property %s is false. Found %s.",
                                    rowField, Properties.Table.CASE_SENSITIVE_ROW_FIELD, Iterables.toString(filtered));
        field = filtered.iterator().next();
        rowField = field.getName();
      }
    }
    return field;
  }
}
