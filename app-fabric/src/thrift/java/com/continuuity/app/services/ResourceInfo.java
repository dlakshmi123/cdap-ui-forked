/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.continuuity.app.services;

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
 * Information about resource
 */
public class ResourceInfo implements org.apache.thrift.TBase<ResourceInfo, ResourceInfo._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ResourceInfo");

  private static final org.apache.thrift.protocol.TField ACCOUNT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("accountId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField APPLICATION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("applicationId", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField FILENAME_FIELD_DESC = new org.apache.thrift.protocol.TField("filename", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("size", org.apache.thrift.protocol.TType.I32, (short)4);
  private static final org.apache.thrift.protocol.TField MODTIME_FIELD_DESC = new org.apache.thrift.protocol.TField("modtime", org.apache.thrift.protocol.TType.I64, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ResourceInfoStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ResourceInfoTupleSchemeFactory());
  }

  private String accountId; // required
  private String applicationId; // required
  private String filename; // required
  private int size; // required
  private long modtime; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ACCOUNT_ID((short)1, "accountId"),
    APPLICATION_ID((short)2, "applicationId"),
    FILENAME((short)3, "filename"),
    SIZE((short)4, "size"),
    MODTIME((short)5, "modtime");

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
        case 1: // ACCOUNT_ID
          return ACCOUNT_ID;
        case 2: // APPLICATION_ID
          return APPLICATION_ID;
        case 3: // FILENAME
          return FILENAME;
        case 4: // SIZE
          return SIZE;
        case 5: // MODTIME
          return MODTIME;
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
  private static final int __SIZE_ISSET_ID = 0;
  private static final int __MODTIME_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ACCOUNT_ID, new org.apache.thrift.meta_data.FieldMetaData("accountId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.APPLICATION_ID, new org.apache.thrift.meta_data.FieldMetaData("applicationId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FILENAME, new org.apache.thrift.meta_data.FieldMetaData("filename", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.SIZE, new org.apache.thrift.meta_data.FieldMetaData("size", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.MODTIME, new org.apache.thrift.meta_data.FieldMetaData("modtime", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ResourceInfo.class, metaDataMap);
  }

  public ResourceInfo() {
  }

  public ResourceInfo(
    String accountId,
    String applicationId,
    String filename,
    int size,
    long modtime)
  {
    this();
    this.accountId = accountId;
    this.applicationId = applicationId;
    this.filename = filename;
    this.size = size;
    setSizeIsSet(true);
    this.modtime = modtime;
    setModtimeIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ResourceInfo(ResourceInfo other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetAccountId()) {
      this.accountId = other.accountId;
    }
    if (other.isSetApplicationId()) {
      this.applicationId = other.applicationId;
    }
    if (other.isSetFilename()) {
      this.filename = other.filename;
    }
    this.size = other.size;
    this.modtime = other.modtime;
  }

  public ResourceInfo deepCopy() {
    return new ResourceInfo(this);
  }

  @Override
  public void clear() {
    this.accountId = null;
    this.applicationId = null;
    this.filename = null;
    setSizeIsSet(false);
    this.size = 0;
    setModtimeIsSet(false);
    this.modtime = 0;
  }

  public String getAccountId() {
    return this.accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public void unsetAccountId() {
    this.accountId = null;
  }

  /** Returns true if field accountId is set (has been assigned a value) and false otherwise */
  public boolean isSetAccountId() {
    return this.accountId != null;
  }

  public void setAccountIdIsSet(boolean value) {
    if (!value) {
      this.accountId = null;
    }
  }

  public String getApplicationId() {
    return this.applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public void unsetApplicationId() {
    this.applicationId = null;
  }

  /** Returns true if field applicationId is set (has been assigned a value) and false otherwise */
  public boolean isSetApplicationId() {
    return this.applicationId != null;
  }

  public void setApplicationIdIsSet(boolean value) {
    if (!value) {
      this.applicationId = null;
    }
  }

  public String getFilename() {
    return this.filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public void unsetFilename() {
    this.filename = null;
  }

  /** Returns true if field filename is set (has been assigned a value) and false otherwise */
  public boolean isSetFilename() {
    return this.filename != null;
  }

  public void setFilenameIsSet(boolean value) {
    if (!value) {
      this.filename = null;
    }
  }

  public int getSize() {
    return this.size;
  }

  public void setSize(int size) {
    this.size = size;
    setSizeIsSet(true);
  }

  public void unsetSize() {
    __isset_bit_vector.clear(__SIZE_ISSET_ID);
  }

  /** Returns true if field size is set (has been assigned a value) and false otherwise */
  public boolean isSetSize() {
    return __isset_bit_vector.get(__SIZE_ISSET_ID);
  }

  public void setSizeIsSet(boolean value) {
    __isset_bit_vector.set(__SIZE_ISSET_ID, value);
  }

  public long getModtime() {
    return this.modtime;
  }

  public void setModtime(long modtime) {
    this.modtime = modtime;
    setModtimeIsSet(true);
  }

  public void unsetModtime() {
    __isset_bit_vector.clear(__MODTIME_ISSET_ID);
  }

  /** Returns true if field modtime is set (has been assigned a value) and false otherwise */
  public boolean isSetModtime() {
    return __isset_bit_vector.get(__MODTIME_ISSET_ID);
  }

  public void setModtimeIsSet(boolean value) {
    __isset_bit_vector.set(__MODTIME_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ACCOUNT_ID:
      if (value == null) {
        unsetAccountId();
      } else {
        setAccountId((String)value);
      }
      break;

    case APPLICATION_ID:
      if (value == null) {
        unsetApplicationId();
      } else {
        setApplicationId((String)value);
      }
      break;

    case FILENAME:
      if (value == null) {
        unsetFilename();
      } else {
        setFilename((String)value);
      }
      break;

    case SIZE:
      if (value == null) {
        unsetSize();
      } else {
        setSize((Integer)value);
      }
      break;

    case MODTIME:
      if (value == null) {
        unsetModtime();
      } else {
        setModtime((Long)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ACCOUNT_ID:
      return getAccountId();

    case APPLICATION_ID:
      return getApplicationId();

    case FILENAME:
      return getFilename();

    case SIZE:
      return Integer.valueOf(getSize());

    case MODTIME:
      return Long.valueOf(getModtime());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ACCOUNT_ID:
      return isSetAccountId();
    case APPLICATION_ID:
      return isSetApplicationId();
    case FILENAME:
      return isSetFilename();
    case SIZE:
      return isSetSize();
    case MODTIME:
      return isSetModtime();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ResourceInfo)
      return this.equals((ResourceInfo)that);
    return false;
  }

  public boolean equals(ResourceInfo that) {
    if (that == null)
      return false;

    boolean this_present_accountId = true && this.isSetAccountId();
    boolean that_present_accountId = true && that.isSetAccountId();
    if (this_present_accountId || that_present_accountId) {
      if (!(this_present_accountId && that_present_accountId))
        return false;
      if (!this.accountId.equals(that.accountId))
        return false;
    }

    boolean this_present_applicationId = true && this.isSetApplicationId();
    boolean that_present_applicationId = true && that.isSetApplicationId();
    if (this_present_applicationId || that_present_applicationId) {
      if (!(this_present_applicationId && that_present_applicationId))
        return false;
      if (!this.applicationId.equals(that.applicationId))
        return false;
    }

    boolean this_present_filename = true && this.isSetFilename();
    boolean that_present_filename = true && that.isSetFilename();
    if (this_present_filename || that_present_filename) {
      if (!(this_present_filename && that_present_filename))
        return false;
      if (!this.filename.equals(that.filename))
        return false;
    }

    boolean this_present_size = true;
    boolean that_present_size = true;
    if (this_present_size || that_present_size) {
      if (!(this_present_size && that_present_size))
        return false;
      if (this.size != that.size)
        return false;
    }

    boolean this_present_modtime = true;
    boolean that_present_modtime = true;
    if (this_present_modtime || that_present_modtime) {
      if (!(this_present_modtime && that_present_modtime))
        return false;
      if (this.modtime != that.modtime)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_accountId = true && (isSetAccountId());
    builder.append(present_accountId);
    if (present_accountId)
      builder.append(accountId);

    boolean present_applicationId = true && (isSetApplicationId());
    builder.append(present_applicationId);
    if (present_applicationId)
      builder.append(applicationId);

    boolean present_filename = true && (isSetFilename());
    builder.append(present_filename);
    if (present_filename)
      builder.append(filename);

    boolean present_size = true;
    builder.append(present_size);
    if (present_size)
      builder.append(size);

    boolean present_modtime = true;
    builder.append(present_modtime);
    if (present_modtime)
      builder.append(modtime);

    return builder.toHashCode();
  }

  public int compareTo(ResourceInfo other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    ResourceInfo typedOther = (ResourceInfo)other;

    lastComparison = Boolean.valueOf(isSetAccountId()).compareTo(typedOther.isSetAccountId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAccountId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.accountId, typedOther.accountId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetApplicationId()).compareTo(typedOther.isSetApplicationId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetApplicationId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.applicationId, typedOther.applicationId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFilename()).compareTo(typedOther.isSetFilename());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFilename()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.filename, typedOther.filename);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSize()).compareTo(typedOther.isSetSize());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSize()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.size, typedOther.size);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetModtime()).compareTo(typedOther.isSetModtime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetModtime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.modtime, typedOther.modtime);
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
    StringBuilder sb = new StringBuilder("ResourceInfo(");
    boolean first = true;

    sb.append("accountId:");
    if (this.accountId == null) {
      sb.append("null");
    } else {
      sb.append(this.accountId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("applicationId:");
    if (this.applicationId == null) {
      sb.append("null");
    } else {
      sb.append(this.applicationId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("filename:");
    if (this.filename == null) {
      sb.append("null");
    } else {
      sb.append(this.filename);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("size:");
    sb.append(this.size);
    first = false;
    if (!first) sb.append(", ");
    sb.append("modtime:");
    sb.append(this.modtime);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (!isSetAccountId()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'accountId' is unset! Struct:" + toString());
    }

    if (!isSetApplicationId()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'applicationId' is unset! Struct:" + toString());
    }

    if (!isSetFilename()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'filename' is unset! Struct:" + toString());
    }

    if (!isSetSize()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'size' is unset! Struct:" + toString());
    }

    if (!isSetModtime()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'modtime' is unset! Struct:" + toString());
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

  private static class ResourceInfoStandardSchemeFactory implements SchemeFactory {
    public ResourceInfoStandardScheme getScheme() {
      return new ResourceInfoStandardScheme();
    }
  }

  private static class ResourceInfoStandardScheme extends StandardScheme<ResourceInfo> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ResourceInfo struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ACCOUNT_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.accountId = iprot.readString();
              struct.setAccountIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // APPLICATION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.applicationId = iprot.readString();
              struct.setApplicationIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // FILENAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.filename = iprot.readString();
              struct.setFilenameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // SIZE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.size = iprot.readI32();
              struct.setSizeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // MODTIME
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.modtime = iprot.readI64();
              struct.setModtimeIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ResourceInfo struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.accountId != null) {
        oprot.writeFieldBegin(ACCOUNT_ID_FIELD_DESC);
        oprot.writeString(struct.accountId);
        oprot.writeFieldEnd();
      }
      if (struct.applicationId != null) {
        oprot.writeFieldBegin(APPLICATION_ID_FIELD_DESC);
        oprot.writeString(struct.applicationId);
        oprot.writeFieldEnd();
      }
      if (struct.filename != null) {
        oprot.writeFieldBegin(FILENAME_FIELD_DESC);
        oprot.writeString(struct.filename);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(SIZE_FIELD_DESC);
      oprot.writeI32(struct.size);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(MODTIME_FIELD_DESC);
      oprot.writeI64(struct.modtime);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ResourceInfoTupleSchemeFactory implements SchemeFactory {
    public ResourceInfoTupleScheme getScheme() {
      return new ResourceInfoTupleScheme();
    }
  }

  private static class ResourceInfoTupleScheme extends TupleScheme<ResourceInfo> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ResourceInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.accountId);
      oprot.writeString(struct.applicationId);
      oprot.writeString(struct.filename);
      oprot.writeI32(struct.size);
      oprot.writeI64(struct.modtime);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ResourceInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.accountId = iprot.readString();
      struct.setAccountIdIsSet(true);
      struct.applicationId = iprot.readString();
      struct.setApplicationIdIsSet(true);
      struct.filename = iprot.readString();
      struct.setFilenameIsSet(true);
      struct.size = iprot.readI32();
      struct.setSizeIsSet(true);
      struct.modtime = iprot.readI64();
      struct.setModtimeIsSet(true);
    }
  }

}
