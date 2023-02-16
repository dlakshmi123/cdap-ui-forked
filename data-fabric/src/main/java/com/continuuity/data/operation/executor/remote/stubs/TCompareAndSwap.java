/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.continuuity.data.operation.executor.remote.stubs;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCompareAndSwap implements org.apache.thrift.TBase<TCompareAndSwap, TCompareAndSwap._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TCompareAndSwap");

  private static final org.apache.thrift.protocol.TField TABLE_FIELD_DESC = new org.apache.thrift.protocol.TField("table", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField METRIC_FIELD_DESC = new org.apache.thrift.protocol.TField("metric", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField KEY_FIELD_DESC = new org.apache.thrift.protocol.TField("key", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField COLUMN_FIELD_DESC = new org.apache.thrift.protocol.TField("column", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField EXPECTED_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("expectedValue", org.apache.thrift.protocol.TType.STRING, (short)5);
  private static final org.apache.thrift.protocol.TField NEW_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("newValue", org.apache.thrift.protocol.TType.STRING, (short)6);
  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I64, (short)7);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TCompareAndSwapStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TCompareAndSwapTupleSchemeFactory());
  }

  public String table; // optional
  public String metric; // optional
  public ByteBuffer key; // required
  public ByteBuffer column; // required
  public ByteBuffer expectedValue; // required
  public ByteBuffer newValue; // required
  public long id; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TABLE((short)1, "table"),
    METRIC((short)2, "metric"),
    KEY((short)3, "key"),
    COLUMN((short)4, "column"),
    EXPECTED_VALUE((short)5, "expectedValue"),
    NEW_VALUE((short)6, "newValue"),
    ID((short)7, "id");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TABLE
          return TABLE;
        case 2: // METRIC
          return METRIC;
        case 3: // KEY
          return KEY;
        case 4: // COLUMN
          return COLUMN;
        case 5: // EXPECTED_VALUE
          return EXPECTED_VALUE;
        case 6: // NEW_VALUE
          return NEW_VALUE;
        case 7: // ID
          return ID;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ID_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  private _Fields optionals[] = {_Fields.TABLE,_Fields.METRIC};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TABLE, new org.apache.thrift.meta_data.FieldMetaData("table", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.METRIC, new org.apache.thrift.meta_data.FieldMetaData("metric", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.KEY, new org.apache.thrift.meta_data.FieldMetaData("key", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.COLUMN, new org.apache.thrift.meta_data.FieldMetaData("column", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.EXPECTED_VALUE, new org.apache.thrift.meta_data.FieldMetaData("expectedValue", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.NEW_VALUE, new org.apache.thrift.meta_data.FieldMetaData("newValue", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TCompareAndSwap.class, metaDataMap);
  }

  public TCompareAndSwap() {
  }

  public TCompareAndSwap(
    ByteBuffer key,
    ByteBuffer column,
    ByteBuffer expectedValue,
    ByteBuffer newValue,
    long id)
  {
    this();
    this.key = key;
    this.column = column;
    this.expectedValue = expectedValue;
    this.newValue = newValue;
    this.id = id;
    setIdIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TCompareAndSwap(TCompareAndSwap other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetTable()) {
      this.table = other.table;
    }
    if (other.isSetMetric()) {
      this.metric = other.metric;
    }
    if (other.isSetKey()) {
      this.key = org.apache.thrift.TBaseHelper.copyBinary(other.key);
;
    }
    if (other.isSetColumn()) {
      this.column = org.apache.thrift.TBaseHelper.copyBinary(other.column);
;
    }
    if (other.isSetExpectedValue()) {
      this.expectedValue = org.apache.thrift.TBaseHelper.copyBinary(other.expectedValue);
;
    }
    if (other.isSetNewValue()) {
      this.newValue = org.apache.thrift.TBaseHelper.copyBinary(other.newValue);
;
    }
    this.id = other.id;
  }

  public TCompareAndSwap deepCopy() {
    return new TCompareAndSwap(this);
  }

  @Override
  public void clear() {
    this.table = null;
    this.metric = null;
    this.key = null;
    this.column = null;
    this.expectedValue = null;
    this.newValue = null;
    setIdIsSet(false);
    this.id = 0;
  }

  public String getTable() {
    return this.table;
  }

  public TCompareAndSwap setTable(String table) {
    this.table = table;
    return this;
  }

  public void unsetTable() {
    this.table = null;
  }

  /** Returns true if field table is set (has been assigned a value) and false otherwise */
  public boolean isSetTable() {
    return this.table != null;
  }

  public void setTableIsSet(boolean value) {
    if (!value) {
      this.table = null;
    }
  }

  public String getMetric() {
    return this.metric;
  }

  public TCompareAndSwap setMetric(String metric) {
    this.metric = metric;
    return this;
  }

  public void unsetMetric() {
    this.metric = null;
  }

  /** Returns true if field metric is set (has been assigned a value) and false otherwise */
  public boolean isSetMetric() {
    return this.metric != null;
  }

  public void setMetricIsSet(boolean value) {
    if (!value) {
      this.metric = null;
    }
  }

  public byte[] getKey() {
    setKey(org.apache.thrift.TBaseHelper.rightSize(key));
    return key == null ? null : key.array();
  }

  public ByteBuffer bufferForKey() {
    return key;
  }

  public TCompareAndSwap setKey(byte[] key) {
    setKey(key == null ? (ByteBuffer)null : ByteBuffer.wrap(key));
    return this;
  }

  public TCompareAndSwap setKey(ByteBuffer key) {
    this.key = key;
    return this;
  }

  public void unsetKey() {
    this.key = null;
  }

  /** Returns true if field key is set (has been assigned a value) and false otherwise */
  public boolean isSetKey() {
    return this.key != null;
  }

  public void setKeyIsSet(boolean value) {
    if (!value) {
      this.key = null;
    }
  }

  public byte[] getColumn() {
    setColumn(org.apache.thrift.TBaseHelper.rightSize(column));
    return column == null ? null : column.array();
  }

  public ByteBuffer bufferForColumn() {
    return column;
  }

  public TCompareAndSwap setColumn(byte[] column) {
    setColumn(column == null ? (ByteBuffer)null : ByteBuffer.wrap(column));
    return this;
  }

  public TCompareAndSwap setColumn(ByteBuffer column) {
    this.column = column;
    return this;
  }

  public void unsetColumn() {
    this.column = null;
  }

  /** Returns true if field column is set (has been assigned a value) and false otherwise */
  public boolean isSetColumn() {
    return this.column != null;
  }

  public void setColumnIsSet(boolean value) {
    if (!value) {
      this.column = null;
    }
  }

  public byte[] getExpectedValue() {
    setExpectedValue(org.apache.thrift.TBaseHelper.rightSize(expectedValue));
    return expectedValue == null ? null : expectedValue.array();
  }

  public ByteBuffer bufferForExpectedValue() {
    return expectedValue;
  }

  public TCompareAndSwap setExpectedValue(byte[] expectedValue) {
    setExpectedValue(expectedValue == null ? (ByteBuffer)null : ByteBuffer.wrap(expectedValue));
    return this;
  }

  public TCompareAndSwap setExpectedValue(ByteBuffer expectedValue) {
    this.expectedValue = expectedValue;
    return this;
  }

  public void unsetExpectedValue() {
    this.expectedValue = null;
  }

  /** Returns true if field expectedValue is set (has been assigned a value) and false otherwise */
  public boolean isSetExpectedValue() {
    return this.expectedValue != null;
  }

  public void setExpectedValueIsSet(boolean value) {
    if (!value) {
      this.expectedValue = null;
    }
  }

  public byte[] getNewValue() {
    setNewValue(org.apache.thrift.TBaseHelper.rightSize(newValue));
    return newValue == null ? null : newValue.array();
  }

  public ByteBuffer bufferForNewValue() {
    return newValue;
  }

  public TCompareAndSwap setNewValue(byte[] newValue) {
    setNewValue(newValue == null ? (ByteBuffer)null : ByteBuffer.wrap(newValue));
    return this;
  }

  public TCompareAndSwap setNewValue(ByteBuffer newValue) {
    this.newValue = newValue;
    return this;
  }

  public void unsetNewValue() {
    this.newValue = null;
  }

  /** Returns true if field newValue is set (has been assigned a value) and false otherwise */
  public boolean isSetNewValue() {
    return this.newValue != null;
  }

  public void setNewValueIsSet(boolean value) {
    if (!value) {
      this.newValue = null;
    }
  }

  public long getId() {
    return this.id;
  }

  public TCompareAndSwap setId(long id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bit_vector.clear(__ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return __isset_bit_vector.get(__ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bit_vector.set(__ID_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TABLE:
      if (value == null) {
        unsetTable();
      } else {
        setTable((String)value);
      }
      break;

    case METRIC:
      if (value == null) {
        unsetMetric();
      } else {
        setMetric((String)value);
      }
      break;

    case KEY:
      if (value == null) {
        unsetKey();
      } else {
        setKey((ByteBuffer)value);
      }
      break;

    case COLUMN:
      if (value == null) {
        unsetColumn();
      } else {
        setColumn((ByteBuffer)value);
      }
      break;

    case EXPECTED_VALUE:
      if (value == null) {
        unsetExpectedValue();
      } else {
        setExpectedValue((ByteBuffer)value);
      }
      break;

    case NEW_VALUE:
      if (value == null) {
        unsetNewValue();
      } else {
        setNewValue((ByteBuffer)value);
      }
      break;

    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Long)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TABLE:
      return getTable();

    case METRIC:
      return getMetric();

    case KEY:
      return getKey();

    case COLUMN:
      return getColumn();

    case EXPECTED_VALUE:
      return getExpectedValue();

    case NEW_VALUE:
      return getNewValue();

    case ID:
      return Long.valueOf(getId());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TABLE:
      return isSetTable();
    case METRIC:
      return isSetMetric();
    case KEY:
      return isSetKey();
    case COLUMN:
      return isSetColumn();
    case EXPECTED_VALUE:
      return isSetExpectedValue();
    case NEW_VALUE:
      return isSetNewValue();
    case ID:
      return isSetId();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TCompareAndSwap)
      return this.equals((TCompareAndSwap)that);
    return false;
  }

  public boolean equals(TCompareAndSwap that) {
    if (that == null)
      return false;

    boolean this_present_table = true && this.isSetTable();
    boolean that_present_table = true && that.isSetTable();
    if (this_present_table || that_present_table) {
      if (!(this_present_table && that_present_table))
        return false;
      if (!this.table.equals(that.table))
        return false;
    }

    boolean this_present_metric = true && this.isSetMetric();
    boolean that_present_metric = true && that.isSetMetric();
    if (this_present_metric || that_present_metric) {
      if (!(this_present_metric && that_present_metric))
        return false;
      if (!this.metric.equals(that.metric))
        return false;
    }

    boolean this_present_key = true && this.isSetKey();
    boolean that_present_key = true && that.isSetKey();
    if (this_present_key || that_present_key) {
      if (!(this_present_key && that_present_key))
        return false;
      if (!this.key.equals(that.key))
        return false;
    }

    boolean this_present_column = true && this.isSetColumn();
    boolean that_present_column = true && that.isSetColumn();
    if (this_present_column || that_present_column) {
      if (!(this_present_column && that_present_column))
        return false;
      if (!this.column.equals(that.column))
        return false;
    }

    boolean this_present_expectedValue = true && this.isSetExpectedValue();
    boolean that_present_expectedValue = true && that.isSetExpectedValue();
    if (this_present_expectedValue || that_present_expectedValue) {
      if (!(this_present_expectedValue && that_present_expectedValue))
        return false;
      if (!this.expectedValue.equals(that.expectedValue))
        return false;
    }

    boolean this_present_newValue = true && this.isSetNewValue();
    boolean that_present_newValue = true && that.isSetNewValue();
    if (this_present_newValue || that_present_newValue) {
      if (!(this_present_newValue && that_present_newValue))
        return false;
      if (!this.newValue.equals(that.newValue))
        return false;
    }

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(TCompareAndSwap other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TCompareAndSwap typedOther = (TCompareAndSwap)other;

    lastComparison = Boolean.valueOf(isSetTable()).compareTo(typedOther.isSetTable());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTable()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.table, typedOther.table);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMetric()).compareTo(typedOther.isSetMetric());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMetric()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.metric, typedOther.metric);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetKey()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key, typedOther.key);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetColumn()).compareTo(typedOther.isSetColumn());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumn()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.column, typedOther.column);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExpectedValue()).compareTo(typedOther.isSetExpectedValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExpectedValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.expectedValue, typedOther.expectedValue);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetNewValue()).compareTo(typedOther.isSetNewValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNewValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.newValue, typedOther.newValue);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetId()).compareTo(typedOther.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, typedOther.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TCompareAndSwap(");
    boolean first = true;

    if (isSetTable()) {
      sb.append("table:");
      if (this.table == null) {
        sb.append("null");
      } else {
        sb.append(this.table);
      }
      first = false;
    }
    if (isSetMetric()) {
      if (!first) sb.append(", ");
      sb.append("metric:");
      if (this.metric == null) {
        sb.append("null");
      } else {
        sb.append(this.metric);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("key:");
    if (this.key == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.key, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("column:");
    if (this.column == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.column, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("expectedValue:");
    if (this.expectedValue == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.expectedValue, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("newValue:");
    if (this.newValue == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.newValue, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("id:");
    sb.append(this.id);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TCompareAndSwapStandardSchemeFactory implements SchemeFactory {
    public TCompareAndSwapStandardScheme getScheme() {
      return new TCompareAndSwapStandardScheme();
    }
  }

  private static class TCompareAndSwapStandardScheme extends StandardScheme<TCompareAndSwap> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TCompareAndSwap struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TABLE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.table = iprot.readString();
              struct.setTableIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // METRIC
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.metric = iprot.readString();
              struct.setMetricIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // KEY
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.key = iprot.readBinary();
              struct.setKeyIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // COLUMN
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.column = iprot.readBinary();
              struct.setColumnIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // EXPECTED_VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.expectedValue = iprot.readBinary();
              struct.setExpectedValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // NEW_VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.newValue = iprot.readBinary();
              struct.setNewValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.id = iprot.readI64();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TCompareAndSwap struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.table != null) {
        if (struct.isSetTable()) {
          oprot.writeFieldBegin(TABLE_FIELD_DESC);
          oprot.writeString(struct.table);
          oprot.writeFieldEnd();
        }
      }
      if (struct.metric != null) {
        if (struct.isSetMetric()) {
          oprot.writeFieldBegin(METRIC_FIELD_DESC);
          oprot.writeString(struct.metric);
          oprot.writeFieldEnd();
        }
      }
      if (struct.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(struct.key);
        oprot.writeFieldEnd();
      }
      if (struct.column != null) {
        oprot.writeFieldBegin(COLUMN_FIELD_DESC);
        oprot.writeBinary(struct.column);
        oprot.writeFieldEnd();
      }
      if (struct.expectedValue != null) {
        oprot.writeFieldBegin(EXPECTED_VALUE_FIELD_DESC);
        oprot.writeBinary(struct.expectedValue);
        oprot.writeFieldEnd();
      }
      if (struct.newValue != null) {
        oprot.writeFieldBegin(NEW_VALUE_FIELD_DESC);
        oprot.writeBinary(struct.newValue);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI64(struct.id);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TCompareAndSwapTupleSchemeFactory implements SchemeFactory {
    public TCompareAndSwapTupleScheme getScheme() {
      return new TCompareAndSwapTupleScheme();
    }
  }

  private static class TCompareAndSwapTupleScheme extends TupleScheme<TCompareAndSwap> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TCompareAndSwap struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetTable()) {
        optionals.set(0);
      }
      if (struct.isSetMetric()) {
        optionals.set(1);
      }
      if (struct.isSetKey()) {
        optionals.set(2);
      }
      if (struct.isSetColumn()) {
        optionals.set(3);
      }
      if (struct.isSetExpectedValue()) {
        optionals.set(4);
      }
      if (struct.isSetNewValue()) {
        optionals.set(5);
      }
      if (struct.isSetId()) {
        optionals.set(6);
      }
      oprot.writeBitSet(optionals, 7);
      if (struct.isSetTable()) {
        oprot.writeString(struct.table);
      }
      if (struct.isSetMetric()) {
        oprot.writeString(struct.metric);
      }
      if (struct.isSetKey()) {
        oprot.writeBinary(struct.key);
      }
      if (struct.isSetColumn()) {
        oprot.writeBinary(struct.column);
      }
      if (struct.isSetExpectedValue()) {
        oprot.writeBinary(struct.expectedValue);
      }
      if (struct.isSetNewValue()) {
        oprot.writeBinary(struct.newValue);
      }
      if (struct.isSetId()) {
        oprot.writeI64(struct.id);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TCompareAndSwap struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(7);
      if (incoming.get(0)) {
        struct.table = iprot.readString();
        struct.setTableIsSet(true);
      }
      if (incoming.get(1)) {
        struct.metric = iprot.readString();
        struct.setMetricIsSet(true);
      }
      if (incoming.get(2)) {
        struct.key = iprot.readBinary();
        struct.setKeyIsSet(true);
      }
      if (incoming.get(3)) {
        struct.column = iprot.readBinary();
        struct.setColumnIsSet(true);
      }
      if (incoming.get(4)) {
        struct.expectedValue = iprot.readBinary();
        struct.setExpectedValueIsSet(true);
      }
      if (incoming.get(5)) {
        struct.newValue = iprot.readBinary();
        struct.setNewValueIsSet(true);
      }
      if (incoming.get(6)) {
        struct.id = iprot.readI64();
        struct.setIdIsSet(true);
      }
    }
  }

}
