//
// Autogenerated by Thrift Compiler (0.8.0)
//
// DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
//
var Thrift = require('thrift').Thrift;

var ttypes = require('./flowservices_types');
//HELPER FUNCTIONS AND STRUCTURES

var AuthorizationService_authenticate_args = function(args) {
  this.user = null;
  this.password = null;
  if (args) {
    if (args.user !== undefined) {
      this.user = args.user;
    }
    if (args.password !== undefined) {
      this.password = args.password;
    }
  }
};
AuthorizationService_authenticate_args.prototype = {};
AuthorizationService_authenticate_args.prototype.read = function(input) {
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
        this.user = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.password = input.readString();
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

AuthorizationService_authenticate_args.prototype.write = function(output) {
  output.writeStructBegin('AuthorizationService_authenticate_args');
  if (this.user) {
    output.writeFieldBegin('user', Thrift.Type.STRING, 1);
    output.writeString(this.user);
    output.writeFieldEnd();
  }
  if (this.password) {
    output.writeFieldBegin('password', Thrift.Type.STRING, 2);
    output.writeString(this.password);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var AuthorizationService_authenticate_result = function(args) {
  this.success = null;
  this.noauth = null;
  this.authtimeout = null;
  if (args) {
    if (args.success !== undefined) {
      this.success = args.success;
    }
    if (args.noauth !== undefined) {
      this.noauth = args.noauth;
    }
    if (args.authtimeout !== undefined) {
      this.authtimeout = args.authtimeout;
    }
  }
};
AuthorizationService_authenticate_result.prototype = {};
AuthorizationService_authenticate_result.prototype.read = function(input) {
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
      case 0:
      if (ftype == Thrift.Type.STRUCT) {
        this.success = new ttypes.DelegationToken();
        this.success.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.noauth = new ttypes.NotAuthorizedException();
        this.noauth.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.authtimeout = new ttypes.AuthorizationTimeoutException();
        this.authtimeout.read(input);
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

AuthorizationService_authenticate_result.prototype.write = function(output) {
  output.writeStructBegin('AuthorizationService_authenticate_result');
  if (this.success) {
    output.writeFieldBegin('success', Thrift.Type.STRUCT, 0);
    this.success.write(output);
    output.writeFieldEnd();
  }
  if (this.noauth) {
    output.writeFieldBegin('noauth', Thrift.Type.STRUCT, 1);
    this.noauth.write(output);
    output.writeFieldEnd();
  }
  if (this.authtimeout) {
    output.writeFieldBegin('authtimeout', Thrift.Type.STRUCT, 2);
    this.authtimeout.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var AuthorizationService_renew_args = function(args) {
  this.token = null;
  if (args) {
    if (args.token !== undefined) {
      this.token = args.token;
    }
  }
};
AuthorizationService_renew_args.prototype = {};
AuthorizationService_renew_args.prototype.read = function(input) {
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
        this.token = new ttypes.DelegationToken();
        this.token.read(input);
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

AuthorizationService_renew_args.prototype.write = function(output) {
  output.writeStructBegin('AuthorizationService_renew_args');
  if (this.token) {
    output.writeFieldBegin('token', Thrift.Type.STRUCT, 1);
    this.token.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var AuthorizationService_renew_result = function(args) {
  this.success = null;
  this.noauth = null;
  this.authtimeout = null;
  if (args) {
    if (args.success !== undefined) {
      this.success = args.success;
    }
    if (args.noauth !== undefined) {
      this.noauth = args.noauth;
    }
    if (args.authtimeout !== undefined) {
      this.authtimeout = args.authtimeout;
    }
  }
};
AuthorizationService_renew_result.prototype = {};
AuthorizationService_renew_result.prototype.read = function(input) {
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
      case 0:
      if (ftype == Thrift.Type.STRUCT) {
        this.success = new ttypes.DelegationToken();
        this.success.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.noauth = new ttypes.NotAuthorizedException();
        this.noauth.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.authtimeout = new ttypes.AuthorizationTimeoutException();
        this.authtimeout.read(input);
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

AuthorizationService_renew_result.prototype.write = function(output) {
  output.writeStructBegin('AuthorizationService_renew_result');
  if (this.success) {
    output.writeFieldBegin('success', Thrift.Type.STRUCT, 0);
    this.success.write(output);
    output.writeFieldEnd();
  }
  if (this.noauth) {
    output.writeFieldBegin('noauth', Thrift.Type.STRUCT, 1);
    this.noauth.write(output);
    output.writeFieldEnd();
  }
  if (this.authtimeout) {
    output.writeFieldBegin('authtimeout', Thrift.Type.STRUCT, 2);
    this.authtimeout.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

var AuthorizationServiceClient = exports.Client = function(output, pClass) {
    this.output = output;
    this.pClass = pClass;
    this.seqid = 0;
    this._reqs = {};
};
AuthorizationServiceClient.prototype = {};
AuthorizationServiceClient.prototype.authenticate = function(user, password, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_authenticate(user, password);
};

AuthorizationServiceClient.prototype.send_authenticate = function(user, password) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('authenticate', Thrift.MessageType.CALL, this.seqid);
  var args = new AuthorizationService_authenticate_args();
  args.user = user;
  args.password = password;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

AuthorizationServiceClient.prototype.recv_authenticate = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new AuthorizationService_authenticate_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.noauth) {
    return callback(result.noauth);
  }
  if (null !== result.authtimeout) {
    return callback(result.authtimeout);
  }
  if (null !== result.success) {
    return callback(null, result.success);
  }
  return callback('authenticate failed: unknown result');
};
AuthorizationServiceClient.prototype.renew = function(token, callback) {
  this.seqid += 1;
  this._reqs[this.seqid] = callback;
  this.send_renew(token);
};

AuthorizationServiceClient.prototype.send_renew = function(token) {
  var output = new this.pClass(this.output);
  output.writeMessageBegin('renew', Thrift.MessageType.CALL, this.seqid);
  var args = new AuthorizationService_renew_args();
  args.token = token;
  args.write(output);
  output.writeMessageEnd();
  return this.output.flush();
};

AuthorizationServiceClient.prototype.recv_renew = function(input,mtype,rseqid) {
  var callback = this._reqs[rseqid] || function() {};
  delete this._reqs[rseqid];
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(input);
    input.readMessageEnd();
    return callback(x);
  }
  var result = new AuthorizationService_renew_result();
  result.read(input);
  input.readMessageEnd();

  if (null !== result.noauth) {
    return callback(result.noauth);
  }
  if (null !== result.authtimeout) {
    return callback(result.authtimeout);
  }
  if (null !== result.success) {
    return callback(null, result.success);
  }
  return callback('renew failed: unknown result');
};
var AuthorizationServiceProcessor = exports.Processor = function(handler) {
  this._handler = handler
}
AuthorizationServiceProcessor.prototype.process = function(input, output) {
  var r = input.readMessageBegin();
  if (this['process_' + r.fname]) {
    return this['process_' + r.fname].call(this, r.rseqid, input, output);
  } else {
    input.skip(Thrift.Type.STRUCT);
    input.readMessageEnd();
    var x = new Thrift.TApplicationException(Thrift.TApplicationExceptionType.UNKNOWN_METHOD, 'Unknown function ' + r.fname);
    output.writeMessageBegin(r.fname, Thrift.MessageType.Exception, r.rseqid);
    x.write(output);
    output.writeMessageEnd();
    output.flush();
  }
}

AuthorizationServiceProcessor.prototype.process_authenticate = function(seqid, input, output) {
  var args = new AuthorizationService_authenticate_args();
  args.read(input);
  input.readMessageEnd();
  var result = new AuthorizationService_authenticate_result();
  this._handler.authenticate(args.user, args.password, function (success) {
    result.success = success;
    output.writeMessageBegin("authenticate", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

AuthorizationServiceProcessor.prototype.process_renew = function(seqid, input, output) {
  var args = new AuthorizationService_renew_args();
  args.read(input);
  input.readMessageEnd();
  var result = new AuthorizationService_renew_result();
  this._handler.renew(args.token, function (success) {
    result.success = success;
    output.writeMessageBegin("renew", Thrift.MessageType.REPLY, seqid);
    result.write(output);
    output.writeMessageEnd();
    output.flush();
  })
}

