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

angular.module(PKG.name + '.feature.services')
  .controller('ServicesRunsController', function($scope, $filter, $state, rRuns, $bootstrapModal, rServiceDetail, myMetadataFactory) {
    var fFilter = $filter('filter');
    this.runs = rRuns;
    this.$bootstrapModal = $bootstrapModal;
    this.description = rServiceDetail.description;
    if ($state.params.runid) {
      var match = fFilter(rRuns, {runid: $state.params.runid});
      if (match.length) {
        this.runs.selected = angular.copy(match[0]);
      } else {
        $state.go('404');
        return;
      }
    } else if (rRuns.length) {
      this.runs.selected = angular.copy(rRuns[0]);
    } else {
      this.runs.selected = {
        runid: 'No Runs'
      };
    }

    $scope.$watch(angular.bind(this, function() {
      return this.runs.selected.runid;
    }), function() {
      if ($state.params.runid) {
        return;
      } else {
        if (rRuns.length) {
          this.runs.selected = angular.copy(rRuns[0]);
        }
      }
    }.bind(this));

    this.tabs = [
      {
        title: 'Status',
        template: '/assets/features/services/templates/tabs/runs/tabs/status.html'
      },
      {
        title: 'Logs',
        template: '/assets/features/services/templates/tabs/runs/tabs/log.html'
      },
      {
        title: 'Datasets',
        template: '/assets/features/services/templates/tabs/data.html'
      }
    ];

    this.activeTab = this.tabs[0];

    this.selectTab = function(tab) {
      this.activeTab = tab;
    };

    this.openHistory = function() {
      this.$bootstrapModal.open({
        size: 'lg',
        windowClass: 'center cdap-modal',
        templateUrl: '/assets/features/services/templates/tabs/history.html',
        controller: ['runs', '$scope', function(runs, $scope) {
          $scope.runs = runs;
        }],
        resolve: {
          runs: function() {
            return this.runs;
          }.bind(this)
        }
      });
    };


    var metadataParams = {
      namespace: $state.params.namespace,
      appId: $state.params.appId,
      programType: 'services',
      programId: $state.params.programId,
      scope: $scope
    };
    this.metadataAddOpen = false;
    this.metadataTags = [];

    myMetadataFactory.getProgramMetadata(metadataParams)
      .then(function (res) {
        this.metadataTags = res;
      }.bind(this));

    this.addMetadata = function () {
      myMetadataFactory.addProgramMetadata(this.tag, metadataParams)
        .then(function (res) {
          this.metadataTags = res;
          this.tag = '';
        }.bind(this));
    };

    this.deleteMetadata = function (tag) {
      myMetadataFactory.deleteProgramMetadata(tag, metadataParams)
        .then(function (res) {
          this.metadataTags = res;
        }.bind(this));
    };

  });
