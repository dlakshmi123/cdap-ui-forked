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

public class TQueueEntryPointer implements org.apache.thrift.TBase<TQueueEntryPointer, TQueueEntryPointer._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TQueueEntryPointer");

  private static final org.apache.thrift.protocol.TField QUEUE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("queueName", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ENTRY_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("entryId", org.apache.thrift.protocol.TType.I64, (short)2);
  private static final org.apache.thrift.protocol.TField SHARD_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("shardId", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField TRIES_FIELD_DESC = new org.apache.thrift.protocol.TField("tries", org.apache.thrift.protocol.TType.I32, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TQueueEntryPointerStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TQueueEntryPointerTupleSchemeFactory());
  }

  public ByteBuffer queueName; // required
  public long entryId; // required
  public long shardId; // required
  public int tries; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    QUEUE_NAME((short)1, "queueName"),
    ENTRY_ID((short)2, "entryId"),
    SHARD_ID((short)3, "shardId"),
    TRIES((short)4, "tries");

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
        case 1: // QUEUE_NAME
          return QUEUE_NAME;
        case 2: // ENTRY_ID
          return ENTRY_ID;
        case 3: // SHARD_ID
          return SHARD_ID;
        case 4: // TRIES
          return TRIES;
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
  private static final int __ENTRYID_ISSET_ID = 0;
  private static final int __SHARDID_ISSET_ID = 1;
  private static final int __TRIES_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.QUEUE_NAME, new org.apache.thrift.meta_data.FieldMetaData("queueName", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.ENTRY_ID, new org.apache.thrift.meta_data.FieldMetaData("entryId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.SHARD_ID, new org.apache.thrift.meta_data.FieldMetaData("shardId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.TRIES, new org.apache.thrift.meta_data.FieldMetaData("tries", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TQueueEntryPointer.class, metaDataMap);
  }

  public TQueueEntryPointer() {
  }

  public TQueueEntryPointer(
    ByteBuffer queueName,
    long entryId,
    long shardId,
    int tries)
  {
    this();
    this.queueName = queueName;
    this.entryId = entryId;
    setEntryIdIsSet(true);
    this.shardId = shardId;
    setShardIdIsSet(true);
    this.tries = tries;
    setTriesIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TQueueEntryPointer(TQueueEntryPointer other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetQueueName()) {
      this.queueName = org.apache.thrift.TBaseHelper.copyBinary(other.queueName);
;
    }
    this.entryId = other.entryId;
    this.shardId = other.shardId;
    this.tries = other.tries;
  }

  public TQueueEntryPointer deepCopy() {
    return new TQueueEntryPointer(this);
  }

  @Override
  public void clear() {
    this.queueName = null;
    setEntryIdIsSet(false);
    this.entryId = 0;
    setShardIdIsSet(false);
    this.shardId = 0;
    setTriesIsSet(false);
    this.tries = 0;
  }

  public byte[] getQueueName() {
    setQueueName(org.apache.thrift.TBaseHelper.rightSize(queueName));
    return queueName == null ? null : queueName.array();
  }

  public ByteBuffer bufferForQueueName() {
    return queueName;
  }

  public TQueueEntryPointer setQueueName(byte[] queueName) {
    setQueueName(queueName == null ? (ByteBuffer)null : ByteBuffer.wrap(queueName));
    return this;
  }

  public TQueueEntryPointer setQueueName(ByteBuffer queueName) {
    this.queueName = queueName;
    return this;
  }

  public void unsetQueueName() {
    this.queueName = null;
  }

  /** Returns true if field queueName is set (has been assigned a value) and false otherwise */
  public boolean isSetQueueName() {
    return this.queueName != null;
  }

  public void setQueueNameIsSet(boolean value) {
    if (!value) {
      this.queueName = null;
    }
  }

  public long getEntryId() {
    return this.entryId;
  }

  public TQueueEntryPointer setEntryId(long entryId) {
    this.entryId = entryId;
    setEntryIdIsSet(true);
    return this;
  }

  public void unsetEntryId() {
    __isset_bit_vector.clear(__ENTRYID_ISSET_ID);
  }

  /** Returns true if field entryId is set (has been assigned a value) and false otherwise */
  public boolean isSetEntryId() {
    return __isset_bit_vector.get(__ENTRYID_ISSET_ID);
  }

  public void setEntryIdIsSet(boolean value) {
    __isset_bit_vector.set(__ENTRYID_ISSET_ID, value);
  }

  public long getShardId() {
    return this.shardId;
  }

  public TQueueEntryPointer setShardId(long shardId) {
    this.shardId = shardId;
    setShardIdIsSet(true);
    return this;
  }

  public void unsetShardId() {
    __isset_bit_vector.clear(__SHARDID_ISSET_ID);
  }

  /** Returns true if field shardId is set (has been assigned a value) and false otherwise */
  public boolean isSetShardId() {
    return __isset_bit_vector.get(__SHARDID_ISSET_ID);
  }

  public void setShardIdIsSet(boolean value) {
    __isset_bit_vector.set(__SHARDID_ISSET_ID, value);
  }

  public int getTries() {
    return this.tries;
  }

  public TQueueEntryPointer setTries(int tries) {
    this.tries = tries;
    setTriesIsSet(true);
    return this;
  }

  public void unsetTries() {
    __isset_bit_vector.clear(__TRIES_ISSET_ID);
  }

  /** Returns true if field tries is set (has been assigned a value) and false otherwise */
  public boolean isSetTries() {
    return __isset_bit_vector.get(__TRIES_ISSET_ID);
  }

  public void setTriesIsSet(boolean value) {
    __isset_bit_vector.set(__TRIES_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case QUEUE_NAME:
      if (value == null) {
        unsetQueueName();
      } else {
        setQueueName((ByteBuffer)value);
      }
      break;

    case ENTRY_ID:
      if (value == null) {
        unsetEntryId();
      } else {
        setEntryId((Long)value);
      }
      break;

    case SHARD_ID:
      if (value == null) {
        unsetShardId();
      } else {
        setShardId((Long)value);
      }
      break;

    case TRIES:
      if (value == null) {
        unsetTries();
      } else {
        setTries((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case QUEUE_NAME:
      return getQueueName();

    case ENTRY_ID:
      return Long.valueOf(getEntryId());

    case SHARD_ID:
      return Long.valueOf(getShardId());

    case TRIES:
      return Integer.valueOf(getTries());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case QUEUE_NAME:
      return isSetQueueName();
    case ENTRY_ID:
      return isSetEntryId();
    case SHARD_ID:
      return isSetShardId();
    case TRIES:
      return isSetTries();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TQueueEntryPointer)
      return this.equals((TQueueEntryPointer)that);
    return false;
  }

  public boolean equals(TQueueEntryPointer that) {
    if (that == null)
      return false;

    boolean this_present_queueName = true && this.isSetQueueName();
    boolean that_present_queueName = true && that.isSetQueueName();
    if (this_present_queueName || that_present_queueName) {
      if (!(this_present_queueName && that_present_queueName))
        return false;
      if (!this.queueName.equals(that.queueName))
        return false;
    }

    boolean this_present_entryId = true;
    boolean that_present_entryId = true;
    if (this_present_entryId || that_present_entryId) {
      if (!(this_present_entryId && that_present_entryId))
        return false;
      if (this.entryId != that.entryId)
        return false;
    }

    boolean this_present_shardId = true;
    boolean that_present_shardId = true;
    if (this_present_shardId || that_present_shardId) {
      if (!(this_present_shardId && that_present_shardId))
        return false;
      if (this.shardId != that.shardId)
        return false;
    }

    boolean this_present_tries = true;
    boolean that_present_tries = true;
    if (this_present_tries || that_present_tries) {
      if (!(this_present_tries && that_present_tries))
        return false;
      if (this.tries != that.tries)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(TQueueEntryPointer other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TQueueEntryPointer typedOther = (TQueueEntryPointer)other;

    lastComparison = Boolean.valueOf(isSetQueueName()).compareTo(typedOther.isSetQueueName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetQueueName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queueName, typedOther.queueName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetEntryId()).compareTo(typedOther.isSetEntryId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEntryId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.entryId, typedOther.entryId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetShardId()).compareTo(typedOther.isSetShardId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetShardId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.shardId, typedOther.shardId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTries()).compareTo(typedOther.isSetTries());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTries()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tries, typedOther.tries);
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
    StringBuilder sb = new StringBuilder("TQueueEntryPointer(");
    boolean first = true;

    sb.append("queueName:");
    if (this.queueName == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.queueName, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("entryId:");
    sb.append(this.entryId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("shardId:");
    sb.append(this.shardId);
    first = false;
    if (!first) sb.append(", ");
    sb.append("tries:");
    sb.append(this.tries);
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

  private static class TQueueEntryPointerStandardSchemeFactory implements SchemeFactory {
    public TQueueEntryPointerStandardScheme getScheme() {
      return new TQueueEntryPointerStandardScheme();
    }
  }

  private static class TQueueEntryPointerStandardScheme extends StandardScheme<TQueueEntryPointer> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TQueueEntryPointer struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // QUEUE_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.queueName = iprot.readBinary();
              struct.setQueueNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ENTRY_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.entryId = iprot.readI64();
              struct.setEntryIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SHARD_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.shardId = iprot.readI64();
              struct.setShardIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TRIES
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.tries = iprot.readI32();
              struct.setTriesIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TQueueEntryPointer struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.queueName != null) {
        oprot.writeFieldBegin(QUEUE_NAME_FIELD_DESC);
        oprot.writeBinary(struct.queueName);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(ENTRY_ID_FIELD_DESC);
      oprot.writeI64(struct.entryId);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(SHARD_ID_FIELD_DESC);
      oprot.writeI64(struct.shardId);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(TRIES_FIELD_DESC);
      oprot.writeI32(struct.tries);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TQueueEntryPointerTupleSchemeFactory implements SchemeFactory {
    public TQueueEntryPointerTupleScheme getScheme() {
      return new TQueueEntryPointerTupleScheme();
    }
  }

  private static class TQueueEntryPointerTupleScheme extends TupleScheme<TQueueEntryPointer> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TQueueEntryPointer struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetQueueName()) {
        optionals.set(0);
      }
      if (struct.isSetEntryId()) {
        optionals.set(1);
      }
      if (struct.isSetShardId()) {
        optionals.set(2);
      }
      if (struct.isSetTries()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetQueueName()) {
        oprot.writeBinary(struct.queueName);
      }
      if (struct.isSetEntryId()) {
        oprot.writeI64(struct.entryId);
      }
      if (struct.isSetShardId()) {
        oprot.writeI64(struct.shardId);
      }
      if (struct.isSetTries()) {
        oprot.writeI32(struct.tries);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TQueueEntryPointer struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.queueName = iprot.readBinary();
        struct.setQueueNameIsSet(true);
      }
      if (incoming.get(1)) {
        struct.entryId = iprot.readI64();
        struct.setEntryIdIsSet(true);
      }
      if (incoming.get(2)) {
        struct.shardId = iprot.readI64();
        struct.setShardIdIsSet(true);
      }
      if (incoming.get(3)) {
        struct.tries = iprot.readI32();
        struct.setTriesIsSet(true);
      }
    }
  }

}

