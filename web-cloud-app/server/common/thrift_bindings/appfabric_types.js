//
// Autogenerated by Thrift Compiler (0.9.0)
//
// DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
//
var Thrift = require('thrift').Thrift;
var ttypes = module.exports = {};
ttypes.EntityType = {
'FLOW' : 0,
'QUERY' : 1,
'MAPREDUCE' : 2
};
AuthToken = module.exports.AuthToken = function(args) {
  this.token = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
  }
};
AuthToken.prototype = {};
AuthToken.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.token = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

AuthToken.prototype.write = function(output) {
  output.writeStructBegin('AuthToken');
  if (this.token !== null && this.token !== undefined) {
    output.writeFieldBegin('token', Thrift.Type.STRING, 1);
    output.writeString(this.token);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

ResourceIdentifier = module.exports.ResourceIdentifier = function(args) {
  this.accountId = null;
  this.applicationId = null;
  this.resource = null;
  this.version = null;
  if (args) {
    if (args.accountId !== undefined) {
      this.accountId = args.accountId;
    }
    if (args.applicationId !== undefined) {
      this.applicationId = args.applicationId;
    }
    if (args.resource !== undefined) {
      this.resource = args.resource;
    }
    if (args.version !== undefined) {
      this.version = args.version;
    }
  }
};
ResourceIdentifier.prototype = {};
ResourceIdentifier.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.accountId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.applicationId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.STRING) {
        this.resource = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.I32) {
        this.version = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

ResourceIdentifier.prototype.write = function(output) {
  output.writeStructBegin('ResourceIdentifier');
  if (this.accountId !== null && this.accountId !== undefined) {
    output.writeFieldBegin('accountId', Thrift.Type.STRING, 1);
    output.writeString(this.accountId);
    output.writeFieldEnd();
  }
  if (this.applicationId !== null && this.applicationId !== undefined) {
    output.writeFieldBegin('applicationId', Thrift.Type.STRING, 2);
    output.writeString(this.applicationId);
    output.writeFieldEnd();
  }
  if (this.resource !== null && this.resource !== undefined) {
    output.writeFieldBegin('resource', Thrift.Type.STRING, 3);
    output.writeString(this.resource);
    output.writeFieldEnd();
  }
  if (this.version !== null && this.version !== undefined) {
    output.writeFieldBegin('version', Thrift.Type.I32, 4);
    output.writeI32(this.version);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

ResourceInfo = module.exports.ResourceInfo = function(args) {
  this.accountId = null;
  this.applicationId = null;
  this.filename = null;
  this.size = null;
  this.modtime = null;
  if (args) {
    if (args.accountId !== undefined) {
      this.accountId = args.accountId;
    }
    if (args.applicationId !== undefined) {
      this.applicationId = args.applicationId;
    }
    if (args.filename !== undefined) {
      this.filename = args.filename;
    }
    if (args.size !== undefined) {
      this.size = args.size;
    }
    if (args.modtime !== undefined) {
      this.modtime = args.modtime;
    }
  }
};
ResourceInfo.prototype = {};
ResourceInfo.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.accountId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.applicationId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.STRING) {
        this.filename = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.I32) {
        this.size = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      case 5:
      if (ftype == Thrift.Type.I64) {
        this.modtime = input.readI64();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

ResourceInfo.prototype.write = function(output) {
  output.writeStructBegin('ResourceInfo');
  if (this.accountId !== null && this.accountId !== undefined) {
    output.writeFieldBegin('accountId', Thrift.Type.STRING, 1);
    output.writeString(this.accountId);
    output.writeFieldEnd();
  }
  if (this.applicationId !== null && this.applicationId !== undefined) {
    output.writeFieldBegin('applicationId', Thrift.Type.STRING, 2);
    output.writeString(this.applicationId);
    output.writeFieldEnd();
  }
  if (this.filename !== null && this.filename !== undefined) {
    output.writeFieldBegin('filename', Thrift.Type.STRING, 3);
    output.writeString(this.filename);
    output.writeFieldEnd();
  }
  if (this.size !== null && this.size !== undefined) {
    output.writeFieldBegin('size', Thrift.Type.I32, 4);
    output.writeI32(this.size);
    output.writeFieldEnd();
  }
  if (this.modtime !== null && this.modtime !== undefined) {
    output.writeFieldBegin('modtime', Thrift.Type.I64, 5);
    output.writeI64(this.modtime);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

AppFabricServiceException = module.exports.AppFabricServiceException = function(args) {
  Thrift.TException.call(this, "AppFabricServiceException")
  this.name = "AppFabricServiceException"
  this.message = null;
  if (args) {
    if (args.message !== undefined) {
      this.message = args.message;
    }
  }
};
Thrift.inherits(AppFabricServiceException, Thrift.TException);
AppFabricServiceException.prototype.name = 'AppFabricServiceException';
AppFabricServiceException.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.message = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

AppFabricServiceException.prototype.write = function(output) {
  output.writeStructBegin('AppFabricServiceException');
  if (this.message !== null && this.message !== undefined) {
    output.writeFieldBegin('message', Thrift.Type.STRING, 1);
    output.writeString(this.message);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

VerificationStatus = module.exports.VerificationStatus = function(args) {
  this.applicationId = null;
  this.program = null;
  this.status = null;
  this.message = null;
  if (args) {
    if (args.applicationId !== undefined) {
      this.applicationId = args.applicationId;
    }
    if (args.program !== undefined) {
      this.program = args.program;
    }
    if (args.status !== undefined) {
      this.status = args.status;
    }
    if (args.message !== undefined) {
      this.message = args.message;
    }
  }
};
VerificationStatus.prototype = {};
VerificationStatus.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.applicationId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.program = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.I32) {
        this.status = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.STRING) {
        this.message = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

VerificationStatus.prototype.write = function(output) {
  output.writeStructBegin('VerificationStatus');
  if (this.applicationId !== null && this.applicationId !== undefined) {
    output.writeFieldBegin('applicationId', Thrift.Type.STRING, 1);
    output.writeString(this.applicationId);
    output.writeFieldEnd();
  }
  if (this.program !== null && this.program !== undefined) {
    output.writeFieldBegin('program', Thrift.Type.STRING, 2);
    output.writeString(this.program);
    output.writeFieldEnd();
  }
  if (this.status !== null && this.status !== undefined) {
    output.writeFieldBegin('status', Thrift.Type.I32, 3);
    output.writeI32(this.status);
    output.writeFieldEnd();
  }
  if (this.message !== null && this.message !== undefined) {
    output.writeFieldBegin('message', Thrift.Type.STRING, 4);
    output.writeString(this.message);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

DeploymentStatus = module.exports.DeploymentStatus = function(args) {
  this.overall = null;
  this.message = null;
  this.verification = null;
  if (args) {
    if (args.overall !== undefined) {
      this.overall = args.overall;
    }
    if (args.message !== undefined) {
      this.message = args.message;
    }
    if (args.verification !== undefined) {
      this.verification = args.verification;
    }
  }
};
DeploymentStatus.prototype = {};
DeploymentStatus.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.I32) {
        this.overall = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.message = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.LIST) {
        var _size0 = 0;
        var _rtmp34;
        this.verification = [];
        var _etype3 = 0;
        _rtmp34 = input.readListBegin();
        _etype3 = _rtmp34.etype;
        _size0 = _rtmp34.size;
        for (var _i5 = 0; _i5 < _size0; ++_i5)
        {
          var elem6 = null;
          elem6 = new ttypes.VerificationStatus();
          elem6.read(input);
          this.verification.push(elem6);
        }
        input.readListEnd();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

DeploymentStatus.prototype.write = function(output) {
  output.writeStructBegin('DeploymentStatus');
  if (this.overall !== null && this.overall !== undefined) {
    output.writeFieldBegin('overall', Thrift.Type.I32, 1);
    output.writeI32(this.overall);
    output.writeFieldEnd();
  }
  if (this.message !== null && this.message !== undefined) {
    output.writeFieldBegin('message', Thrift.Type.STRING, 2);
    output.writeString(this.message);
    output.writeFieldEnd();
  }
  if (this.verification !== null && this.verification !== undefined) {
    output.writeFieldBegin('verification', Thrift.Type.LIST, 3);
    output.writeListBegin(Thrift.Type.STRUCT, this.verification.length);
    for (var iter7 in this.verification)
    {
      if (this.verification.hasOwnProperty(iter7))
      {
        iter7 = this.verification[iter7];
        iter7.write(output);
      }
    }
    output.writeListEnd();
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

FlowIdentifier = module.exports.FlowIdentifier = function(args) {
  this.accountId = null;
  this.applicationId = null;
  this.flowId = null;
  this.version = -1;
  this.type = 0;
  if (args) {
    if (args.accountId !== undefined) {
      this.accountId = args.accountId;
    }
    if (args.applicationId !== undefined) {
      this.applicationId = args.applicationId;
    }
    if (args.flowId !== undefined) {
      this.flowId = args.flowId;
    }
    if (args.version !== undefined) {
      this.version = args.version;
    }
    if (args.type !== undefined) {
      this.type = args.type;
    }
  }
};
FlowIdentifier.prototype = {};
FlowIdentifier.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.accountId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.applicationId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.STRING) {
        this.flowId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.I32) {
        this.version = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      case 5:
      if (ftype == Thrift.Type.I32) {
        this.type = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FlowIdentifier.prototype.write = function(output) {
  output.writeStructBegin('FlowIdentifier');
  if (this.accountId !== null && this.accountId !== undefined) {
    output.writeFieldBegin('accountId', Thrift.Type.STRING, 1);
    output.writeString(this.accountId);
    output.writeFieldEnd();
  }
  if (this.applicationId !== null && this.applicationId !== undefined) {
    output.writeFieldBegin('applicationId', Thrift.Type.STRING, 2);
    output.writeString(this.applicationId);
    output.writeFieldEnd();
  }
  if (this.flowId !== null && this.flowId !== undefined) {
    output.writeFieldBegin('flowId', Thrift.Type.STRING, 3);
    output.writeString(this.flowId);
    output.writeFieldEnd();
  }
  if (this.version !== null && this.version !== undefined) {
    output.writeFieldBegin('version', Thrift.Type.I32, 4);
    output.writeI32(this.version);
    output.writeFieldEnd();
  }
  if (this.type !== null && this.type !== undefined) {
    output.writeFieldBegin('type', Thrift.Type.I32, 5);
    output.writeI32(this.type);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

RunIdentifier = module.exports.RunIdentifier = function(args) {
  this.id = null;
  if (args) {
    if (args.id !== undefined) {
      this.id = args.id;
    }
  }
};
RunIdentifier.prototype = {};
RunIdentifier.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.id = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

RunIdentifier.prototype.write = function(output) {
  output.writeStructBegin('RunIdentifier');
  if (this.id !== null && this.id !== undefined) {
    output.writeFieldBegin('id', Thrift.Type.STRING, 1);
    output.writeString(this.id);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

FlowStatus = module.exports.FlowStatus = function(args) {
  this.applicationId = null;
  this.flowId = null;
  this.version = null;
  this.runId = null;
  this.status = null;
  if (args) {
    if (args.applicationId !== undefined) {
      this.applicationId = args.applicationId;
    }
    if (args.flowId !== undefined) {
      this.flowId = args.flowId;
    }
    if (args.version !== undefined) {
      this.version = args.version;
    }
    if (args.runId !== undefined) {
      this.runId = args.runId;
    }
    if (args.status !== undefined) {
      this.status = args.status;
    }
  }
};
FlowStatus.prototype = {};
FlowStatus.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.applicationId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.flowId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.I32) {
        this.version = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.STRUCT) {
        this.runId = new ttypes.RunIdentifier();
        this.runId.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 5:
      if (ftype == Thrift.Type.STRING) {
        this.status = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FlowStatus.prototype.write = function(output) {
  output.writeStructBegin('FlowStatus');
  if (this.applicationId !== null && this.applicationId !== undefined) {
    output.writeFieldBegin('applicationId', Thrift.Type.STRING, 1);
    output.writeString(this.applicationId);
    output.writeFieldEnd();
  }
  if (this.flowId !== null && this.flowId !== undefined) {
    output.writeFieldBegin('flowId', Thrift.Type.STRING, 2);
    output.writeString(this.flowId);
    output.writeFieldEnd();
  }
  if (this.version !== null && this.version !== undefined) {
    output.writeFieldBegin('version', Thrift.Type.I32, 3);
    output.writeI32(this.version);
    output.writeFieldEnd();
  }
  if (this.runId !== null && this.runId !== undefined) {
    output.writeFieldBegin('runId', Thrift.Type.STRUCT, 4);
    this.runId.write(output);
    output.writeFieldEnd();
  }
  if (this.status !== null && this.status !== undefined) {
    output.writeFieldBegin('status', Thrift.Type.STRING, 5);
    output.writeString(this.status);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

FlowDescriptor = module.exports.FlowDescriptor = function(args) {
  this.identifier = null;
  this.arguments = null;
  if (args) {
    if (args.identifier !== undefined) {
      this.identifier = args.identifier;
    }
    if (args.arguments !== undefined) {
      this.arguments = args.arguments;
    }
  }
};
FlowDescriptor.prototype = {};
FlowDescriptor.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.identifier = new ttypes.FlowIdentifier();
        this.identifier.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.MAP) {
        var _size8 = 0;
        var _rtmp312;
        this.arguments = {};
        var _ktype9 = 0;
        var _vtype10 = 0;
        _rtmp312 = input.readMapBegin();
        _ktype9 = _rtmp312.ktype;
        _vtype10 = _rtmp312.vtype;
        _size8 = _rtmp312.size;
        for (var _i13 = 0; _i13 < _size8; ++_i13)
        {
          var key14 = null;
          var val15 = null;
          key14 = input.readString();
          val15 = input.readString();
          this.arguments[key14] = val15;
        }
        input.readMapEnd();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FlowDescriptor.prototype.write = function(output) {
  output.writeStructBegin('FlowDescriptor');
  if (this.identifier !== null && this.identifier !== undefined) {
    output.writeFieldBegin('identifier', Thrift.Type.STRUCT, 1);
    this.identifier.write(output);
    output.writeFieldEnd();
  }
  if (this.arguments !== null && this.arguments !== undefined) {
    output.writeFieldBegin('arguments', Thrift.Type.MAP, 2);
    output.writeMapBegin(Thrift.Type.STRING, Thrift.Type.STRING, Thrift.objectLength(this.arguments));
    for (var kiter16 in this.arguments)
    {
      if (this.arguments.hasOwnProperty(kiter16))
      {
        var viter17 = this.arguments[kiter16];
        output.writeString(kiter16);
        output.writeString(viter17);
      }
    }
    output.writeMapEnd();
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

ActiveFlow = module.exports.ActiveFlow = function(args) {
  this.applicationId = null;
  this.flowId = null;
  this.type = null;
  this.lastStopped = null;
  this.lastStarted = null;
  this.currentState = null;
  this.runs = null;
  if (args) {
    if (args.applicationId !== undefined) {
      this.applicationId = args.applicationId;
    }
    if (args.flowId !== undefined) {
      this.flowId = args.flowId;
    }
    if (args.type !== undefined) {
      this.type = args.type;
    }
    if (args.lastStopped !== undefined) {
      this.lastStopped = args.lastStopped;
    }
    if (args.lastStarted !== undefined) {
      this.lastStarted = args.lastStarted;
    }
    if (args.currentState !== undefined) {
      this.currentState = args.currentState;
    }
    if (args.runs !== undefined) {
      this.runs = args.runs;
    }
  }
};
ActiveFlow.prototype = {};
ActiveFlow.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.applicationId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.flowId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.I32) {
        this.type = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.I64) {
        this.lastStopped = input.readI64();
      } else {
        input.skip(ftype);
      }
      break;
      case 5:
      if (ftype == Thrift.Type.I64) {
        this.lastStarted = input.readI64();
      } else {
        input.skip(ftype);
      }
      break;
      case 6:
      if (ftype == Thrift.Type.STRING) {
        this.currentState = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 7:
      if (ftype == Thrift.Type.I32) {
        this.runs = input.readI32();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

ActiveFlow.prototype.write = function(output) {
  output.writeStructBegin('ActiveFlow');
  if (this.applicationId !== null && this.applicationId !== undefined) {
    output.writeFieldBegin('applicationId', Thrift.Type.STRING, 1);
    output.writeString(this.applicationId);
    output.writeFieldEnd();
  }
  if (this.flowId !== null && this.flowId !== undefined) {
    output.writeFieldBegin('flowId', Thrift.Type.STRING, 2);
    output.writeString(this.flowId);
    output.writeFieldEnd();
  }
  if (this.type !== null && this.type !== undefined) {
    output.writeFieldBegin('type', Thrift.Type.I32, 3);
    output.writeI32(this.type);
    output.writeFieldEnd();
  }
  if (this.lastStopped !== null && this.lastStopped !== undefined) {
    output.writeFieldBegin('lastStopped', Thrift.Type.I64, 4);
    output.writeI64(this.lastStopped);
    output.writeFieldEnd();
  }
  if (this.lastStarted !== null && this.lastStarted !== undefined) {
    output.writeFieldBegin('lastStarted', Thrift.Type.I64, 5);
    output.writeI64(this.lastStarted);
    output.writeFieldEnd();
  }
  if (this.currentState !== null && this.currentState !== undefined) {
    output.writeFieldBegin('currentState', Thrift.Type.STRING, 6);
    output.writeString(this.currentState);
    output.writeFieldEnd();
  }
  if (this.runs !== null && this.runs !== undefined) {
    output.writeFieldBegin('runs', Thrift.Type.I32, 7);
    output.writeI32(this.runs);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

FlowRunRecord = module.exports.FlowRunRecord = function(args) {
  this.runId = null;
  this.startTime = null;
  this.endTime = null;
  this.endStatus = null;
  if (args) {
    if (args.runId !== undefined) {
      this.runId = args.runId;
    }
    if (args.startTime !== undefined) {
      this.startTime = args.startTime;
    }
    if (args.endTime !== undefined) {
      this.endTime = args.endTime;
    }
    if (args.endStatus !== undefined) {
      this.endStatus = args.endStatus;
    }
  }
};
FlowRunRecord.prototype = {};
FlowRunRecord.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.runId = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.I64) {
        this.startTime = input.readI64();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.I64) {
        this.endTime = input.readI64();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.STRING) {
        this.endStatus = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

FlowRunRecord.prototype.write = function(output) {
  output.writeStructBegin('FlowRunRecord');
  if (this.runId !== null && this.runId !== undefined) {
    output.writeFieldBegin('runId', Thrift.Type.STRING, 1);
    output.writeString(this.runId);
    output.writeFieldEnd();
  }
  if (this.startTime !== null && this.startTime !== undefined) {
    output.writeFieldBegin('startTime', Thrift.Type.I64, 2);
    output.writeI64(this.startTime);
    output.writeFieldEnd();
  }
  if (this.endTime !== null && this.endTime !== undefined) {
    output.writeFieldBegin('endTime', Thrift.Type.I64, 3);
    output.writeI64(this.endTime);
    output.writeFieldEnd();
  }
  if (this.endStatus !== null && this.endStatus !== undefined) {
    output.writeFieldBegin('endStatus', Thrift.Type.STRING, 4);
    output.writeString(this.endStatus);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

