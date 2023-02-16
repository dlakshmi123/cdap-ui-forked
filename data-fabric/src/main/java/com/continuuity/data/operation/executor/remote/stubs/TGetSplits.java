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

public class TGetSplits implements org.apache.thrift.TBase<TGetSplits, TGetSplits._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TGetSplits");

  private static final org.apache.thrift.protocol.TField TABLE_FIELD_DESC = new org.apache.thrift.protocol.TField("table", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField METRIC_FIELD_DESC = new org.apache.thrift.protocol.TField("metric", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField START_FIELD_DESC = new org.apache.thrift.protocol.TField("start", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField STOP_FIELD_DESC = new org.apache.thrift.protocol.TField("stop", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short)5);
  private static final org.apache.thrift.protocol.TField NUM_SPLITS_FIELD_DESC = new org.apache.thrift.protocol.TField("numSplits", org.apache.thrift.protocol.TType.I32, (short)6);
  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I64, (short)7);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TGetSplitsStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TGetSplitsTupleSchemeFactory());
  }

  public String table; // required
  public String metric; // optional
  public ByteBuffer start; // optional
  public ByteBuffer stop; // optional
  public List<ByteBuffer> columns; // optional
  public int numSplits; // required
  public long id; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TABLE((short)1, "table"),
    METRIC((short)2, "metric"),
    START((short)3, "start"),
    STOP((short)4, "stop"),
    COLUMNS((short)5, "columns"),
    NUM_SPLITS((short)6, "numSplits"),
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
        case 3: // START
          return START;
        case 4: // STOP
          return STOP;
        case 5: // COLUMNS
          return COLUMNS;
        case 6: // NUM_SPLITS
          return NUM_SPLITS;
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
  private static final int __NUMSPLITS_ISSET_ID = 0;
  private static final int __ID_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);
  private _Fields optionals[] = {_Fields.METRIC,_Fields.START,_Fields.STOP,_Fields.COLUMNS};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TABLE, new org.apache.thrift.meta_data.FieldMetaData("table", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.METRIC, new org.apache.thrift.meta_data.FieldMetaData("metric", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.START, new org.apache.thrift.meta_data.FieldMetaData("start", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.STOP, new org.apache.thrift.meta_data.FieldMetaData("stop", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING            , true))));
    tmpMap.put(_Fields.NUM_SPLITS, new org.apache.thrift.meta_data.FieldMetaData("numSplits", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TGetSplits.class, metaDataMap);
  }

  public TGetSplits() {
  }

  public TGetSplits(
    String table,
    int numSplits,
    long id)
  {
    this();
    this.table = table;
    this.numSplits = numSplits;
    setNumSplitsIsSet(true);
    this.id = id;
    setIdIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TGetSplits(TGetSplits other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetTable()) {
      this.table = other.table;
    }
    if (other.isSetMetric()) {
      this.metric = other.metric;
    }
    if (other.isSetStart()) {
      this.start = org.apache.thrift.TBaseHelper.copyBinary(other.start);
;
    }
    if (other.isSetStop()) {
      this.stop = org.apache.thrift.TBaseHelper.copyBinary(other.stop);
;
    }
    if (other.isSetColumns()) {
      List<ByteBuffer> __this__columns = new ArrayList<ByteBuffer>();
      for (ByteBuffer other_element : other.columns) {
        ByteBuffer temp_binary_element = org.apache.thrift.TBaseHelper.copyBinary(other_element);
;
        __this__columns.add(temp_binary_element);
      }
      this.columns = __this__columns;
    }
    this.numSplits = other.numSplits;
    this.id = other.id;
  }

  public TGetSplits deepCopy() {
    return new TGetSplits(this);
  }

  @Override
  public void clear() {
    this.table = null;
    this.metric = null;
    this.start = null;
    this.stop = null;
    this.columns = null;
    setNumSplitsIsSet(false);
    this.numSplits = 0;
    setIdIsSet(false);
    this.id = 0;
  }

  public String getTable() {
    return this.table;
  }

  public TGetSplits setTable(String table) {
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

  public TGetSplits setMetric(String metric) {
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

  public byte[] getStart() {
    setStart(org.apache.thrift.TBaseHelper.rightSize(start));
    return start == null ? null : start.array();
  }

  public ByteBuffer bufferForStart() {
    return start;
  }

  public TGetSplits setStart(byte[] start) {
    setStart(start == null ? (ByteBuffer)null : ByteBuffer.wrap(start));
    return this;
  }

  public TGetSplits setStart(ByteBuffer start) {
    this.start = start;
    return this;
  }

  public void unsetStart() {
    this.start = null;
  }

  /** Returns true if field start is set (has been assigned a value) and false otherwise */
  public boolean isSetStart() {
    return this.start != null;
  }

  public void setStartIsSet(boolean value) {
    if (!value) {
      this.start = null;
    }
  }

  public byte[] getStop() {
    setStop(org.apache.thrift.TBaseHelper.rightSize(stop));
    return stop == null ? null : stop.array();
  }

  public ByteBuffer bufferForStop() {
    return stop;
  }

  public TGetSplits setStop(byte[] stop) {
    setStop(stop == null ? (ByteBuffer)null : ByteBuffer.wrap(stop));
    return this;
  }

  public TGetSplits setStop(ByteBuffer stop) {
    this.stop = stop;
    return this;
  }

  public void unsetStop() {
    this.stop = null;
  }

  /** Returns true if field stop is set (has been assigned a value) and false otherwise */
  public boolean isSetStop() {
    return this.stop != null;
  }

  public void setStopIsSet(boolean value) {
    if (!value) {
      this.stop = null;
    }
  }

  public int getColumnsSize() {
    return (this.columns == null) ? 0 : this.columns.size();
  }

  public java.util.Iterator<ByteBuffer> getColumnsIterator() {
    return (this.columns == null) ? null : this.columns.iterator();
  }

  public void addToColumns(ByteBuffer elem) {
    if (this.columns == null) {
      this.columns = new ArrayList<ByteBuffer>();
    }
    this.columns.add(elem);
  }

  public List<ByteBuffer> getColumns() {
    return this.columns;
  }

  public TGetSplits setColumns(List<ByteBuffer> columns) {
    this.columns = columns;
    return this;
  }

  public void unsetColumns() {
    this.columns = null;
  }

  /** Returns true if field columns is set (has been assigned a value) and false otherwise */
  public boolean isSetColumns() {
    return this.columns != null;
  }

  public void setColumnsIsSet(boolean value) {
    if (!value) {
      this.columns = null;
    }
  }

  public int getNumSplits() {
    return this.numSplits;
  }

  public TGetSplits setNumSplits(int numSplits) {
    this.numSplits = numSplits;
    setNumSplitsIsSet(true);
    return this;
  }

  public void unsetNumSplits() {
    __isset_bit_vector.clear(__NUMSPLITS_ISSET_ID);
  }

  /** Returns true if field numSplits is set (has been assigned a value) and false otherwise */
  public boolean isSetNumSplits() {
    return __isset_bit_vector.get(__NUMSPLITS_ISSET_ID);
  }

  public void setNumSplitsIsSet(boolean value) {
    __isset_bit_vector.set(__NUMSPLITS_ISSET_ID, value);
  }

  public long getId() {
    return this.id;
  }

  public TGetSplits setId(long id) {
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

    case START:
      if (value == null) {
        unsetStart();
      } else {
        setStart((ByteBuffer)value);
      }
      break;

    case STOP:
      if (value == null) {
        unsetStop();
      } else {
        setStop((ByteBuffer)value);
      }
      break;

    case COLUMNS:
      if (value == null) {
        unsetColumns();
      } else {
        setColumns((List<ByteBuffer>)value);
      }
      break;

    case NUM_SPLITS:
      if (value == null) {
        unsetNumSplits();
      } else {
        setNumSplits((Integer)value);
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

    case START:
      return getStart();

    case STOP:
      return getStop();

    case COLUMNS:
      return getColumns();

    case NUM_SPLITS:
      return Integer.valueOf(getNumSplits());

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
    case START:
      return isSetStart();
    case STOP:
      return isSetStop();
    case COLUMNS:
      return isSetColumns();
    case NUM_SPLITS:
      return isSetNumSplits();
    case ID:
      return isSetId();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TGetSplits)
      return this.equals((TGetSplits)that);
    return false;
  }

  public boolean equals(TGetSplits that) {
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

    boolean this_present_start = true && this.isSetStart();
    boolean that_present_start = true && that.isSetStart();
    if (this_present_start || that_present_start) {
      if (!(this_present_start && that_present_start))
        return false;
      if (!this.start.equals(that.start))
        return false;
    }

    boolean this_present_stop = true && this.isSetStop();
    boolean that_present_stop = true && that.isSetStop();
    if (this_present_stop || that_present_stop) {
      if (!(this_present_stop && that_present_stop))
        return false;
      if (!this.stop.equals(that.stop))
        return false;
    }

    boolean this_present_columns = true && this.isSetColumns();
    boolean that_present_columns = true && that.isSetColumns();
    if (this_present_columns || that_present_columns) {
      if (!(this_present_columns && that_present_columns))
        return false;
      if (!this.columns.equals(that.columns))
        return false;
    }

    boolean this_present_numSplits = true;
    boolean that_present_numSplits = true;
    if (this_present_numSplits || that_present_numSplits) {
      if (!(this_present_numSplits && that_present_numSplits))
        return false;
      if (this.numSplits != that.numSplits)
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

  public int compareTo(TGetSplits other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TGetSplits typedOther = (TGetSplits)other;

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
    lastComparison = Boolean.valueOf(isSetStart()).compareTo(typedOther.isSetStart());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStart()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.start, typedOther.start);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStop()).compareTo(typedOther.isSetStop());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStop()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stop, typedOther.stop);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetColumns()).compareTo(typedOther.isSetColumns());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumns()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.columns, typedOther.columns);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetNumSplits()).compareTo(typedOther.isSetNumSplits());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNumSplits()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.numSplits, typedOther.numSplits);
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
    StringBuilder sb = new StringBuilder("TGetSplits(");
    boolean first = true;

    sb.append("table:");
    if (this.table == null) {
      sb.append("null");
    } else {
      sb.append(this.table);
    }
    first = false;
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
    if (isSetStart()) {
      if (!first) sb.append(", ");
      sb.append("start:");
      if (this.start == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.start, sb);
      }
      first = false;
    }
    if (isSetStop()) {
      if (!first) sb.append(", ");
      sb.append("stop:");
      if (this.stop == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.stop, sb);
      }
      first = false;
    }
    if (isSetColumns()) {
      if (!first) sb.append(", ");
      sb.append("columns:");
      if (this.columns == null) {
        sb.append("null");
      } else {
        sb.append(this.columns);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("numSplits:");
    sb.append(this.numSplits);
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

  private static class TGetSplitsStandardSchemeFactory implements SchemeFactory {
    public TGetSplitsStandardScheme getScheme() {
      return new TGetSplitsStandardScheme();
    }
  }

  private static class TGetSplitsStandardScheme extends StandardScheme<TGetSplits> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TGetSplits struct) throws org.apache.thrift.TException {
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
          case 3: // START
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.start = iprot.readBinary();
              struct.setStartIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // STOP
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.stop = iprot.readBinary();
              struct.setStopIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // COLUMNS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.columns = new ArrayList<ByteBuffer>(_list8.size);
                for (int _i9 = 0; _i9 < _list8.size; ++_i9)
                {
                  ByteBuffer _elem10; // required
                  _elem10 = iprot.readBinary();
                  struct.columns.add(_elem10);
                }
                iprot.readListEnd();
              }
              struct.setColumnsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // NUM_SPLITS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.numSplits = iprot.readI32();
              struct.setNumSplitsIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TGetSplits struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.table != null) {
        oprot.writeFieldBegin(TABLE_FIELD_DESC);
        oprot.writeString(struct.table);
        oprot.writeFieldEnd();
      }
      if (struct.metric != null) {
        if (struct.isSetMetric()) {
          oprot.writeFieldBegin(METRIC_FIELD_DESC);
          oprot.writeString(struct.metric);
          oprot.writeFieldEnd();
        }
      }
      if (struct.start != null) {
        if (struct.isSetStart()) {
          oprot.writeFieldBegin(START_FIELD_DESC);
          oprot.writeBinary(struct.start);
          oprot.writeFieldEnd();
        }
      }
      if (struct.stop != null) {
        if (struct.isSetStop()) {
          oprot.writeFieldBegin(STOP_FIELD_DESC);
          oprot.writeBinary(struct.stop);
          oprot.writeFieldEnd();
        }
      }
      if (struct.columns != null) {
        if (struct.isSetColumns()) {
          oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.columns.size()));
            for (ByteBuffer _iter11 : struct.columns)
            {
              oprot.writeBinary(_iter11);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldBegin(NUM_SPLITS_FIELD_DESC);
      oprot.writeI32(struct.numSplits);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI64(struct.id);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TGetSplitsTupleSchemeFactory implements SchemeFactory {
    public TGetSplitsTupleScheme getScheme() {
      return new TGetSplitsTupleScheme();
    }
  }

  private static class TGetSplitsTupleScheme extends TupleScheme<TGetSplits> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TGetSplits struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetTable()) {
        optionals.set(0);
      }
      if (struct.isSetMetric()) {
        optionals.set(1);
      }
      if (struct.isSetStart()) {
        optionals.set(2);
      }
      if (struct.isSetStop()) {
        optionals.set(3);
      }
      if (struct.isSetColumns()) {
        optionals.set(4);
      }
      if (struct.isSetNumSplits()) {
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
      if (struct.isSetStart()) {
        oprot.writeBinary(struct.start);
      }
      if (struct.isSetStop()) {
        oprot.writeBinary(struct.stop);
      }
      if (struct.isSetColumns()) {
        {
          oprot.writeI32(struct.columns.size());
          for (ByteBuffer _iter12 : struct.columns)
          {
            oprot.writeBinary(_iter12);
          }
        }
      }
      if (struct.isSetNumSplits()) {
        oprot.writeI32(struct.numSplits);
      }
      if (struct.isSetId()) {
        oprot.writeI64(struct.id);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TGetSplits struct) throws org.apache.thrift.TException {
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
        struct.start = iprot.readBinary();
        struct.setStartIsSet(true);
      }
      if (incoming.get(3)) {
        struct.stop = iprot.readBinary();
        struct.setStopIsSet(true);
      }
      if (incoming.get(4)) {
        {
          org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.columns = new ArrayList<ByteBuffer>(_list13.size);
          for (int _i14 = 0; _i14 < _list13.size; ++_i14)
          {
            ByteBuffer _elem15; // required
            _elem15 = iprot.readBinary();
            struct.columns.add(_elem15);
          }
        }
        struct.setColumnsIsSet(true);
      }
      if (incoming.get(5)) {
        struct.numSplits = iprot.readI32();
        struct.setNumSplitsIsSet(true);
      }
      if (incoming.get(6)) {
        struct.id = iprot.readI64();
        struct.setIdIsSet(true);
      }
    }
  }

}
