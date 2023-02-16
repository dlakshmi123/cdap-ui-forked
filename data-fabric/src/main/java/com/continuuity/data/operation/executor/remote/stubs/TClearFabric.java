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

public class TClearFabric implements org.apache.thrift.TBase<TClearFabric, TClearFabric._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TClearFabric");

  private static final org.apache.thrift.protocol.TField CLEAR_DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("clearData", org.apache.thrift.protocol.TType.BOOL, (short)1);
  private static final org.apache.thrift.protocol.TField CLEAR_META_FIELD_DESC = new org.apache.thrift.protocol.TField("clearMeta", org.apache.thrift.protocol.TType.BOOL, (short)2);
  private static final org.apache.thrift.protocol.TField CLEAR_TABLES_FIELD_DESC = new org.apache.thrift.protocol.TField("clearTables", org.apache.thrift.protocol.TType.BOOL, (short)3);
  private static final org.apache.thrift.protocol.TField CLEAR_QUEUES_FIELD_DESC = new org.apache.thrift.protocol.TField("clearQueues", org.apache.thrift.protocol.TType.BOOL, (short)4);
  private static final org.apache.thrift.protocol.TField CLEAR_STREAMS_FIELD_DESC = new org.apache.thrift.protocol.TField("clearStreams", org.apache.thrift.protocol.TType.BOOL, (short)5);
  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I64, (short)6);
  private static final org.apache.thrift.protocol.TField METRIC_FIELD_DESC = new org.apache.thrift.protocol.TField("metric", org.apache.thrift.protocol.TType.STRING, (short)7);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TClearFabricStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TClearFabricTupleSchemeFactory());
  }

  public boolean clearData; // required
  public boolean clearMeta; // required
  public boolean clearTables; // required
  public boolean clearQueues; // required
  public boolean clearStreams; // required
  public long id; // required
  public String metric; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CLEAR_DATA((short)1, "clearData"),
    CLEAR_META((short)2, "clearMeta"),
    CLEAR_TABLES((short)3, "clearTables"),
    CLEAR_QUEUES((short)4, "clearQueues"),
    CLEAR_STREAMS((short)5, "clearStreams"),
    ID((short)6, "id"),
    METRIC((short)7, "metric");

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
        case 1: // CLEAR_DATA
          return CLEAR_DATA;
        case 2: // CLEAR_META
          return CLEAR_META;
        case 3: // CLEAR_TABLES
          return CLEAR_TABLES;
        case 4: // CLEAR_QUEUES
          return CLEAR_QUEUES;
        case 5: // CLEAR_STREAMS
          return CLEAR_STREAMS;
        case 6: // ID
          return ID;
        case 7: // METRIC
          return METRIC;
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
  private static final int __CLEARDATA_ISSET_ID = 0;
  private static final int __CLEARMETA_ISSET_ID = 1;
  private static final int __CLEARTABLES_ISSET_ID = 2;
  private static final int __CLEARQUEUES_ISSET_ID = 3;
  private static final int __CLEARSTREAMS_ISSET_ID = 4;
  private static final int __ID_ISSET_ID = 5;
  private BitSet __isset_bit_vector = new BitSet(6);
  private _Fields optionals[] = {_Fields.METRIC};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CLEAR_DATA, new org.apache.thrift.meta_data.FieldMetaData("clearData", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.CLEAR_META, new org.apache.thrift.meta_data.FieldMetaData("clearMeta", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.CLEAR_TABLES, new org.apache.thrift.meta_data.FieldMetaData("clearTables", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.CLEAR_QUEUES, new org.apache.thrift.meta_data.FieldMetaData("clearQueues", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.CLEAR_STREAMS, new org.apache.thrift.meta_data.FieldMetaData("clearStreams", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.METRIC, new org.apache.thrift.meta_data.FieldMetaData("metric", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TClearFabric.class, metaDataMap);
  }

  public TClearFabric() {
  }

  public TClearFabric(
    boolean clearData,
    boolean clearMeta,
    boolean clearTables,
    boolean clearQueues,
    boolean clearStreams,
    long id)
  {
    this();
    this.clearData = clearData;
    setClearDataIsSet(true);
    this.clearMeta = clearMeta;
    setClearMetaIsSet(true);
    this.clearTables = clearTables;
    setClearTablesIsSet(true);
    this.clearQueues = clearQueues;
    setClearQueuesIsSet(true);
    this.clearStreams = clearStreams;
    setClearStreamsIsSet(true);
    this.id = id;
    setIdIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TClearFabric(TClearFabric other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.clearData = other.clearData;
    this.clearMeta = other.clearMeta;
    this.clearTables = other.clearTables;
    this.clearQueues = other.clearQueues;
    this.clearStreams = other.clearStreams;
    this.id = other.id;
    if (other.isSetMetric()) {
      this.metric = other.metric;
    }
  }

  public TClearFabric deepCopy() {
    return new TClearFabric(this);
  }

  @Override
  public void clear() {
    setClearDataIsSet(false);
    this.clearData = false;
    setClearMetaIsSet(false);
    this.clearMeta = false;
    setClearTablesIsSet(false);
    this.clearTables = false;
    setClearQueuesIsSet(false);
    this.clearQueues = false;
    setClearStreamsIsSet(false);
    this.clearStreams = false;
    setIdIsSet(false);
    this.id = 0;
    this.metric = null;
  }

  public boolean isClearData() {
    return this.clearData;
  }

  public TClearFabric setClearData(boolean clearData) {
    this.clearData = clearData;
    setClearDataIsSet(true);
    return this;
  }

  public void unsetClearData() {
    __isset_bit_vector.clear(__CLEARDATA_ISSET_ID);
  }

  /** Returns true if field clearData is set (has been assigned a value) and false otherwise */
  public boolean isSetClearData() {
    return __isset_bit_vector.get(__CLEARDATA_ISSET_ID);
  }

  public void setClearDataIsSet(boolean value) {
    __isset_bit_vector.set(__CLEARDATA_ISSET_ID, value);
  }

  public boolean isClearMeta() {
    return this.clearMeta;
  }

  public TClearFabric setClearMeta(boolean clearMeta) {
    this.clearMeta = clearMeta;
    setClearMetaIsSet(true);
    return this;
  }

  public void unsetClearMeta() {
    __isset_bit_vector.clear(__CLEARMETA_ISSET_ID);
  }

  /** Returns true if field clearMeta is set (has been assigned a value) and false otherwise */
  public boolean isSetClearMeta() {
    return __isset_bit_vector.get(__CLEARMETA_ISSET_ID);
  }

  public void setClearMetaIsSet(boolean value) {
    __isset_bit_vector.set(__CLEARMETA_ISSET_ID, value);
  }

  public boolean isClearTables() {
    return this.clearTables;
  }

  public TClearFabric setClearTables(boolean clearTables) {
    this.clearTables = clearTables;
    setClearTablesIsSet(true);
    return this;
  }

  public void unsetClearTables() {
    __isset_bit_vector.clear(__CLEARTABLES_ISSET_ID);
  }

  /** Returns true if field clearTables is set (has been assigned a value) and false otherwise */
  public boolean isSetClearTables() {
    return __isset_bit_vector.get(__CLEARTABLES_ISSET_ID);
  }

  public void setClearTablesIsSet(boolean value) {
    __isset_bit_vector.set(__CLEARTABLES_ISSET_ID, value);
  }

  public boolean isClearQueues() {
    return this.clearQueues;
  }

  public TClearFabric setClearQueues(boolean clearQueues) {
    this.clearQueues = clearQueues;
    setClearQueuesIsSet(true);
    return this;
  }

  public void unsetClearQueues() {
    __isset_bit_vector.clear(__CLEARQUEUES_ISSET_ID);
  }

  /** Returns true if field clearQueues is set (has been assigned a value) and false otherwise */
  public boolean isSetClearQueues() {
    return __isset_bit_vector.get(__CLEARQUEUES_ISSET_ID);
  }

  public void setClearQueuesIsSet(boolean value) {
    __isset_bit_vector.set(__CLEARQUEUES_ISSET_ID, value);
  }

  public boolean isClearStreams() {
    return this.clearStreams;
  }

  public TClearFabric setClearStreams(boolean clearStreams) {
    this.clearStreams = clearStreams;
    setClearStreamsIsSet(true);
    return this;
  }

  public void unsetClearStreams() {
    __isset_bit_vector.clear(__CLEARSTREAMS_ISSET_ID);
  }

  /** Returns true if field clearStreams is set (has been assigned a value) and false otherwise */
  public boolean isSetClearStreams() {
    return __isset_bit_vector.get(__CLEARSTREAMS_ISSET_ID);
  }

  public void setClearStreamsIsSet(boolean value) {
    __isset_bit_vector.set(__CLEARSTREAMS_ISSET_ID, value);
  }

  public long getId() {
    return this.id;
  }

  public TClearFabric setId(long id) {
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

  public String getMetric() {
    return this.metric;
  }

  public TClearFabric setMetric(String metric) {
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

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CLEAR_DATA:
      if (value == null) {
        unsetClearData();
      } else {
        setClearData((Boolean)value);
      }
      break;

    case CLEAR_META:
      if (value == null) {
        unsetClearMeta();
      } else {
        setClearMeta((Boolean)value);
      }
      break;

    case CLEAR_TABLES:
      if (value == null) {
        unsetClearTables();
      } else {
        setClearTables((Boolean)value);
      }
      break;

    case CLEAR_QUEUES:
      if (value == null) {
        unsetClearQueues();
      } else {
        setClearQueues((Boolean)value);
      }
      break;

    case CLEAR_STREAMS:
      if (value == null) {
        unsetClearStreams();
      } else {
        setClearStreams((Boolean)value);
      }
      break;

    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Long)value);
      }
      break;

    case METRIC:
      if (value == null) {
        unsetMetric();
      } else {
        setMetric((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CLEAR_DATA:
      return Boolean.valueOf(isClearData());

    case CLEAR_META:
      return Boolean.valueOf(isClearMeta());

    case CLEAR_TABLES:
      return Boolean.valueOf(isClearTables());

    case CLEAR_QUEUES:
      return Boolean.valueOf(isClearQueues());

    case CLEAR_STREAMS:
      return Boolean.valueOf(isClearStreams());

    case ID:
      return Long.valueOf(getId());

    case METRIC:
      return getMetric();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CLEAR_DATA:
      return isSetClearData();
    case CLEAR_META:
      return isSetClearMeta();
    case CLEAR_TABLES:
      return isSetClearTables();
    case CLEAR_QUEUES:
      return isSetClearQueues();
    case CLEAR_STREAMS:
      return isSetClearStreams();
    case ID:
      return isSetId();
    case METRIC:
      return isSetMetric();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TClearFabric)
      return this.equals((TClearFabric)that);
    return false;
  }

  public boolean equals(TClearFabric that) {
    if (that == null)
      return false;

    boolean this_present_clearData = true;
    boolean that_present_clearData = true;
    if (this_present_clearData || that_present_clearData) {
      if (!(this_present_clearData && that_present_clearData))
        return false;
      if (this.clearData != that.clearData)
        return false;
    }

    boolean this_present_clearMeta = true;
    boolean that_present_clearMeta = true;
    if (this_present_clearMeta || that_present_clearMeta) {
      if (!(this_present_clearMeta && that_present_clearMeta))
        return false;
      if (this.clearMeta != that.clearMeta)
        return false;
    }

    boolean this_present_clearTables = true;
    boolean that_present_clearTables = true;
    if (this_present_clearTables || that_present_clearTables) {
      if (!(this_present_clearTables && that_present_clearTables))
        return false;
      if (this.clearTables != that.clearTables)
        return false;
    }

    boolean this_present_clearQueues = true;
    boolean that_present_clearQueues = true;
    if (this_present_clearQueues || that_present_clearQueues) {
      if (!(this_present_clearQueues && that_present_clearQueues))
        return false;
      if (this.clearQueues != that.clearQueues)
        return false;
    }

    boolean this_present_clearStreams = true;
    boolean that_present_clearStreams = true;
    if (this_present_clearStreams || that_present_clearStreams) {
      if (!(this_present_clearStreams && that_present_clearStreams))
        return false;
      if (this.clearStreams != that.clearStreams)
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

    boolean this_present_metric = true && this.isSetMetric();
    boolean that_present_metric = true && that.isSetMetric();
    if (this_present_metric || that_present_metric) {
      if (!(this_present_metric && that_present_metric))
        return false;
      if (!this.metric.equals(that.metric))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(TClearFabric other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TClearFabric typedOther = (TClearFabric)other;

    lastComparison = Boolean.valueOf(isSetClearData()).compareTo(typedOther.isSetClearData());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClearData()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clearData, typedOther.clearData);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetClearMeta()).compareTo(typedOther.isSetClearMeta());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClearMeta()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clearMeta, typedOther.clearMeta);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetClearTables()).compareTo(typedOther.isSetClearTables());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClearTables()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clearTables, typedOther.clearTables);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetClearQueues()).compareTo(typedOther.isSetClearQueues());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClearQueues()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clearQueues, typedOther.clearQueues);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetClearStreams()).compareTo(typedOther.isSetClearStreams());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetClearStreams()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.clearStreams, typedOther.clearStreams);
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
    StringBuilder sb = new StringBuilder("TClearFabric(");
    boolean first = true;

    sb.append("clearData:");
    sb.append(this.clearData);
    first = false;
    if (!first) sb.append(", ");
    sb.append("clearMeta:");
    sb.append(this.clearMeta);
    first = false;
    if (!first) sb.append(", ");
    sb.append("clearTables:");
    sb.append(this.clearTables);
    first = false;
    if (!first) sb.append(", ");
    sb.append("clearQueues:");
    sb.append(this.clearQueues);
    first = false;
    if (!first) sb.append(", ");
    sb.append("clearStreams:");
    sb.append(this.clearStreams);
    first = false;
    if (!first) sb.append(", ");
    sb.append("id:");
    sb.append(this.id);
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

  private static class TClearFabricStandardSchemeFactory implements SchemeFactory {
    public TClearFabricStandardScheme getScheme() {
      return new TClearFabricStandardScheme();
    }
  }

  private static class TClearFabricStandardScheme extends StandardScheme<TClearFabric> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TClearFabric struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CLEAR_DATA
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.clearData = iprot.readBool();
              struct.setClearDataIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CLEAR_META
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.clearMeta = iprot.readBool();
              struct.setClearMetaIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CLEAR_TABLES
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.clearTables = iprot.readBool();
              struct.setClearTablesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // CLEAR_QUEUES
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.clearQueues = iprot.readBool();
              struct.setClearQueuesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // CLEAR_STREAMS
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.clearStreams = iprot.readBool();
              struct.setClearStreamsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.id = iprot.readI64();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // METRIC
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.metric = iprot.readString();
              struct.setMetricIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TClearFabric struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(CLEAR_DATA_FIELD_DESC);
      oprot.writeBool(struct.clearData);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(CLEAR_META_FIELD_DESC);
      oprot.writeBool(struct.clearMeta);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(CLEAR_TABLES_FIELD_DESC);
      oprot.writeBool(struct.clearTables);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(CLEAR_QUEUES_FIELD_DESC);
      oprot.writeBool(struct.clearQueues);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(CLEAR_STREAMS_FIELD_DESC);
      oprot.writeBool(struct.clearStreams);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI64(struct.id);
      oprot.writeFieldEnd();
      if (struct.metric != null) {
        if (struct.isSetMetric()) {
          oprot.writeFieldBegin(METRIC_FIELD_DESC);
          oprot.writeString(struct.metric);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TClearFabricTupleSchemeFactory implements SchemeFactory {
    public TClearFabricTupleScheme getScheme() {
      return new TClearFabricTupleScheme();
    }
  }

  private static class TClearFabricTupleScheme extends TupleScheme<TClearFabric> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TClearFabric struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetClearData()) {
        optionals.set(0);
      }
      if (struct.isSetClearMeta()) {
        optionals.set(1);
      }
      if (struct.isSetClearTables()) {
        optionals.set(2);
      }
      if (struct.isSetClearQueues()) {
        optionals.set(3);
      }
      if (struct.isSetClearStreams()) {
        optionals.set(4);
      }
      if (struct.isSetId()) {
        optionals.set(5);
      }
      if (struct.isSetMetric()) {
        optionals.set(6);
      }
      oprot.writeBitSet(optionals, 7);
      if (struct.isSetClearData()) {
        oprot.writeBool(struct.clearData);
      }
      if (struct.isSetClearMeta()) {
        oprot.writeBool(struct.clearMeta);
      }
      if (struct.isSetClearTables()) {
        oprot.writeBool(struct.clearTables);
      }
      if (struct.isSetClearQueues()) {
        oprot.writeBool(struct.clearQueues);
      }
      if (struct.isSetClearStreams()) {
        oprot.writeBool(struct.clearStreams);
      }
      if (struct.isSetId()) {
        oprot.writeI64(struct.id);
      }
      if (struct.isSetMetric()) {
        oprot.writeString(struct.metric);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TClearFabric struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(7);
      if (incoming.get(0)) {
        struct.clearData = iprot.readBool();
        struct.setClearDataIsSet(true);
      }
      if (incoming.get(1)) {
        struct.clearMeta = iprot.readBool();
        struct.setClearMetaIsSet(true);
      }
      if (incoming.get(2)) {
        struct.clearTables = iprot.readBool();
        struct.setClearTablesIsSet(true);
      }
      if (incoming.get(3)) {
        struct.clearQueues = iprot.readBool();
        struct.setClearQueuesIsSet(true);
      }
      if (incoming.get(4)) {
        struct.clearStreams = iprot.readBool();
        struct.setClearStreamsIsSet(true);
      }
      if (incoming.get(5)) {
        struct.id = iprot.readI64();
        struct.setIdIsSet(true);
      }
      if (incoming.get(6)) {
        struct.metric = iprot.readString();
        struct.setMetricIsSet(true);
      }
    }
  }

}
