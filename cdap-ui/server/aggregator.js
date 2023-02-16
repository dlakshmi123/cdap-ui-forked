/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*global require, module */

var request = require('request'),
    fs = require('fs'),
    log4js = require('log4js');

var log = log4js.getLogger('default');

/**
 * Default Poll Interval used by the backend.
 * We set the default poll interval to high, so
 * if any of the frontend needs faster than this
 * time, then would have to pass in the 'interval'
 * in their request.
 */
var POLL_INTERVAL = 10*1000;

/**
 * Aggregator
 * receives resourceObj, aggregate them,
 * and send poll responses back through socket
 *
 * @param {Object} SockJS connection
 */
function Aggregator (conn) {
  // make 'new' optional
  if ( !(this instanceof Aggregator) ) {
    return new Aggregator(conn);
  }

  conn.on('data', onSocketData.bind(this));
  conn.on('close', onSocketClose.bind(this));

  this.connection = conn;

  // WebSocket local resource pool. Key here is the resource id
  // as send from the backend. The FE has to guarantee that the
  // the resource id is unique within a websocket connection.
  this.polledResources = {};
}

/**
 * Checks if the 'id' received from the client is already registered -- This
 * check was added because for whatever reason 'Safari' was sending multiple
 * requests to backend with same ids. As this is happens only once during
 * the start of poll, it's safe to make this check. The assumption here is
 * that the frontend is sending in unique ids within the websocket session.
 *
 * Upon check if it's not duplicate, we invoke doPoll that would make the
 * first call and set the interval for the timeout.
 */
Aggregator.prototype.startPolling = function (resource) {
  // WARN: This assumes that the browser side ids are unique for a websocket session.
  // This check is needed for Safari.
  if(this.polledResources[resource.id]) {
    return;
  }

  resource.interval = resource.interval || POLL_INTERVAL;
  log.debug('Scheduling (' + resource.id + ',' + resource.url + ',' + resource.interval + ')');
  this.polledResources[resource.id] = resource;
  doPoll.bind(this, resource)();
};

/**
 * This method is called regularly by 'doPoll' to register the next interval
 * for timeout. Every resource handle has a flag is used to indicate if the
 * the resource has been requested to be stopped, if it's already stopped, then
 * there is nothing for us to do. If it's not then we go ahead and register
 * the interval timeout.
 */
Aggregator.prototype.scheduleAnotherIteration = function (resource) {
  if (resource.stop) {
    // Don't reschedule another iteration if the resource has been stopped
    return;
  }
  log.debug('Rescheduling (' + resource.id + ',' + resource.url + ',' + resource.interval + ')');
  resource.timerId = setTimeout(doPoll.bind(this, resource), resource.interval);
};

/**
 * Stops the polling of a resource. The resources that has been requested to be stopped
 * is removed from the websocket local poll and the timeouts are cleared and stop flag
 * is set to true.
 */
Aggregator.prototype.stopPolling = function (resource) {
  log.debug('Stopping (' + resource.id + ',' + resource.url + ')');
  var thisResource = removeFromObj(this.polledResources, resource.id);
  if (thisResource === undefined) {
    return;
  }
  clearTimeout(thisResource.timerId);
  thisResource.stop = true;
};

/**
 * Iterates through the websocket local resources and clears the timers and sets
 * the flag. This is called when the websocket is closing the connection.
 */
Aggregator.prototype.stopPollingAll = function() {
  var id, resource;
  for (id in this.polledResources) {
    if (this.polledResources.hasOwnProperty(id)) {
      resource = this.polledResources[id];
      clearTimeout(resource.timerId);
      resource.stop = true;
    }
  }
};

/**
 * Pushes the adapter configuration for templates and plugins to the
 * FE. These configurations are UI specific and hences need to be supported
 * here.
 */
Aggregator.prototype.pushConfiguration = function(resource) {
  var templateid = resource.templateid;
  var pluginid = resource.pluginid;
  var config = {};
  var statusCode = 404;
  var file;

  try {
    // Check if the configuration is present within the plugin for a template
    file = __dirname + '/../templates/' + templateid + '/' + pluginid + '.json';
    config = JSON.parse(fs.readFileSync(file, 'utf8'));
    statusCode = 200;
  } catch(e1) {
    try {
      // Some times there might a plugin that is common across multiple templates
      // in which case, this is stored within the common directory. So, if the
      // template specific plugin check fails, then attempt to get it from common.
      file = __dirname + '/../templates/common/' + pluginid + '.json';
      config = JSON.parse(fs.readFileSync(file, 'utf8'));
      statusCode = 200;
    } catch (e2) {
      log.debug('Unable to find template %s, plugin %s', templateid, pluginid);
    }
  }
  this.connection.write(JSON.stringify({
    resource: resource,
    statusCode: statusCode,
    response: config
  }));
};

/**
 * Removes the resource id from the websocket connection local resource pool.
 */
function removeFromObj(obj, key) {
  var el = obj[key];
  delete obj[key];
  return el;
}

/**
 * 'doPoll' is the doer - it makes the resource request call to backend and
 * sends the response back once it receives it. Upon completion of the request
 * it schedulers the interval for next trigger.
 */
function doPoll (resource) {
    var that = this,
        callBack = this.scheduleAnotherIteration.bind(that, resource);

    resource.startTs = Date.now();
    request(resource, function(error, response, body) {
      if (error) {
        emitResponse.call(that, resource, error);
        return;
      }

      emitResponse.call(that, resource, false, response, body);

    }).on('response', callBack)
    .on('error', callBack);
}

/**
 * Helps avoid sending certain properties to the browser (meta attributes used only in the node server)
 */
function stripResource(key, value) {
  // note that 'stop' is not the stop timestamp, but rather a stop flag/signal (unlike the startTs)
  if (key==='timerId' || key==='startTs' || key==='stop') {
    return undefined;
  }
  return value;
}


/**
 * @private emitResponse
 *
 * sends data back to the client through socket
 *
 * @param  {object} resource that was requested
 * @param  {error|null} error
 * @param  {object} response
 * @param  {string} body
 */
function emitResponse (resource, error, response, body) {
  var timeDiff = Date.now()  - resource.startTs;

  if(error) {
    log.debug('[' + timeDiff + 'ms] Error (' + resource.id + ',' + resource.url + ')');
    log.trace('[' + timeDiff + 'ms] Error (' + resource.id + ',' + resource.url + ') body : (' + error.toString() + ')');
    this.connection.write(JSON.stringify({
      resource: resource,
      error: error,
      warning: error.toString()
    }, stripResource));

  } else {
    log.debug('[' + timeDiff + 'ms] Success (' + resource.id + ',' + resource.url + ')');
    log.trace('[' + timeDiff + 'ms] Success (' + resource.id + ',' + resource.url + ') body : (' + JSON.stringify(body) + ')');
    this.connection.write(JSON.stringify({
      resource: resource,
      statusCode: response.statusCode,
      response: body
    }, stripResource));
  }
}

/**
 * @private onSocketData
 * @param  {string} message received via socket
 */
function onSocketData (message) {
  try {
    message = JSON.parse(message);
    var r = message.resource;

    switch(message.action) {
      case 'template-config':
        log.debug('Adapter config request (' + r.method + ',' + r.id + ',' + r.templateid + ',' + r.pluginid);
        this.pushConfiguration(r);
        break;
      case 'poll-start':
        log.debug ('Poll start (' + r.method + ',' + r.id + ',' + r.url + ')');
        this.startPolling(r);
        break;
      case 'request':
        log.debug ('Single request (' + r.method + ',' + r.id + ',' + r.url + ')');
        r.startTs = Date.now();
        request(r, emitResponse.bind(this, r));
        break;
      case 'poll-stop':
        log.debug ('Poll stop (' + r.id + ',' + r.url + ')');
        this.stopPolling(r);
        break;
    }
  }
  catch (e) {
    log.warn(e);
  }
}

/**
 * @private onSocketClose
 */
function onSocketClose () {
  this.stopPollingAll();
  this.polledResources = {};
}

module.exports = Aggregator;
