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

angular.module(PKG.name + '.feature.hydrator')
  .service('DetailNonRunsStore', function(PipelineDetailDispatcher, HydratorService) {
    this.HydratorService = HydratorService;
    this.setDefaults = function(app) {
      this.state = {
        scheduleStatus: null,
        name: app.name || '',
        type: app.type,
        description: app.description,

        datasets: app.datasets || [],
        streams: app.streams || [],

        configJson: app.configJson || {},
        cloneConfig: app.cloneConfig || {},
        DAGConfig: app.DAGConfig || {nodes: [], connections: []}
      };
    };
    this.changeListeners = [];
    var dispatcher = PipelineDetailDispatcher.getDispatcher();

    this.setDefaults({});
    this.getScheduleStatus = function() {
      return this.state.scheduleStatus;
    };

    this.registerOnChangeListener = function(callback) {
      this.changeListeners.push(callback);
    };
    this.emitChange = function() {
      this.changeListeners.forEach(function(callback) {
        callback(this.state);
      }.bind(this));
    };
    this.setState = function(schedule) {
      this.state.scheduleStatus = schedule.status;
      this.emitChange();
    };
    this.getCloneConfig = function() {
      return this.state.cloneConfig;
    };
    this.getPipelineType = function() {
      return this.state.type;
    };
    this.getPipelineName = function() {
      return this.state.name;
    };
    this.getPipelineDescription = function() {
      return this.state.description;
    };
    this.getDAGConfig = function() {
      var config = angular.copy(this.state.DAGConfig);
      angular.forEach(config.nodes, (node) => {
        if (node.plugin){
          node.label = node.plugin.label;
        }
      });
      return config;
    };
    this.getConnections = function() {
      return this.state.DAGConfig.connections;
    };
    this.getNodes = function() {
      return this.state.DAGConfig.nodes;
    };
    this.getSourceNodes = function(nodeId) {
      let nodesMap = {};
      this.state.DAGConfig.nodes.forEach( node => nodesMap[node.name] = node );
      return this.state.DAGConfig.connections.filter( conn => conn.to === nodeId ).map( matchedConnection => nodesMap[matchedConnection.from] );
    };
    this.getDatasets = function() {
      return this.state.datasets;
    };
    this.getStreams = function() {
      return this.state.streams;
    };
    this.getConfigJson = function() {
      return this.state.configJson;
    };
    this.getAppType = function() {
      return this.state.type;
    };
    this.getPluginObject = function(nodeId) {
      var nodes = this.getNodes();
      var match = nodes.filter( node => node.name === nodeId);
      match = (match.length? match[0]: null);
      return match;
    };
    this.getNode = this.getPluginObject;
    this.init = function(app) {
      var appConfig = {};
      var uiConfig;
      angular.extend(appConfig, app);

      try {
        appConfig.configJson = JSON.parse(app.configuration);
      } catch(e) {
        appConfig.configJson = e;
        console.log('ERROR cannot parse configuration');
        return;
      }
      if(appConfig.configJson) {
        app.config = appConfig.configJson;
        uiConfig = this.HydratorService.getNodesAndConnectionsFromConfig(app);
        let setDefaultOutputSchemaForNodes = (node) => {
          var pluginName = node.plugin.name;
          var pluginToSchemaMap = {
            'Stream': [
              {
                readonly: true,
                name: 'ts',
                type: 'long'
              },
              {
                readonly: true,
                name: 'headers',
                type: {
                  type: 'map',
                  keys: 'string',
                  values: 'string'
                }
              }
            ]
          };
          if (pluginToSchemaMap[pluginName]){
            if (!node.plugin.properties.schema) {
              node.plugin.properties.schema = {
                fields: [{ name: 'body', type: 'string'}]
              };
              node.plugin.properties.schema = JSON.stringify({
                type: 'record',
                name: 'etlSchemaBody',
                fields: angular.isObject(node.outputSchema)?
                  pluginToSchemaMap[pluginName].concat(node.outputSchema.fields || []):
                  pluginToSchemaMap[pluginName]
              });
            } else {
              try {
                let schema = JSON.parse(node.plugin.properties.schema);
                node.plugin.properties.schema = JSON.stringify({
                  type: 'record',
                  name: 'etlSchemaBody',
                  fields: pluginToSchemaMap[pluginName].concat(schema.fields)
                });
              } catch(e) {}
            }
          }
        };
        uiConfig.nodes.forEach(setDefaultOutputSchemaForNodes);
        appConfig.DAGConfig = {
          nodes: uiConfig.nodes,
          connections: uiConfig.connections
        };

        appConfig.description = appConfig.configJson.description ? appConfig.configJson.description : appConfig.description;
      }

      appConfig.type = app.artifact.name;
      appConfig.cloneConfig = {
        name: app.name,
        artifact: app.artifact,
        description: appConfig.configJson.description,
        __ui__: appConfig.DAGConfig,
        config: {
          source: appConfig.configJson.source,
          sinks: appConfig.configJson.sinks,
          transforms: appConfig.configJson.transforms,
          instances: appConfig.configJson.instance,
          schedule: appConfig.configJson.schedule,
          connections: uiConfig.connections,
          comments: appConfig.configJson.comments
        }
      };
      appConfig.streams = app.streams.map(function (stream) {
        stream.type = 'Stream';
        return stream;
      });
      appConfig.datasets = app.datasets.map(function (dataset) {
        dataset.type = 'Dataset';
        return dataset;
      });
      this.setDefaults(appConfig);
    };
    this.reset = function() {
      this.setDefaults({});
      this.changeListeners = [];
    };
    dispatcher.register('onScheduleStatusFetch', this.setState.bind(this));
    dispatcher.register('onReset', this.reset.bind(this));
  });
