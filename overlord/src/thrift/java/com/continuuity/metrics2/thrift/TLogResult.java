/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.continuuity.metrics2.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Log result
 */
public class TLogResult implements org.apache.thrift.TBase<TLogResult, TLogResult._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TLogResult");

  private static final org.apache.thrift.protocol.TField LOG_LINE_FIELD_DESC = new org.apache.thrift.protocol.TField("logLine", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField OFFSET_FIELD_DESC = new org.apache.thrift.protocol.TField("offset", org.apache.thrift.protocol.TType.I64, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TLogResultStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TLogResultTupleSchemeFactory());
  }

  private String logLine; // required
  private long offset; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    LOG_LINE((short)1, "logLine"),
    OFFSET((short)2, "offset");

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
        case 1: // LOG_LINE
          return LOG_LINE;
        case 2: // OFFSET
          return OFFSET;
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
  private static final int __OFFSET_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.LOG_LINE, new org.apache.thrift.meta_data.FieldMetaData("logLine", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.OFFSET, new org.apache.thrift.meta_data.FieldMetaData("offset", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TLogResult.class, metaDataMap);
  }

  public TLogResult() {
  }

  public TLogResult(
    String logLine,
    long offset)
  {
    this();
    this.logLine = logLine;
    this.offset = offset;
    setOffsetIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TLogResult(TLogResult other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetLogLine()) {
      this.logLine = other.logLine;
    }
    this.offset = other.offset;
  }

  public TLogResult deepCopy() {
    return new TLogResult(this);
  }

  @Override
  public void clear() {
    this.logLine = null;
    setOffsetIsSet(false);
    this.offset = 0;
  }

  public String getLogLine() {
    return this.logLine;
  }

  public void setLogLine(String logLine) {
    this.logLine = logLine;
  }

  public void unsetLogLine() {
    this.logLine = null;
  }

  /** Returns true if field logLine is set (has been assigned a value) and false otherwise */
  public boolean isSetLogLine() {
    return this.logLine != null;
  }

  public void setLogLineIsSet(boolean value) {
    if (!value) {
      this.logLine = null;
    }
  }

  public long getOffset() {
    return this.offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
    setOffsetIsSet(true);
  }

  public void unsetOffset() {
    __isset_bit_vector.clear(__OFFSET_ISSET_ID);
  }

  /** Returns true if field offset is set (has been assigned a value) and false otherwise */
  public boolean isSetOffset() {
    return __isset_bit_vector.get(__OFFSET_ISSET_ID);
  }

  public void setOffsetIsSet(boolean value) {
    __isset_bit_vector.set(__OFFSET_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case LOG_LINE:
      if (value == null) {
        unsetLogLine();
      } else {
        setLogLine((String)value);
      }
      break;

    case OFFSET:
      if (value == null) {
        unsetOffset();
      } else {
        setOffset((Long)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case LOG_LINE:
      return getLogLine();

    case OFFSET:
      return Long.valueOf(getOffset());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case LOG_LINE:
      return isSetLogLine();
    case OFFSET:
      return isSetOffset();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TLogResult)
      return this.equals((TLogResult)that);
    return false;
  }

  public boolean equals(TLogResult that) {
    if (that == null)
      return false;

    boolean this_present_logLine = true && this.isSetLogLine();
    boolean that_present_logLine = true && that.isSetLogLine();
    if (this_present_logLine || that_present_logLine) {
      if (!(this_present_logLine && that_present_logLine))
        return false;
      if (!this.logLine.equals(that.logLine))
        return false;
    }

    boolean this_present_offset = true;
    boolean that_present_offset = true;
    if (this_present_offset || that_present_offset) {
      if (!(this_present_offset && that_present_offset))
        return false;
      if (this.offset != that.offset)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_logLine = true && (isSetLogLine());
    builder.append(present_logLine);
    if (present_logLine)
      builder.append(logLine);

    boolean present_offset = true;
    builder.append(present_offset);
    if (present_offset)
      builder.append(offset);

    return builder.toHashCode();
  }

  public int compareTo(TLogResult other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TLogResult typedOther = (TLogResult)other;

    lastComparison = Boolean.valueOf(isSetLogLine()).compareTo(typedOther.isSetLogLine());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLogLine()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.logLine, typedOther.logLine);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOffset()).compareTo(typedOther.isSetOffset());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOffset()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.offset, typedOther.offset);
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
    StringBuilder sb = new StringBuilder("TLogResult(");
    boolean first = true;

    sb.append("logLine:");
    if (this.logLine == null) {
      sb.append("null");
    } else {
      sb.append(this.logLine);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("offset:");
    sb.append(this.offset);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (!isSetLogLine()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'logLine' is unset! Struct:" + toString());
    }

    if (!isSetOffset()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'offset' is unset! Struct:" + toString());
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

  private static class TLogResultStandardSchemeFactory implements SchemeFactory {
    public TLogResultStandardScheme getScheme() {
      return new TLogResultStandardScheme();
    }
  }

  private static class TLogResultStandardScheme extends StandardScheme<TLogResult> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TLogResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // LOG_LINE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.logLine = iprot.readString();
              struct.setLogLineIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // OFFSET
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.offset = iprot.readI64();
              struct.setOffsetIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TLogResult struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.logLine != null) {
        oprot.writeFieldBegin(LOG_LINE_FIELD_DESC);
        oprot.writeString(struct.logLine);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(OFFSET_FIELD_DESC);
      oprot.writeI64(struct.offset);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TLogResultTupleSchemeFactory implements SchemeFactory {
    public TLogResultTupleScheme getScheme() {
      return new TLogResultTupleScheme();
    }
  }

  private static class TLogResultTupleScheme extends TupleScheme<TLogResult> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TLogResult struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.logLine);
      oprot.writeI64(struct.offset);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TLogResult struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.logLine = iprot.readString();
      struct.setLogLineIsSet(true);
      struct.offset = iprot.readI64();
      struct.setOffsetIsSet(true);
    }
  }

}
