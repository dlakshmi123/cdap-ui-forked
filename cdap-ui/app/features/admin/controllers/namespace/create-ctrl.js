angular.module(PKG.name + '.feature.admin')
  .controller('NamespaceCreateController', function ($scope, $alert, $modalInstance, MyDataSource, myNamespace, EventPipe) {
    $scope.model = {
      name: '',
      description: ''
    };
    $scope.isSaving = false;

    var myDataSrc = new MyDataSource($scope);
    $scope.submitHandler = function() {
      if ($scope.isSaving) {
        return;
      }

      $scope.isSaving = true;
      myDataSrc.request({
        method: 'PUT',
        _cdapPath: '/namespaces/' + $scope.model.name,
        body: {
          name: $scope.model.name,
          description: $scope.model.description
        }
      })
        .then(
          function success(res) {
            $scope.isSaving = false;
            $modalInstance.close();
            $alert({
              content: res,
              type: 'success'
            });

            myNamespace.getList(true).then(function() {
              EventPipe.emit('namespace.update');
            });
          },
          function error(err) {
            $scope.isSaving = false;
            $scope.error = err.data;
          }
        );
    };
    $scope.closeModal = function() {
      $modalInstance.close();

    };
  });
