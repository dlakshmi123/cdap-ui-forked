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
 * Identifies the resource that is being deployed.
 */
public class ArchiveId implements org.apache.thrift.TBase<ArchiveId, ArchiveId._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ArchiveId");

  private static final org.apache.thrift.protocol.TField ACCOUNT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("accountId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField APPLICATION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("applicationId", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField RESOURCE_FIELD_DESC = new org.apache.thrift.protocol.TField("resource", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ArchiveIdStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ArchiveIdTupleSchemeFactory());
  }

  private String accountId; // required
  private String applicationId; // required
  private String resource; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ACCOUNT_ID((short)1, "accountId"),
    APPLICATION_ID((short)2, "applicationId"),
    RESOURCE((short)3, "resource");

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
        case 3: // RESOURCE
          return RESOURCE;
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
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ACCOUNT_ID, new org.apache.thrift.meta_data.FieldMetaData("accountId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.APPLICATION_ID, new org.apache.thrift.meta_data.FieldMetaData("applicationId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.RESOURCE, new org.apache.thrift.meta_data.FieldMetaData("resource", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ArchiveId.class, metaDataMap);
  }

  public ArchiveId() {
  }

  public ArchiveId(
    String accountId,
    String applicationId,
    String resource)
  {
    this();
    this.accountId = accountId;
    this.applicationId = applicationId;
    this.resource = resource;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ArchiveId(ArchiveId other) {
    if (other.isSetAccountId()) {
      this.accountId = other.accountId;
    }
    if (other.isSetApplicationId()) {
      this.applicationId = other.applicationId;
    }
    if (other.isSetResource()) {
      this.resource = other.resource;
    }
  }

  public ArchiveId deepCopy() {
    return new ArchiveId(this);
  }

  @Override
  public void clear() {
    this.accountId = null;
    this.applicationId = null;
    this.resource = null;
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

  public String getResource() {
    return this.resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public void unsetResource() {
    this.resource = null;
  }

  /** Returns true if field resource is set (has been assigned a value) and false otherwise */
  public boolean isSetResource() {
    return this.resource != null;
  }

  public void setResourceIsSet(boolean value) {
    if (!value) {
      this.resource = null;
    }
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

    case RESOURCE:
      if (value == null) {
        unsetResource();
      } else {
        setResource((String)value);
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

    case RESOURCE:
      return getResource();

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
    case RESOURCE:
      return isSetResource();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ArchiveId)
      return this.equals((ArchiveId)that);
    return false;
  }

  public boolean equals(ArchiveId that) {
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

    boolean this_present_resource = true && this.isSetResource();
    boolean that_present_resource = true && that.isSetResource();
    if (this_present_resource || that_present_resource) {
      if (!(this_present_resource && that_present_resource))
        return false;
      if (!this.resource.equals(that.resource))
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

    boolean present_resource = true && (isSetResource());
    builder.append(present_resource);
    if (present_resource)
      builder.append(resource);

    return builder.toHashCode();
  }

  public int compareTo(ArchiveId other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    ArchiveId typedOther = (ArchiveId)other;

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
    lastComparison = Boolean.valueOf(isSetResource()).compareTo(typedOther.isSetResource());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetResource()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.resource, typedOther.resource);
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
    StringBuilder sb = new StringBuilder("ArchiveId(");
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
    sb.append("resource:");
    if (this.resource == null) {
      sb.append("null");
    } else {
      sb.append(this.resource);
    }
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

    if (!isSetResource()) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'resource' is unset! Struct:" + toString());
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
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ArchiveIdStandardSchemeFactory implements SchemeFactory {
    public ArchiveIdStandardScheme getScheme() {
      return new ArchiveIdStandardScheme();
    }
  }

  private static class ArchiveIdStandardScheme extends StandardScheme<ArchiveId> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ArchiveId struct) throws org.apache.thrift.TException {
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
          case 3: // RESOURCE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.resource = iprot.readString();
              struct.setResourceIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ArchiveId struct) throws org.apache.thrift.TException {
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
      if (struct.resource != null) {
        oprot.writeFieldBegin(RESOURCE_FIELD_DESC);
        oprot.writeString(struct.resource);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ArchiveIdTupleSchemeFactory implements SchemeFactory {
    public ArchiveIdTupleScheme getScheme() {
      return new ArchiveIdTupleScheme();
    }
  }

  private static class ArchiveIdTupleScheme extends TupleScheme<ArchiveId> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ArchiveId struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.accountId);
      oprot.writeString(struct.applicationId);
      oprot.writeString(struct.resource);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ArchiveId struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.accountId = iprot.readString();
      struct.setAccountIdIsSet(true);
      struct.applicationId = iprot.readString();
      struct.setApplicationIdIsSet(true);
      struct.resource = iprot.readString();
      struct.setResourceIsSet(true);
    }
  }

}

