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

angular.module(PKG.name + '.feature.adapters')
  .controller('CanvasController', function (MyAppDAGService, $scope, $modalStack) {

    this.nodes = [];

    function errorNotification(errors) {
      angular.forEach(this.pluginTypes, function (type) {
        delete type.error;
        if (errors[type.name]) {
          type.error = errors[type.name];
        }
      });
    }

    MyAppDAGService.errorCallback(errorNotification.bind(this));

    $scope.$on('$destroy', function() {
      $modalStack.dismissAll();
    });

  });
