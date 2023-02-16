/**
 * myNavbar
 */

angular.module(PKG.name+'.commons').directive('myNavbar',

function myNavbarDirective ($dropdown, myAuth, caskTheme, MY_CONFIG) {
  return {
    restrict: 'A',
    templateUrl: 'navbar/navbar.html',
    controller: 'navbarCtrl',
    link: function (scope, element) {

      var toggles = element[0].querySelectorAll('a.dropdown-toggle');

      // namespace dropdown
      $dropdown(angular.element(toggles[0]), {
        template: 'navbar/namespace.html',
        animation: 'am-flip-x',
        scope: scope
      });

      // right dropdown
      $dropdown(angular.element(toggles[1]), {
        template: 'navbar/dropdown.html',
        animation: 'am-flip-x',
        placement: 'bottom-right',
        scope: scope
      });

      scope.logout = myAuth.logout;
      // If we plan later we could add multiple
      // themes but not in the near future.
      // scope.theme = caskTheme;
      scope.securityEnabled = MY_CONFIG.securityEnabled;
    }
  };
});
