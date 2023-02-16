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
  .service('MyConsoleTabService', function() {
    this.messages = [];
    this.resetMessages = function() {
      this.messages = [];
      this.notifyOnResetMessageListeners();
    };

    this.onMessageUpdateListeners = [];
    this.onResetMessageListeners = [];
    this.addMessage = function(message) {
      message.date = new Date();
      this.messages.push(message);
      this.notifyMessageUpdateListeners(message);
    };

    this.registerOnMessageUpdates = function(callback) {
      this.onMessageUpdateListeners.push(callback);
    };

    this.notifyMessageUpdateListeners = function(message) {
      this.onMessageUpdateListeners.forEach(function(callback) {
        callback(message);
      });
    };

    this.registerOnResetMessages = function(callback) {
      this.onResetMessageListeners.push(callback);
    };

    this.notifyOnResetMessageListeners = function() {
      this.onResetMessageListeners.forEach(function (callback) {
        callback();
      });
    };

  });
