angular.module(PKG.name + '.feature.admin').controller('NamespaceAppMetadataController',
function ($scope, $state, $alert, $timeout, MyDataSource) {

  var data = new MyDataSource($scope);
  var path = '/namespaces/' + $state.params.nsadmin + '/apps/' + $state.params.appId;

  data.request({
    _cdapPath: path
  })
    .then(function(apps) {
      $scope.apps = apps;
    });

  $scope.deleteApp = function(app) {
    data.request({
      _cdapPath: path,
      method: 'DELETE'
    }, function() {
      $alert({
        type: 'success',
        title: app,
        content: 'App deleted successfully'
      });
      // FIXME: Have to avoid $timeout here. Un-necessary.
      $timeout(function() {
        $state.go('^.apps');
      });
    });
  };

});
