var module = angular.module(PKG.name+'.services');

/*
  inspired by https://medium.com/opinionated-angularjs/
    techniques-for-authentication-in-angularjs-applications-7bbf0346acec
 */

module.constant('MYAUTH_EVENT', {
  loginSuccess: 'myauth-login-success',
  loginFailed: 'myauth-login-failed',
  logoutSuccess: 'myauth-logout-success',
  sessionTimeout: 'myauth-session-timeout',
  notAuthenticated: 'myauth-not-authenticated',
  notAuthorized: 'myauth-not-authorized'
});


module.constant('MYAUTH_ROLE', {
  all: '*',
  user: 'user',
  admin: 'admin'
});


module.run(function ($location, $state, $rootScope, myAuth, MYAUTH_EVENT, MYAUTH_ROLE) {

  $rootScope.$on('$stateChangeStart', function (event, next) {

    var authorizedRoles = next.data && next.data.authorizedRoles;
    if (!authorizedRoles) { return; } // no role required, anyone can access

    var user = myAuth.isAuthenticated();
    if (user) { // user is logged in
      if (authorizedRoles === MYAUTH_ROLE.all) { return; } // any logged-in user is welcome
      if (user.hasRole(authorizedRoles)) { return; } // user is legit
    }

    // in all other cases, prevent going to this state
    event.preventDefault();

    // FIXME: THIS IS FLAKY/WRONG. We need to change this.
    // and go to login instead
    // $state.go('login', {
    //   next: angular.toJson({
    //     path: $location.path(),
    //     search: $location.search()
    //   })
    // });

    $state.go('login');

    $rootScope.$broadcast(user ? MYAUTH_EVENT.notAuthorized : MYAUTH_EVENT.notAuthenticated);
  });

});


module.service('myAuth', function myAuthService (MYAUTH_EVENT, MyAuthUser, myAuthPromise, $rootScope, $localStorage, $cookies) {

  /**
   * private method to sync the user everywhere
   */
  var persist = angular.bind(this, function (u) {
    this.currentUser = u;
    $rootScope.currentUser = u;
  });

  /**
   * remembered
   * @return {object} credentials
   */
  this.remembered = function () {
    var r = $localStorage.remember;
    return angular.extend({
      remember: !!r
    }, r || {});
  };

  /**
   * login
   * @param  {object} credentials
   * @return {promise} resolved on sucessful login
   */
  this.login = function (cred) {
    return myAuthPromise(cred).then(
      function loginSuccess(data) {
        var user = new MyAuthUser(data);
        persist(user);
        $cookies['CDAP_Auth_Token'] = user.token;
        $cookies['CDAP_Auth_User'] = user.username;
        $localStorage.remember = cred.remember && user.storable();
        $rootScope.$broadcast(MYAUTH_EVENT.loginSuccess);
      },
      function loginError() {
        $rootScope.$broadcast(MYAUTH_EVENT.loginFailed);
      }
    );
  };

  /**
   * logout
   */
  this.logout = function () {
    if (this.currentUser){
      persist(null);
      delete $cookies['CDAP_Auth_Token'];
      delete $cookies['CDAP_Auth_User'];
      $rootScope.$broadcast(MYAUTH_EVENT.logoutSuccess);
    }
  };

  /**
   * is there someone here?
   * @return {Boolean}
   */
  this.isAuthenticated = function () {
    if (this.currentUser) {
      return !!this.currentUser;
    }

    if ($cookies['CDAP_Auth_Token'] && $cookies['CDAP_Auth_User']) {
      var user = new MyAuthUser({
        access_token: $cookies['CDAP_Auth_Token'],
        username: $cookies['CDAP_Auth_User']
      });
      persist(user);
      return !!this.currentUser;
    }
  };

});


module.factory('myAuthPromise', function myAuthPromiseFactory (MY_CONFIG, $q, $http) {
  return function myAuthPromise (credentials) {
    var deferred = $q.defer();

    if(MY_CONFIG.securityEnabled) {

      $http({
        url: '/login',
        method: 'POST',
        data: credentials
      })
      .success(function (data) {
        deferred.resolve(angular.extend(data, {
          username: credentials.username
        }));
      })
      .error(function (data) {
        deferred.reject(data);
      });

    } else {

      console.warn('Security is disabled, logging in automatically');
      deferred.resolve({
        username: credentials.username
      });

    }

    return deferred.promise;
  };
});


module.factory('MyAuthUser', function MyAuthUserFactory (MYAUTH_ROLE) {

  /**
   * Constructor for currentUser data
   * @param {object} user data
   */
  function User(data) {
    this.token = data.access_token;
    this.username = data.username;
    this.role = MYAUTH_ROLE.user;

    if (data.username==='admin') {
      this.role = MYAUTH_ROLE.admin;
    }
  }


  /**
   * do i haz one of given roles?
   * @param  {String|Array} authorizedRoles
   * @return {Boolean}
   */
  User.prototype.hasRole = function(authorizedRoles) {
    if(this.role === MYAUTH_ROLE.admin) {
      return true;
    }
    if (!angular.isArray(authorizedRoles)) {
      authorizedRoles = [authorizedRoles];
    }
    return authorizedRoles.indexOf(this.role) !== -1;
  };


  /**
   * Omits secure info (i.e. token) and gets object for use
   * in localstorage.
   * @return {Object} storage info.
   */
  User.prototype.storable = function () {
    return {
      username: this.username
    };
  };


  return User;
});
