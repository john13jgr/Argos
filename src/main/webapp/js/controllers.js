'use strict';


var conferenceApp = conferenceApp || {};


conferenceApp.controllers = angular.module('conferenceControllers', ['ui.bootstrap']);


conferenceApp.controllers.controller('MyProfileCtrl',
    function ($scope, $log, oauth2Provider, HTTP_ERRORS) {
        $scope.submitted = false;
        $scope.loading = false;

        $scope.initialProfile = {};

        $scope.sexo = [
            'MASCULINO',
            'FEMENINO'
        ];

        $scope.init = function () {
            var retrieveProfileCallback = function () {
                $scope.profile = {};
                $scope.loading = true;
                gapi.client.conference.getProfile().
                    execute(function (resp) {
                        $scope.$apply(function () {
                            $scope.loading = false;
                            if (resp.error) {
                            } else {
                                $scope.profile.displayName = resp.result.displayName;
                                $scope.profile.cedula = resp.result.cedula;
                                $scope.profile.telefono = resp.result.telefono;
                                $scope.profile.celular = resp.result.celular;
                                $scope.profile.direccion = resp.result.direccion;
                                $scope.profile.sexo = resp.result.sexo;
                                $scope.initialProfile = resp.result;
                            }
                        });
                    }
                );
            };
            if (!oauth2Provider.signedIn) {
                var modalInstance = oauth2Provider.showLoginModal();
                modalInstance.result.then(retrieveProfileCallback);
            } else {
                retrieveProfileCallback();
            }
        };

        $scope.saveProfile = function () {
            $scope.submitted = true;
            $scope.loading = true;
            gapi.client.conference.saveProfile($scope.profile).
                execute(function (resp) {
                    $scope.$apply(function () {
                        $scope.loading = false;
                        if (resp.error) {
                            var errorMessage = resp.error.message || '';
                            $scope.messages = 'Error al actualizar el perfil : ' + errorMessage;
                            $scope.alertStatus = 'warning';
                            $log.error($scope.messages + 'Perfil : ' + JSON.stringify($scope.profile));

                            if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                                oauth2Provider.showLoginModal();
                                return;
                            }
                        } else {
                            $scope.messages = 'El perfil ha sido actualizado';
                            $scope.alertStatus = 'success';
                            $scope.submitted = false;
                            $scope.initialProfile = {
                                displayName: $scope.profile.displayName,
                                cedula: $scope.profile.cedula,
                                telefono: $scope.profile.telefono,
                                celular: $scope.profile.celular,
                                direccion: $scope.profile.direccion,
                                sexo: $scope.profile.sexo
                            };

                            $log.info($scope.messages + JSON.stringify(resp.result));
                        }
                    });
                });
        };
    })
;

conferenceApp.controllers.controller('CreateConferenceCtrl',
    function ($scope, $log, oauth2Provider, HTTP_ERRORS) {

        $scope.conference = $scope.conference || {};

        $scope.cities = [
            'Teatro Colsubsidio Roberto Arias Pérez',
            'Teatro Julio Mario Santo Domingo',
            'Teatro Jorge Eliecer Gaitán',
            'Auditorio León de Greiff',
            'Fundación Gilberto Alzate Avendaño'
        ];

        $scope.topics = [
            'Obra de Teatro',
            'Concierto',
            'Musical',
            'Privado',
            'Masivo'
        ];

        $scope.isValidMaxAttendees = function () {
            if (!$scope.conference.maxAttendees || $scope.conference.maxAttendees.length == 0) {
                return true;
            }
            return /^[\d]+$/.test($scope.conference.maxAttendees) && $scope.conference.maxAttendees >= 0;
        }

        $scope.isValidDates = function () {
            if (!$scope.conference.startDate && !$scope.conference.endDate) {
                return true;
            }
            if ($scope.conference.startDate && !$scope.conference.endDate) {
                return true;
            }
            return $scope.conference.startDate <= $scope.conference.endDate;
        }

        $scope.isValidConference = function (conferenceForm) {
            return !conferenceForm.$invalid &&
                $scope.isValidMaxAttendees() &&
                $scope.isValidDates();
        }

        $scope.createConference = function (conferenceForm) {
            if (!$scope.isValidConference(conferenceForm)) {
                return;
            }

            $scope.loading = true;
            gapi.client.conference.createConference($scope.conference).
                execute(function (resp) {
                    $scope.$apply(function () {
                        $scope.loading = false;
                        if (resp.error) {
                            var errorMessage = resp.error.message || '';
                            $scope.messages = 'Error, no se pudo crear el evento : ' + errorMessage;
                            $scope.alertStatus = 'warning';
                            $log.error($scope.messages + ' Evento : ' + JSON.stringify($scope.conference));

                            if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                                oauth2Provider.showLoginModal();
                                return;
                            }
                        } else {
                            $scope.messages = 'El evento ha sido creado : ' + resp.result.name;
                            $scope.alertStatus = 'success';
                            $scope.submitted = false;
                            $scope.conference = {};
                            $log.info($scope.messages + ' : ' + JSON.stringify(resp.result));
                        }
                    });
                });
        };
    });

conferenceApp.controllers.controller('ShowConferenceCtrl', function ($scope, $log, oauth2Provider, HTTP_ERRORS) {

    $scope.submitted = false;

    $scope.selectedTab = 'ALL';

    $scope.filters = [
    ];

    $scope.filtereableFields = [
        {enumValue: 'CITY', displayName: 'City'},
        {enumValue: 'TOPIC', displayName: 'Topic'},
        {enumValue: 'MONTH', displayName: 'Start month'},
        {enumValue: 'MAX_ATTENDEES', displayName: 'Max Attendees'}
    ]

    $scope.operators = [
        {displayName: '=', enumValue: 'EQ'},
        {displayName: '>', enumValue: 'GT'},
        {displayName: '>=', enumValue: 'GTEQ'},
        {displayName: '<', enumValue: 'LT'},
        {displayName: '<=', enumValue: 'LTEQ'},
        {displayName: '!=', enumValue: 'NE'}
    ];

    $scope.conferences = [];

    $scope.isOffcanvasEnabled = false;

    $scope.tabAllSelected = function () {
        $scope.selectedTab = 'ALL';
        $scope.queryConferences();
    };

    $scope.tabYouHaveCreatedSelected = function () {
        $scope.selectedTab = 'YOU_HAVE_CREATED';
        if (!oauth2Provider.signedIn) {
            oauth2Provider.showLoginModal();
            return;
        }
        $scope.queryConferences();
    };

    $scope.tabYouWillAttendSelected = function () {
        $scope.selectedTab = 'YOU_WILL_ATTEND';
        if (!oauth2Provider.signedIn) {
            oauth2Provider.showLoginModal();
            return;
        }
        $scope.queryConferences();
    };

    $scope.toggleOffcanvas = function () {
        $scope.isOffcanvasEnabled = !$scope.isOffcanvasEnabled;
    };

    $scope.pagination = $scope.pagination || {};
    $scope.pagination.currentPage = 0;
    $scope.pagination.pageSize = 20;

    $scope.pagination.numberOfPages = function () {
        return Math.ceil($scope.conferences.length / $scope.pagination.pageSize);
    };

    $scope.pagination.pageArray = function () {
        var pages = [];
        var numberOfPages = $scope.pagination.numberOfPages();
        for (var i = 0; i < numberOfPages; i++) {
            pages.push(i);
        }
        return pages;
    };

    $scope.pagination.isDisabled = function (event) {
        return angular.element(event.target).hasClass('disabled');
    }
    $scope.addFilter = function () {
        $scope.filters.push({
            field: $scope.filtereableFields[0],
            operator: $scope.operators[0],
            value: ''
        })
    };

    $scope.clearFilters = function () {
        $scope.filters = [];
    };

    $scope.removeFilter = function (index) {
        if ($scope.filters[index]) {
            $scope.filters.splice(index, 1);
        }
    };

    $scope.queryConferences = function () {
        $scope.submitted = false;
        if ($scope.selectedTab == 'ALL') {
            $scope.queryConferencesAll();
        } else if ($scope.selectedTab == 'YOU_HAVE_CREATED') {
            $scope.getConferencesCreated();
        } else if ($scope.selectedTab == 'YOU_WILL_ATTEND') {
            $scope.getConferencesAttend();
        }
    };

    $scope.queryConferencesAll = function () {
        var sendFilters = {
            filters: []
        }
        for (var i = 0; i < $scope.filters.length; i++) {
            var filter = $scope.filters[i];
            if (filter.field && filter.operator && filter.value) {
                sendFilters.filters.push({
                    field: filter.field.enumValue,
                    operator: filter.operator.enumValue,
                    value: filter.value
                });
            }
        }
        $scope.loading = true;
        gapi.client.conference.queryConferences(sendFilters).
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Error al buscar eventos : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages + ' filters : ' + JSON.stringify(sendFilters));
                    } else {
                        $scope.submitted = false;
                        $scope.messages = 'Todos los eventos disponibles';
                        $scope.alertStatus = 'success';
                        $log.info($scope.messages);

                        $scope.conferences = [];
                        angular.forEach(resp.items, function (conference) {
                            $scope.conferences.push(conference);
                        });
                    }
                    $scope.submitted = true;
                });
            });
    }

    $scope.getConferencesCreated = function () {
        $scope.loading = true;
        gapi.client.conference.getConferencesCreated().
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Failed to query the conferences created : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages);

                        if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                            oauth2Provider.showLoginModal();
                            return;
                        }
                    } else {
                        $scope.submitted = false;
                        $scope.messages = 'Query succeeded : Conferences you have created';
                        $scope.alertStatus = 'success';
                        $log.info($scope.messages);

                        $scope.conferences = [];
                        angular.forEach(resp.items, function (conference) {
                            $scope.conferences.push(conference);
                        });
                    }
                    $scope.submitted = true;
                });
            });
    };

    $scope.getConferencesAttend = function () {
        $scope.loading = true;
        gapi.client.conference.getConferencesToAttend().
            execute(function (resp) {
                $scope.$apply(function () {
                    if (resp.error) {
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Error, no se pudo encontrar los eventos : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages);

                        if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                            oauth2Provider.showLoginModal();
                            return;
                        }
                    } else {
                        $scope.conferences = resp.result.items;
                        $scope.loading = false;
                        $scope.messages = 'Eventos en los cuales se encuentra programado';
                        $scope.alertStatus = 'success';
                        $log.info($scope.messages);
                    }
                    $scope.submitted = true;
                });
            });
    };
});


conferenceApp.controllers.controller('ConferenceDetailCtrl', function ($scope, $log, $routeParams, HTTP_ERRORS) {
    $scope.conference = {};

    $scope.isUserAttending = false;

    $scope.init = function () {
        $scope.loading = true;
        gapi.client.conference.getConference({
            websafeConferenceKey: $routeParams.websafeConferenceKey
        }).execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                    var errorMessage = resp.error.message || '';
                    $scope.messages = 'Error, no se pudo obtener el evento : ' + $routeParams.websafeKey
                        + ' ' + errorMessage;
                    $scope.alertStatus = 'warning';
                    $log.error($scope.messages);
                } else {
                    $scope.alertStatus = 'success';
                    $scope.conference = resp.result;
                }
            });
        });

        $scope.loading = true;
        gapi.client.conference.getProfile().execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                } else {
                    var profile = resp.result;
                    for (var i = 0; i < profile.conferenceKeysToAttend.length; i++) {
                        if ($routeParams.websafeConferenceKey == profile.conferenceKeysToAttend[i]) {
                            $scope.alertStatus = 'info';
                            $scope.messages = 'Se encuentra programado para este evento';
                            $scope.isUserAttending = true;
                        }
                    }
                }
            });
        });
    };

    $scope.registerForConference = function () {
        $scope.loading = true;
        gapi.client.conference.registerForConference({
            websafeConferenceKey: $routeParams.websafeConferenceKey
        }).execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                    var errorMessage = resp.error.message || '';
                    $scope.messages = 'Error, no se pudo programar para este evento : ' + errorMessage;
                    $scope.alertStatus = 'warning';
                    $log.error($scope.messages);

                    if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                        oauth2Provider.showLoginModal();
                        return;
                    }
                } else {
                    if (resp.result) {
                        $scope.messages = 'Programado para el evento';
                        $scope.alertStatus = 'success';
                        $scope.isUserAttending = true;
                        $scope.conference.seatsAvailable = $scope.conference.seatsAvailable - 1;
                    } else {
                        $scope.messages = 'Error, no se pudo programar para este evento';
                        $scope.alertStatus = 'warning';
                    }
                }
            });
        });
    };

    $scope.unregisterFromConference = function () {
        $scope.loading = true;
        gapi.client.conference.unregisterFromConference({
            websafeConferenceKey: $routeParams.websafeConferenceKey
        }).execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                    var errorMessage = resp.error.message || '';
                    $scope.messages = 'Error, no se pudo cancelar la programación para el evento : ' + errorMessage;
                    $scope.alertStatus = 'warning';
                    $log.error($scope.messages);
                    if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                        oauth2Provider.showLoginModal();
                        return;
                    }
                } else {
                    if (resp.result) {
                        $scope.messages = 'Cancelada la programación para este evento';
                        $scope.alertStatus = 'success';
                        $scope.conference.seatsAvailable = $scope.conference.seatsAvailable + 1;
                        $scope.isUserAttending = false;
                        $log.info($scope.messages);
                    } else {
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Error, no se pudo cancelar la programación para el evento : ' + $routeParams.websafeKey +
                            ' : ' + errorMessage;
                        $scope.messages = 'Error';
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages);
                    }
                }
            });
        });
    };
});


conferenceApp.controllers.controller('RootCtrl', function ($scope, $location, oauth2Provider) {

    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };

    $scope.getSignedInState = function () {
        return oauth2Provider.signedIn;
    };

    /**
     * Calls the OAuth2 authentication method.
     */
    $scope.signIn = function () {
        oauth2Provider.signIn(function () {
            gapi.client.oauth2.userinfo.get().execute(function (resp) {
                $scope.$apply(function () {
                    if (resp.email) {
                        oauth2Provider.signedIn = true;
                        $scope.alertStatus = 'success';
                        $scope.rootMessages = 'Sesión iniciada con ' + resp.email;
                    }
                });
            });
        });
    };

    $scope.initSignInButton = function () {
        gapi.signin.render('signInButton', {
            'callback': function () {
                jQuery('#signInButton button').attr('disabled', 'true').css('cursor', 'default');
                if (gapi.auth.getToken() && gapi.auth.getToken().access_token) {
                    $scope.$apply(function () {
                        oauth2Provider.signedIn = true;
                    });
                }
            },
            'clientid': oauth2Provider.CLIENT_ID,
            'cookiepolicy': 'single_host_origin',
            'scope': oauth2Provider.SCOPES
        });
    };

    $scope.signOut = function () {
        oauth2Provider.signOut();
        $scope.alertStatus = 'success';
        $scope.rootMessages = 'Sesión cerrada';
    };

    $scope.collapseNavbar = function () {
        angular.element(document.querySelector('.navbar-collapse')).removeClass('in');
    };

});


conferenceApp.controllers.controller('OAuth2LoginModalCtrl',
    function ($scope, $modalInstance, $rootScope, oauth2Provider) {
        $scope.singInViaModal = function () {
            oauth2Provider.signIn(function () {
                gapi.client.oauth2.userinfo.get().execute(function (resp) {
                    $scope.$root.$apply(function () {
                        oauth2Provider.signedIn = true;
                        $scope.$root.alertStatus = 'success';
                        $scope.$root.rootMessages = 'Sesión iniciada con ' + resp.email;
                    });

                    $modalInstance.close();
                });
            });
        };
    });

conferenceApp.controllers.controller('DatepickerCtrl', function ($scope) {
    $scope.today = function () {
        $scope.dt = new Date();
    };
    $scope.today();

    $scope.clear = function () {
        $scope.dt = null;
    };

    $scope.disabled = function (date, mode) {
        return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
    };

    $scope.toggleMin = function () {
        $scope.minDate = ( $scope.minDate ) ? null : new Date();
    };
    $scope.toggleMin();

    $scope.open = function ($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.opened = true;
    };

    $scope.dateOptions = {
        'year-format': "'yy'",
        'starting-day': 1
    };

    $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
    $scope.format = $scope.formats[0];
});
