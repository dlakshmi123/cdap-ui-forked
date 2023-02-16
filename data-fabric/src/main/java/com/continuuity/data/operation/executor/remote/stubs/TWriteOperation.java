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

public class TWriteOperation extends org.apache.thrift.TUnion<TWriteOperation, TWriteOperation._Fields> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TWriteOperation");
  private static final org.apache.thrift.protocol.TField WRITE_FIELD_DESC = new org.apache.thrift.protocol.TField("write", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField DELET_FIELD_DESC = new org.apache.thrift.protocol.TField("delet", org.apache.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.thrift.protocol.TField INCREMENT_FIELD_DESC = new org.apache.thrift.protocol.TField("increment", org.apache.thrift.protocol.TType.STRUCT, (short)3);
  private static final org.apache.thrift.protocol.TField COMPARE_AND_SWAP_FIELD_DESC = new org.apache.thrift.protocol.TField("compareAndSwap", org.apache.thrift.protocol.TType.STRUCT, (short)4);
  private static final org.apache.thrift.protocol.TField QUEUE_ENQUEUE_FIELD_DESC = new org.apache.thrift.protocol.TField("queueEnqueue", org.apache.thrift.protocol.TType.STRUCT, (short)5);
  private static final org.apache.thrift.protocol.TField QUEUE_ACK_FIELD_DESC = new org.apache.thrift.protocol.TField("queueAck", org.apache.thrift.protocol.TType.STRUCT, (short)6);

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    WRITE((short)1, "write"),
    DELET((short)2, "delet"),
    INCREMENT((short)3, "increment"),
    COMPARE_AND_SWAP((short)4, "compareAndSwap"),
    QUEUE_ENQUEUE((short)5, "queueEnqueue"),
    QUEUE_ACK((short)6, "queueAck");

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
        case 1: // WRITE
          return WRITE;
        case 2: // DELET
          return DELET;
        case 3: // INCREMENT
          return INCREMENT;
        case 4: // COMPARE_AND_SWAP
          return COMPARE_AND_SWAP;
        case 5: // QUEUE_ENQUEUE
          return QUEUE_ENQUEUE;
        case 6: // QUEUE_ACK
          return QUEUE_ACK;
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

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.WRITE, new org.apache.thrift.meta_data.FieldMetaData("write", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TWrite.class)));
    tmpMap.put(_Fields.DELET, new org.apache.thrift.meta_data.FieldMetaData("delet", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TDelete.class)));
    tmpMap.put(_Fields.INCREMENT, new org.apache.thrift.meta_data.FieldMetaData("increment", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TIncrement.class)));
    tmpMap.put(_Fields.COMPARE_AND_SWAP, new org.apache.thrift.meta_data.FieldMetaData("compareAndSwap", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TCompareAndSwap.class)));
    tmpMap.put(_Fields.QUEUE_ENQUEUE, new org.apache.thrift.meta_data.FieldMetaData("queueEnqueue", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TQueueEnqueue.class)));
    tmpMap.put(_Fields.QUEUE_ACK, new org.apache.thrift.meta_data.FieldMetaData("queueAck", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TQueueAck.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TWriteOperation.class, metaDataMap);
  }

  public TWriteOperation() {
    super();
  }

  public TWriteOperation(_Fields setField, Object value) {
    super(setField, value);
  }

  public TWriteOperation(TWriteOperation other) {
    super(other);
  }
  public TWriteOperation deepCopy() {
    return new TWriteOperation(this);
  }

  public static TWriteOperation write(TWrite value) {
    TWriteOperation x = new TWriteOperation();
    x.setWrite(value);
    return x;
  }

  public static TWriteOperation delet(TDelete value) {
    TWriteOperation x = new TWriteOperation();
    x.setDelet(value);
    return x;
  }

  public static TWriteOperation increment(TIncrement value) {
    TWriteOperation x = new TWriteOperation();
    x.setIncrement(value);
    return x;
  }

  public static TWriteOperation compareAndSwap(TCompareAndSwap value) {
    TWriteOperation x = new TWriteOperation();
    x.setCompareAndSwap(value);
    return x;
  }

  public static TWriteOperation queueEnqueue(TQueueEnqueue value) {
    TWriteOperation x = new TWriteOperation();
    x.setQueueEnqueue(value);
    return x;
  }

  public static TWriteOperation queueAck(TQueueAck value) {
    TWriteOperation x = new TWriteOperation();
    x.setQueueAck(value);
    return x;
  }


  @Override
  protected void checkType(_Fields setField, Object value) throws ClassCastException {
    switch (setField) {
      case WRITE:
        if (value instanceof TWrite) {
          break;
        }
        throw new ClassCastException("Was expecting value of type TWrite for field 'write', but got " + value.getClass().getSimpleName());
      case DELET:
        if (value instanceof TDelete) {
          break;
        }
        throw new ClassCastException("Was expecting value of type TDelete for field 'delet', but got " + value.getClass().getSimpleName());
      case INCREMENT:
        if (value instanceof TIncrement) {
          break;
        }
        throw new ClassCastException("Was expecting value of type TIncrement for field 'increment', but got " + value.getClass().getSimpleName());
      case COMPARE_AND_SWAP:
        if (value instanceof TCompareAndSwap) {
          break;
        }
        throw new ClassCastException("Was expecting value of type TCompareAndSwap for field 'compareAndSwap', but got " + value.getClass().getSimpleName());
      case QUEUE_ENQUEUE:
        if (value instanceof TQueueEnqueue) {
          break;
        }
        throw new ClassCastException("Was expecting value of type TQueueEnqueue for field 'queueEnqueue', but got " + value.getClass().getSimpleName());
      case QUEUE_ACK:
        if (value instanceof TQueueAck) {
          break;
        }
        throw new ClassCastException("Was expecting value of type TQueueAck for field 'queueAck', but got " + value.getClass().getSimpleName());
      default:
        throw new IllegalArgumentException("Unknown field id " + setField);
    }
  }

  @Override
  protected Object standardSchemeReadValue(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TField field) throws org.apache.thrift.TException {
    _Fields setField = _Fields.findByThriftId(field.id);
    if (setField != null) {
      switch (setField) {
        case WRITE:
          if (field.type == WRITE_FIELD_DESC.type) {
            TWrite write;
            write = new TWrite();
            write.read(iprot);
            return write;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case DELET:
          if (field.type == DELET_FIELD_DESC.type) {
            TDelete delet;
            delet = new TDelete();
            delet.read(iprot);
            return delet;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case INCREMENT:
          if (field.type == INCREMENT_FIELD_DESC.type) {
            TIncrement increment;
            increment = new TIncrement();
            increment.read(iprot);
            return increment;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case COMPARE_AND_SWAP:
          if (field.type == COMPARE_AND_SWAP_FIELD_DESC.type) {
            TCompareAndSwap compareAndSwap;
            compareAndSwap = new TCompareAndSwap();
            compareAndSwap.read(iprot);
            return compareAndSwap;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case QUEUE_ENQUEUE:
          if (field.type == QUEUE_ENQUEUE_FIELD_DESC.type) {
            TQueueEnqueue queueEnqueue;
            queueEnqueue = new TQueueEnqueue();
            queueEnqueue.read(iprot);
            return queueEnqueue;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case QUEUE_ACK:
          if (field.type == QUEUE_ACK_FIELD_DESC.type) {
            TQueueAck queueAck;
            queueAck = new TQueueAck();
            queueAck.read(iprot);
            return queueAck;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        default:
          throw new IllegalStateException("setField wasn't null, but didn't match any of the case statements!");
      }
    } else {
      return null;
    }
  }

  @Override
  protected void standardSchemeWriteValue(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    switch (setField_) {
      case WRITE:
        TWrite write = (TWrite)value_;
        write.write(oprot);
        return;
      case DELET:
        TDelete delet = (TDelete)value_;
        delet.write(oprot);
        return;
      case INCREMENT:
        TIncrement increment = (TIncrement)value_;
        increment.write(oprot);
        return;
      case COMPARE_AND_SWAP:
        TCompareAndSwap compareAndSwap = (TCompareAndSwap)value_;
        compareAndSwap.write(oprot);
        return;
      case QUEUE_ENQUEUE:
        TQueueEnqueue queueEnqueue = (TQueueEnqueue)value_;
        queueEnqueue.write(oprot);
        return;
      case QUEUE_ACK:
        TQueueAck queueAck = (TQueueAck)value_;
        queueAck.write(oprot);
        return;
      default:
        throw new IllegalStateException("Cannot write union with unknown field " + setField_);
    }
  }

  @Override
  protected Object tupleSchemeReadValue(org.apache.thrift.protocol.TProtocol iprot, short fieldID) throws org.apache.thrift.TException {
    _Fields setField = _Fields.findByThriftId(fieldID);
    if (setField != null) {
      switch (setField) {
        case WRITE:
          TWrite write;
          write = new TWrite();
          write.read(iprot);
          return write;
        case DELET:
          TDelete delet;
          delet = new TDelete();
          delet.read(iprot);
          return delet;
        case INCREMENT:
          TIncrement increment;
          increment = new TIncrement();
          increment.read(iprot);
          return increment;
        case COMPARE_AND_SWAP:
          TCompareAndSwap compareAndSwap;
          compareAndSwap = new TCompareAndSwap();
          compareAndSwap.read(iprot);
          return compareAndSwap;
        case QUEUE_ENQUEUE:
          TQueueEnqueue queueEnqueue;
          queueEnqueue = new TQueueEnqueue();
          queueEnqueue.read(iprot);
          return queueEnqueue;
        case QUEUE_ACK:
          TQueueAck queueAck;
          queueAck = new TQueueAck();
          queueAck.read(iprot);
          return queueAck;
        default:
          throw new IllegalStateException("setField wasn't null, but didn't match any of the case statements!");
      }
    } else {
      return null;
    }
  }

  @Override
  protected void tupleSchemeWriteValue(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    switch (setField_) {
      case WRITE:
        TWrite write = (TWrite)value_;
        write.write(oprot);
        return;
      case DELET:
        TDelete delet = (TDelete)value_;
        delet.write(oprot);
        return;
      case INCREMENT:
        TIncrement increment = (TIncrement)value_;
        increment.write(oprot);
        return;
      case COMPARE_AND_SWAP:
        TCompareAndSwap compareAndSwap = (TCompareAndSwap)value_;
        compareAndSwap.write(oprot);
        return;
      case QUEUE_ENQUEUE:
        TQueueEnqueue queueEnqueue = (TQueueEnqueue)value_;
        queueEnqueue.write(oprot);
        return;
      case QUEUE_ACK:
        TQueueAck queueAck = (TQueueAck)value_;
        queueAck.write(oprot);
        return;
      default:
        throw new IllegalStateException("Cannot write union with unknown field " + setField_);
    }
  }

  @Override
  protected org.apache.thrift.protocol.TField getFieldDesc(_Fields setField) {
    switch (setField) {
      case WRITE:
        return WRITE_FIELD_DESC;
      case DELET:
        return DELET_FIELD_DESC;
      case INCREMENT:
        return INCREMENT_FIELD_DESC;
      case COMPARE_AND_SWAP:
        return COMPARE_AND_SWAP_FIELD_DESC;
      case QUEUE_ENQUEUE:
        return QUEUE_ENQUEUE_FIELD_DESC;
      case QUEUE_ACK:
        return QUEUE_ACK_FIELD_DESC;
      default:
        throw new IllegalArgumentException("Unknown field id " + setField);
    }
  }

  @Override
  protected org.apache.thrift.protocol.TStruct getStructDesc() {
    return STRUCT_DESC;
  }

  @Override
  protected _Fields enumForId(short id) {
    return _Fields.findByThriftIdOrThrow(id);
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }


  public TWrite getWrite() {
    if (getSetField() == _Fields.WRITE) {
      return (TWrite)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'write' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setWrite(TWrite value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.WRITE;
    value_ = value;
  }

  public TDelete getDelet() {
    if (getSetField() == _Fields.DELET) {
      return (TDelete)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'delet' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setDelet(TDelete value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.DELET;
    value_ = value;
  }

  public TIncrement getIncrement() {
    if (getSetField() == _Fields.INCREMENT) {
      return (TIncrement)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'increment' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setIncrement(TIncrement value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.INCREMENT;
    value_ = value;
  }

  public TCompareAndSwap getCompareAndSwap() {
    if (getSetField() == _Fields.COMPARE_AND_SWAP) {
      return (TCompareAndSwap)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'compareAndSwap' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setCompareAndSwap(TCompareAndSwap value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.COMPARE_AND_SWAP;
    value_ = value;
  }

  public TQueueEnqueue getQueueEnqueue() {
    if (getSetField() == _Fields.QUEUE_ENQUEUE) {
      return (TQueueEnqueue)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'queueEnqueue' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setQueueEnqueue(TQueueEnqueue value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.QUEUE_ENQUEUE;
    value_ = value;
  }

  public TQueueAck getQueueAck() {
    if (getSetField() == _Fields.QUEUE_ACK) {
      return (TQueueAck)getFieldValue();
    } else {
      throw new RuntimeException("Cannot get field 'queueAck' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setQueueAck(TQueueAck value) {
    if (value == null) throw new NullPointerException();
    setField_ = _Fields.QUEUE_ACK;
    value_ = value;
  }

  public boolean isSetWrite() {
    return setField_ == _Fields.WRITE;
  }


  public boolean isSetDelet() {
    return setField_ == _Fields.DELET;
  }


  public boolean isSetIncrement() {
    return setField_ == _Fields.INCREMENT;
  }


  public boolean isSetCompareAndSwap() {
    return setField_ == _Fields.COMPARE_AND_SWAP;
  }


  public boolean isSetQueueEnqueue() {
    return setField_ == _Fields.QUEUE_ENQUEUE;
  }


  public boolean isSetQueueAck() {
    return setField_ == _Fields.QUEUE_ACK;
  }


  public boolean equals(Object other) {
    if (other instanceof TWriteOperation) {
      return equals((TWriteOperation)other);
    } else {
      return false;
    }
  }

  public boolean equals(TWriteOperation other) {
    return other != null && getSetField() == other.getSetField() && getFieldValue().equals(other.getFieldValue());
  }

  @Override
  public int compareTo(TWriteOperation other) {
    int lastComparison = org.apache.thrift.TBaseHelper.compareTo(getSetField(), other.getSetField());
    if (lastComparison == 0) {
      return org.apache.thrift.TBaseHelper.compareTo(getFieldValue(), other.getFieldValue());
    }
    return lastComparison;
  }


  /**
   * If you'd like this to perform more respectably, use the hashcode generator option.
   */
  @Override
  public int hashCode() {
    return 0;
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


}