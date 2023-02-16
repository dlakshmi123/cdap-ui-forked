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

public class TOptionalBinaryList implements org.apache.thrift.TBase<TOptionalBinaryList, TOptionalBinaryList._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TOptionalBinaryList");

  private static final org.apache.thrift.protocol.TField THE_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("theList", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField MESSAGE_FIELD_DESC = new org.apache.thrift.protocol.TField("message", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TOptionalBinaryListStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TOptionalBinaryListTupleSchemeFactory());
  }

  public List<ByteBuffer> theList; // optional
  public int status; // optional
  public String message; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    THE_LIST((short)1, "theList"),
    STATUS((short)2, "status"),
    MESSAGE((short)3, "message");

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
        case 1: // THE_LIST
          return THE_LIST;
        case 2: // STATUS
          return STATUS;
        case 3: // MESSAGE
          return MESSAGE;
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
  private static final int __STATUS_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  private _Fields optionals[] = {_Fields.THE_LIST,_Fields.STATUS,_Fields.MESSAGE};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.THE_LIST, new org.apache.thrift.meta_data.FieldMetaData("theList", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING            , true))));
    tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.MESSAGE, new org.apache.thrift.meta_data.FieldMetaData("message", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TOptionalBinaryList.class, metaDataMap);
  }

  public TOptionalBinaryList() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TOptionalBinaryList(TOptionalBinaryList other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetTheList()) {
      List<ByteBuffer> __this__theList = new ArrayList<ByteBuffer>();
      for (ByteBuffer other_element : other.theList) {
        ByteBuffer temp_binary_element = org.apache.thrift.TBaseHelper.copyBinary(other_element);
;
        __this__theList.add(temp_binary_element);
      }
      this.theList = __this__theList;
    }
    this.status = other.status;
    if (other.isSetMessage()) {
      this.message = other.message;
    }
  }

  public TOptionalBinaryList deepCopy() {
    return new TOptionalBinaryList(this);
  }

  @Override
  public void clear() {
    this.theList = null;
    setStatusIsSet(false);
    this.status = 0;
    this.message = null;
  }

  public int getTheListSize() {
    return (this.theList == null) ? 0 : this.theList.size();
  }

  public java.util.Iterator<ByteBuffer> getTheListIterator() {
    return (this.theList == null) ? null : this.theList.iterator();
  }

  public void addToTheList(ByteBuffer elem) {
    if (this.theList == null) {
      this.theList = new ArrayList<ByteBuffer>();
    }
    this.theList.add(elem);
  }

  public List<ByteBuffer> getTheList() {
    return this.theList;
  }

  public TOptionalBinaryList setTheList(List<ByteBuffer> theList) {
    this.theList = theList;
    return this;
  }

  public void unsetTheList() {
    this.theList = null;
  }

  /** Returns true if field theList is set (has been assigned a value) and false otherwise */
  public boolean isSetTheList() {
    return this.theList != null;
  }

  public void setTheListIsSet(boolean value) {
    if (!value) {
      this.theList = null;
    }
  }

  public int getStatus() {
    return this.status;
  }

  public TOptionalBinaryList setStatus(int status) {
    this.status = status;
    setStatusIsSet(true);
    return this;
  }

  public void unsetStatus() {
    __isset_bit_vector.clear(__STATUS_ISSET_ID);
  }

  /** Returns true if field status is set (has been assigned a value) and false otherwise */
  public boolean isSetStatus() {
    return __isset_bit_vector.get(__STATUS_ISSET_ID);
  }

  public void setStatusIsSet(boolean value) {
    __isset_bit_vector.set(__STATUS_ISSET_ID, value);
  }

  public String getMessage() {
    return this.message;
  }

  public TOptionalBinaryList setMessage(String message) {
    this.message = message;
    return this;
  }

  public void unsetMessage() {
    this.message = null;
  }

  /** Returns true if field message is set (has been assigned a value) and false otherwise */
  public boolean isSetMessage() {
    return this.message != null;
  }

  public void setMessageIsSet(boolean value) {
    if (!value) {
      this.message = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case THE_LIST:
      if (value == null) {
        unsetTheList();
      } else {
        setTheList((List<ByteBuffer>)value);
      }
      break;

    case STATUS:
      if (value == null) {
        unsetStatus();
      } else {
        setStatus((Integer)value);
      }
      break;

    case MESSAGE:
      if (value == null) {
        unsetMessage();
      } else {
        setMessage((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case THE_LIST:
      return getTheList();

    case STATUS:
      return Integer.valueOf(getStatus());

    case MESSAGE:
      return getMessage();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case THE_LIST:
      return isSetTheList();
    case STATUS:
      return isSetStatus();
    case MESSAGE:
      return isSetMessage();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TOptionalBinaryList)
      return this.equals((TOptionalBinaryList)that);
    return false;
  }

  public boolean equals(TOptionalBinaryList that) {
    if (that == null)
      return false;

    boolean this_present_theList = true && this.isSetTheList();
    boolean that_present_theList = true && that.isSetTheList();
    if (this_present_theList || that_present_theList) {
      if (!(this_present_theList && that_present_theList))
        return false;
      if (!this.theList.equals(that.theList))
        return false;
    }

    boolean this_present_status = true && this.isSetStatus();
    boolean that_present_status = true && that.isSetStatus();
    if (this_present_status || that_present_status) {
      if (!(this_present_status && that_present_status))
        return false;
      if (this.status != that.status)
        return false;
    }

    boolean this_present_message = true && this.isSetMessage();
    boolean that_present_message = true && that.isSetMessage();
    if (this_present_message || that_present_message) {
      if (!(this_present_message && that_present_message))
        return false;
      if (!this.message.equals(that.message))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(TOptionalBinaryList other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TOptionalBinaryList typedOther = (TOptionalBinaryList)other;

    lastComparison = Boolean.valueOf(isSetTheList()).compareTo(typedOther.isSetTheList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTheList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.theList, typedOther.theList);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStatus()).compareTo(typedOther.isSetStatus());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStatus()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, typedOther.status);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMessage()).compareTo(typedOther.isSetMessage());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMessage()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.message, typedOther.message);
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
    StringBuilder sb = new StringBuilder("TOptionalBinaryList(");
    boolean first = true;

    if (isSetTheList()) {
      sb.append("theList:");
      if (this.theList == null) {
        sb.append("null");
      } else {
        sb.append(this.theList);
      }
      first = false;
    }
    if (isSetStatus()) {
      if (!first) sb.append(", ");
      sb.append("status:");
      sb.append(this.status);
      first = false;
    }
    if (isSetMessage()) {
      if (!first) sb.append(", ");
      sb.append("message:");
      if (this.message == null) {
        sb.append("null");
      } else {
        sb.append(this.message);
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

  private static class TOptionalBinaryListStandardSchemeFactory implements SchemeFactory {
    public TOptionalBinaryListStandardScheme getScheme() {
      return new TOptionalBinaryListStandardScheme();
    }
  }

  private static class TOptionalBinaryListStandardScheme extends StandardScheme<TOptionalBinaryList> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TOptionalBinaryList struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // THE_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list106 = iprot.readListBegin();
                struct.theList = new ArrayList<ByteBuffer>(_list106.size);
                for (int _i107 = 0; _i107 < _list106.size; ++_i107)
                {
                  ByteBuffer _elem108; // required
                  _elem108 = iprot.readBinary();
                  struct.theList.add(_elem108);
                }
                iprot.readListEnd();
              }
              struct.setTheListIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // STATUS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.status = iprot.readI32();
              struct.setStatusIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // MESSAGE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.message = iprot.readString();
              struct.setMessageIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TOptionalBinaryList struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.theList != null) {
        if (struct.isSetTheList()) {
          oprot.writeFieldBegin(THE_LIST_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.theList.size()));
            for (ByteBuffer _iter109 : struct.theList)
            {
              oprot.writeBinary(_iter109);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetStatus()) {
        oprot.writeFieldBegin(STATUS_FIELD_DESC);
        oprot.writeI32(struct.status);
        oprot.writeFieldEnd();
      }
      if (struct.message != null) {
        if (struct.isSetMessage()) {
          oprot.writeFieldBegin(MESSAGE_FIELD_DESC);
          oprot.writeString(struct.message);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TOptionalBinaryListTupleSchemeFactory implements SchemeFactory {
    public TOptionalBinaryListTupleScheme getScheme() {
      return new TOptionalBinaryListTupleScheme();
    }
  }

  private static class TOptionalBinaryListTupleScheme extends TupleScheme<TOptionalBinaryList> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TOptionalBinaryList struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetTheList()) {
        optionals.set(0);
      }
      if (struct.isSetStatus()) {
        optionals.set(1);
      }
      if (struct.isSetMessage()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetTheList()) {
        {
          oprot.writeI32(struct.theList.size());
          for (ByteBuffer _iter110 : struct.theList)
          {
            oprot.writeBinary(_iter110);
          }
        }
      }
      if (struct.isSetStatus()) {
        oprot.writeI32(struct.status);
      }
      if (struct.isSetMessage()) {
        oprot.writeString(struct.message);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TOptionalBinaryList struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list111 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.theList = new ArrayList<ByteBuffer>(_list111.size);
          for (int _i112 = 0; _i112 < _list111.size; ++_i112)
          {
            ByteBuffer _elem113; // required
            _elem113 = iprot.readBinary();
            struct.theList.add(_elem113);
          }
        }
        struct.setTheListIsSet(true);
      }
      if (incoming.get(1)) {
        struct.status = iprot.readI32();
        struct.setStatusIsSet(true);
      }
      if (incoming.get(2)) {
        struct.message = iprot.readString();
        struct.setMessageIsSet(true);
      }
    }
  }

}

