/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.continuuity.metrics2.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
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

/**
 * Timeseries request
 */
public class TimeseriesRequest implements org.apache.thrift.TBase<TimeseriesRequest, TimeseriesRequest._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TimeseriesRequest");

  private static final org.apache.thrift.protocol.TField ARGUMENT_FIELD_DESC = new org.apache.thrift.protocol.TField("argument", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField METRICS_FIELD_DESC = new org.apache.thrift.protocol.TField("metrics", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField LEVEL_FIELD_DESC = new org.apache.thrift.protocol.TField("level", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField STARTTS_FIELD_DESC = new org.apache.thrift.protocol.TField("startts", org.apache.thrift.protocol.TType.I64, (short)4);
  private static final org.apache.thrift.protocol.TField ENDTS_FIELD_DESC = new org.apache.thrift.protocol.TField("endts", org.apache.thrift.protocol.TType.I64, (short)5);
  private static final org.apache.thrift.protocol.TField SUMMARY_FIELD_DESC = new org.apache.thrift.protocol.TField("summary", org.apache.thrift.protocol.TType.BOOL, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TimeseriesRequestStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TimeseriesRequestTupleSchemeFactory());
  }

  private FlowArgument argument; // required
  private List<String> metrics; // required
  private MetricTimeseriesLevel level; // optional
  private long startts; // required
  private long endts; // optional
  private boolean summary; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ARGUMENT((short)1, "argument"),
    METRICS((short)2, "metrics"),
    /**
     * 
     * @see MetricTimeseriesLevel
     */
    LEVEL((short)3, "level"),
    STARTTS((short)4, "startts"),
    ENDTS((short)5, "endts"),
    SUMMARY((short)6, "summary");

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
        case 1: // ARGUMENT
          return ARGUMENT;
        case 2: // METRICS
          return METRICS;
        case 3: // LEVEL
          return LEVEL;
        case 4: // STARTTS
          return STARTTS;
        case 5: // ENDTS
          return ENDTS;
        case 6: // SUMMARY
          return SUMMARY;
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
  private static final int __STARTTS_ISSET_ID = 0;
  private static final int __ENDTS_ISSET_ID = 1;
  private static final int __SUMMARY_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);
  private _Fields optionals[] = {_Fields.LEVEL,_Fields.ENDTS,_Fields.SUMMARY};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ARGUMENT, new org.apache.thrift.meta_data.FieldMetaData("argument", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, FlowArgument.class)));
    tmpMap.put(_Fields.METRICS, new org.apache.thrift.meta_data.FieldMetaData("metrics", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.LEVEL, new org.apache.thrift.meta_data.FieldMetaData("level", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, MetricTimeseriesLevel.class)));
    tmpMap.put(_Fields.STARTTS, new org.apache.thrift.meta_data.FieldMetaData("startts", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.ENDTS, new org.apache.thrift.meta_data.FieldMetaData("endts", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.SUMMARY, new org.apache.thrift.meta_data.FieldMetaData("summary", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TimeseriesRequest.class, metaDataMap);
  }

  public TimeseriesRequest() {
    this.summary = true;

  }

  public TimeseriesRequest(
    FlowArgument argument,
    List<String> metrics,
    long startts)
  {
    this();
    this.argument = argument;
    this.metrics = metrics;
    this.startts = startts;
    setStarttsIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TimeseriesRequest(TimeseriesRequest other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetArgument()) {
      this.argument = new FlowArgument(other.argument);
    }
    if (other.isSetMetrics()) {
      List<String> __this__metrics = new ArrayList<String>();
      for (String other_element : other.metrics) {
        __this__metrics.add(other_element);
      }
      this.metrics = __this__metrics;
    }
    if (other.isSetLevel()) {
      this.level = other.level;
    }
    this.startts = other.startts;
    this.endts = other.endts;
    this.summary = other.summary;
  }

  public TimeseriesRequest deepCopy() {
    return new TimeseriesRequest(this);
  }

  @Override
  public void clear() {
    this.argument = null;
    this.metrics = null;
    this.level = null;
    setStarttsIsSet(false);
    this.startts = 0;
    setEndtsIsSet(false);
    this.endts = 0;
    this.summary = true;

  }

  public FlowArgument getArgument() {
    return this.argument;
  }

  public void setArgument(FlowArgument argument) {
    this.argument = argument;
  }

  public void unsetArgument() {
    this.argument = null;
  }

  /** Returns true if field argument is set (has been assigned a value) and false otherwise */
  public boolean isSetArgument() {
    return this.argument != null;
  }

  public void setArgumentIsSet(boolean value) {
    if (!value) {
      this.argument = null;
    }
  }

  public int getMetricsSize() {
    return (this.metrics == null) ? 0 : this.metrics.size();
  }

  public java.util.Iterator<String> getMetricsIterator() {
    return (this.metrics == null) ? null : this.metrics.iterator();
  }

  public void addToMetrics(String elem) {
    if (this.metrics == null) {
      this.metrics = new ArrayList<String>();
    }
    this.metrics.add(elem);
  }

  public List<String> getMetrics() {
    return this.metrics;
  }

  public void setMetrics(List<String> metrics) {
    this.metrics = metrics;
  }

  public void unsetMetrics() {
    this.metrics = null;
  }

  /** Returns true if field metrics is set (has been assigned a value) and false otherwise */
  public boolean isSetMetrics() {
    return this.metrics != null;
  }

  public void setMetricsIsSet(boolean value) {
    if (!value) {
      this.metrics = null;
    }
  }

  /**
   * 
   * @see MetricTimeseriesLevel
   */
  public MetricTimeseriesLevel getLevel() {
    return this.level;
  }

  /**
   * 
   * @see MetricTimeseriesLevel
   */
  public void setLevel(MetricTimeseriesLevel level) {
    this.level = level;
  }

  public void unsetLevel() {
    this.level = null;
  }

  /** Returns true if field level is set (has been assigned a value) and false otherwise */
  public boolean isSetLevel() {
    return this.level != null;
  }

  public void setLevelIsSet(boolean value) {
    if (!value) {
      this.level = null;
    }
  }

  public long getStartts() {
    return this.startts;
  }

  public void setStartts(long startts) {
    this.startts = startts;
    setStarttsIsSet(true);
  }

  public void unsetStartts() {
    __isset_bit_vector.clear(__STARTTS_ISSET_ID);
  }

  /** Returns true if field startts is set (has been assigned a value) and false otherwise */
  public boolean isSetStartts() {
    return __isset_bit_vector.get(__STARTTS_ISSET_ID);
  }

  public void setStarttsIsSet(boolean value) {
    __isset_bit_vector.set(__STARTTS_ISSET_ID, value);
  }

  public long getEndts() {
    return this.endts;
  }

  public void setEndts(long endts) {
    this.endts = endts;
    setEndtsIsSet(true);
  }

  public void unsetEndts() {
    __isset_bit_vector.clear(__ENDTS_ISSET_ID);
  }

  /** Returns true if field endts is set (has been assigned a value) and false otherwise */
  public boolean isSetEndts() {
    return __isset_bit_vector.get(__ENDTS_ISSET_ID);
  }

  public void setEndtsIsSet(boolean value) {
    __isset_bit_vector.set(__ENDTS_ISSET_ID, value);
  }

  public boolean isSummary() {
    return this.summary;
  }

  public void setSummary(boolean summary) {
    this.summary = summary;
    setSummaryIsSet(true);
  }

  public void unsetSummary() {
    __isset_bit_vector.clear(__SUMMARY_ISSET_ID);
  }

  /** Returns true if field summary is set (has been assigned a value) and false otherwise */
  public boolean isSetSummary() {
    return __isset_bit_vector.get(__SUMMARY_ISSET_ID);
  }

  public void setSummaryIsSet(boolean value) {
    __isset_bit_vector.set(__SUMMARY_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ARGUMENT:
      if (value == null) {
        unsetArgument();
      } else {
        setArgument((FlowArgument)value);
      }
      break;

    case METRICS:
      if (value == null) {
        unsetMetrics();
      } else {
        setMetrics((List<String>)value);
      }
      break;

    case LEVEL:
      if (value == null) {
        unsetLevel();
      } else {
        setLevel((MetricTimeseriesLevel)value);
      }
      break;

    case STARTTS:
      if (value == null) {
        unsetStartts();
      } else {
        setStartts((Long)value);
      }
      break;

    case ENDTS:
      if (value == null) {
        unsetEndts();
      } else {
        setEndts((Long)value);
      }
      break;

    case SUMMARY:
      if (value == null) {
        unsetSummary();
      } else {
        setSummary((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ARGUMENT:
      return getArgument();

    case METRICS:
      return getMetrics();

    case LEVEL:
      return getLevel();

    case STARTTS:
      return Long.valueOf(getStartts());

    case ENDTS:
      return Long.valueOf(getEndts());

    case SUMMARY:
      return Boolean.valueOf(isSummary());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ARGUMENT:
      return isSetArgument();
    case METRICS:
      return isSetMetrics();
    case LEVEL:
      return isSetLevel();
    case STARTTS:
      return isSetStartts();
    case ENDTS:
      return isSetEndts();
    case SUMMARY:
      return isSetSummary();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TimeseriesRequest)
      return this.equals((TimeseriesRequest)that);
    return false;
  }

  public boolean equals(TimeseriesRequest that) {
    if (that == null)
      return false;

    boolean this_present_argument = true && this.isSetArgument();
    boolean that_present_argument = true && that.isSetArgument();
    if (this_present_argument || that_present_argument) {
      if (!(this_present_argument && that_present_argument))
        return false;
      if (!this.argument.equals(that.argument))
        return false;
    }

    boolean this_present_metrics = true && this.isSetMetrics();
    boolean that_present_metrics = true && that.isSetMetrics();
    if (this_present_metrics || that_present_metrics) {
      if (!(this_present_metrics && that_present_metrics))
        return false;
      if (!this.metrics.equals(that.metrics))
        return false;
    }

    boolean this_present_level = true && this.isSetLevel();
    boolean that_present_level = true && that.isSetLevel();
    if (this_present_level || that_present_level) {
      if (!(this_present_level && that_present_level))
        return false;
      if (!this.level.equals(that.level))
        return false;
    }

    boolean this_present_startts = true;
    boolean that_present_startts = true;
    if (this_present_startts || that_present_startts) {
      if (!(this_present_startts && that_present_startts))
        return false;
      if (this.startts != that.startts)
        return false;
    }

    boolean this_present_endts = true && this.isSetEndts();
    boolean that_present_endts = true && that.isSetEndts();
    if (this_present_endts || that_present_endts) {
      if (!(this_present_endts && that_present_endts))
        return false;
      if (this.endts != that.endts)
        return false;
    }

    boolean this_present_summary = true && this.isSetSummary();
    boolean that_present_summary = true && that.isSetSummary();
    if (this_present_summary || that_present_summary) {
      if (!(this_present_summary && that_present_summary))
        return false;
      if (this.summary != that.summary)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_argument = true && (isSetArgument());
    builder.append(present_argument);
    if (present_argument)
      builder.append(argument);

    boolean present_metrics = true && (isSetMetrics());
    builder.append(present_metrics);
    if (present_metrics)
      builder.append(metrics);

    boolean present_level = true && (isSetLevel());
    builder.append(present_level);
    if (present_level)
      builder.append(level.getValue());

    boolean present_startts = true;
    builder.append(present_startts);
    if (present_startts)
      builder.append(startts);

    boolean present_endts = true && (isSetEndts());
    builder.append(present_endts);
    if (present_endts)
      builder.append(endts);

    boolean present_summary = true && (isSetSummary());
    builder.append(present_summary);
    if (present_summary)
      builder.append(summary);

    return builder.toHashCode();
  }

  public int compareTo(TimeseriesRequest other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TimeseriesRequest typedOther = (TimeseriesRequest)other;

    lastComparison = Boolean.valueOf(isSetArgument()).compareTo(typedOther.isSetArgument());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetArgument()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.argument, typedOther.argument);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMetrics()).compareTo(typedOther.isSetMetrics());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMetrics()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.metrics, typedOther.metrics);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLevel()).compareTo(typedOther.isSetLevel());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLevel()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.level, typedOther.level);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStartts()).compareTo(typedOther.isSetStartts());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartts()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startts, typedOther.startts);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetEndts()).compareTo(typedOther.isSetEndts());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEndts()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.endts, typedOther.endts);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSummary()).compareTo(typedOther.isSetSummary());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSummary()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.summary, typedOther.summary);
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
    StringBuilder sb = new StringBuilder("TimeseriesRequest(");
    boolean first = true;

    sb.append("argument:");
    if (this.argument == null) {
      sb.append("null");
    } else {
      sb.append(this.argument);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("metrics:");
    if (this.metrics == null) {
      sb.append("null");
    } else {
      sb.append(this.metrics);
    }
    first = false;
    if (isSetLevel()) {
      if (!first) sb.append(", ");
      sb.append("level:");
      if (this.level == null) {
        sb.append("null");
      } else {
        sb.append(this.level);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("startts:");
    sb.append(this.startts);
    first = false;
    if (isSetEndts()) {
      if (!first) sb.append(", ");
      sb.append("endts:");
      sb.append(this.endts);
      first = false;
    }
    if (isSetSummary()) {
      if (!first) sb.append(", ");
      sb.append("summary:");
      sb.append(this.summary);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (!isSetArgument()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'argument' is unset! Struct:" + toString());
    }

    if (!isSetMetrics()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'metrics' is unset! Struct:" + toString());
    }

    if (!isSetStartts()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'startts' is unset! Struct:" + toString());
    }

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

  private static class TimeseriesRequestStandardSchemeFactory implements SchemeFactory {
    public TimeseriesRequestStandardScheme getScheme() {
      return new TimeseriesRequestStandardScheme();
    }
  }

  private static class TimeseriesRequestStandardScheme extends StandardScheme<TimeseriesRequest> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TimeseriesRequest struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ARGUMENT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.argument = new FlowArgument();
              struct.argument.read(iprot);
              struct.setArgumentIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // METRICS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list36 = iprot.readListBegin();
                struct.metrics = new ArrayList<String>(_list36.size);
                for (int _i37 = 0; _i37 < _list36.size; ++_i37)
                {
                  String _elem38; // required
                  _elem38 = iprot.readString();
                  struct.metrics.add(_elem38);
                }
                iprot.readListEnd();
              }
              struct.setMetricsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // LEVEL
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.level = MetricTimeseriesLevel.findByValue(iprot.readI32());
              struct.setLevelIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // STARTTS
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.startts = iprot.readI64();
              struct.setStarttsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // ENDTS
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.endts = iprot.readI64();
              struct.setEndtsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // SUMMARY
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.summary = iprot.readBool();
              struct.setSummaryIsSet(true);
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
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TimeseriesRequest struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.argument != null) {
        oprot.writeFieldBegin(ARGUMENT_FIELD_DESC);
        struct.argument.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.metrics != null) {
        oprot.writeFieldBegin(METRICS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.metrics.size()));
          for (String _iter39 : struct.metrics)
          {
            oprot.writeString(_iter39);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.level != null) {
        if (struct.isSetLevel()) {
          oprot.writeFieldBegin(LEVEL_FIELD_DESC);
          oprot.writeI32(struct.level.getValue());
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldBegin(STARTTS_FIELD_DESC);
      oprot.writeI64(struct.startts);
      oprot.writeFieldEnd();
      if (struct.isSetEndts()) {
        oprot.writeFieldBegin(ENDTS_FIELD_DESC);
        oprot.writeI64(struct.endts);
        oprot.writeFieldEnd();
      }
      if (struct.isSetSummary()) {
        oprot.writeFieldBegin(SUMMARY_FIELD_DESC);
        oprot.writeBool(struct.summary);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TimeseriesRequestTupleSchemeFactory implements SchemeFactory {
    public TimeseriesRequestTupleScheme getScheme() {
      return new TimeseriesRequestTupleScheme();
    }
  }

  private static class TimeseriesRequestTupleScheme extends TupleScheme<TimeseriesRequest> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TimeseriesRequest struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      struct.argument.write(oprot);
      {
        oprot.writeI32(struct.metrics.size());
        for (String _iter40 : struct.metrics)
        {
          oprot.writeString(_iter40);
        }
      }
      oprot.writeI64(struct.startts);
      BitSet optionals = new BitSet();
      if (struct.isSetLevel()) {
        optionals.set(0);
      }
      if (struct.isSetEndts()) {
        optionals.set(1);
      }
      if (struct.isSetSummary()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetLevel()) {
        oprot.writeI32(struct.level.getValue());
      }
      if (struct.isSetEndts()) {
        oprot.writeI64(struct.endts);
      }
      if (struct.isSetSummary()) {
        oprot.writeBool(struct.summary);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TimeseriesRequest struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.argument = new FlowArgument();
      struct.argument.read(iprot);
      struct.setArgumentIsSet(true);
      {
        org.apache.thrift.protocol.TList _list41 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
        struct.metrics = new ArrayList<String>(_list41.size);
        for (int _i42 = 0; _i42 < _list41.size; ++_i42)
        {
          String _elem43; // required
          _elem43 = iprot.readString();
          struct.metrics.add(_elem43);
        }
      }
      struct.setMetricsIsSet(true);
      struct.startts = iprot.readI64();
      struct.setStarttsIsSet(true);
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.level = MetricTimeseriesLevel.findByValue(iprot.readI32());
        struct.setLevelIsSet(true);
      }
      if (incoming.get(1)) {
        struct.endts = iprot.readI64();
        struct.setEndtsIsSet(true);
      }
      if (incoming.get(2)) {
        struct.summary = iprot.readBool();
        struct.setSummaryIsSet(true);
      }
    }
  }

}
