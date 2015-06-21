'use strict';

var app = angular.module('conferenceApp',
    ['conferenceControllers', 'ngRoute', 'ui.bootstrap']).
    config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.
                when('/conference', {
                    templateUrl: '/partials/show_conferences.html',
                    controller: 'ShowConferenceCtrl'
                }).
                when('/conference/create', {
                    templateUrl: '/partials/create_conferences.html',
                    controller: 'CreateConferenceCtrl'
                }).
                when('/conference/detail/:websafeConferenceKey', {
                    templateUrl: '/partials/conference_detail.html',
                    controller: 'ConferenceDetailCtrl'
                }).
                when('/profile', {
                    templateUrl: '/partials/profile.html',
                    controller: 'MyProfileCtrl'
                }).
                when('/', {
                    templateUrl: '/partials/home.html'
                }).
                otherwise({
                    redirectTo: '/'
                });
        }]);

app.filter('startFrom', function () {
    /**
     * Extracts an array from the specific index.
     *
     * @param {Array} data
     * @param {Integer} start
     * @returns {Array|*}
     */
    var filter = function (data, start) {
        return data.slice(start);
    }
    return filter;
});


app.constant('HTTP_ERRORS', {
    'UNAUTHORIZED': 401
});


app.factory('oauth2Provider', function ($modal) {
    var oauth2Provider = {
        CLIENT_ID: '699416837520-hheni4i8tu7f7s5ev8poqleoevf7jcjg.apps.googleusercontent.com',
        SCOPES: 'https://www.googleapis.com/auth/userinfo.email profile',
        signedIn: false
    };

    /**
     * Calls the OAuth2 authentication method.
     */
    oauth2Provider.signIn = function (callback) {
        gapi.auth.signIn({
            'clientid': oauth2Provider.CLIENT_ID,
            'cookiepolicy': 'single_host_origin',
            'accesstype': 'online',
            'approveprompt': 'auto',
            'scope': oauth2Provider.SCOPES,
            'callback': callback
        });
    };

    oauth2Provider.signOut = function () {
        gapi.auth.signOut();
        gapi.auth.setToken({access_token: ''})
        oauth2Provider.signedIn = false;
    };

    oauth2Provider.showLoginModal = function() {
        var modalInstance = $modal.open({
            templateUrl: '/partials/login.modal.html',
            controller: 'OAuth2LoginModalCtrl'
        });
        return modalInstance;
    };

    return oauth2Provider;
});
