var appName = 'client';

/*******************************************************************************
 * Uploads images to made doge-tastic
 */
require.config({
	paths : {
		doge : 'doge',
		stomp : doge.jsUrl('stomp-websocket/lib/stomp'),
		sockjs : doge.jsUrl('sockjs/sockjs'),
		angular : doge.jsUrl('angular/angular'),
		angularFileUpload : doge.jsUrl('ng-file-upload/angular-file-upload'),
		domReady : doge.jsUrl('requirejs-domready/domReady')
	},
	shim : {
		angular : {
			exports : 'angular'
		}
	}
});

define([ 'require', 'angular' ], function(require, angular) {

	'use strict';

	require([ 'angular', 'angularFileUpload', 'sockjs', 'stomp', 'domReady!' ],
			function(angular) {
				angular.bootstrap(document, [ appName ]);
			});

	angular.module(appName, [ 'angularFileUpload' ]).controller('ClientController',
			[ '$scope', '$http', '$upload', '$log', function($scope, $http, $upload, $log) {

				$scope.users = [];
				$scope.dogeUploads = [];

				$http.get('/users').success(function(data) {
					$scope.users = data;
					if ($scope.users != null && $scope.users.length > 0) {
						$scope.selectedUser = $scope.users[0];
					}
				});

				$scope.onFileSelect = function($files) {
					for (var i = 0; i < $files.length; i++) {
						$scope.upload = $upload.upload({
							url : '/users/' + $scope.selectedUser.id + '/doge',
							method : 'POST',
							file : $files[i],
							fileFormDataName: 'file'
						}).success(function(data, status, headers, config) {
							$scope.dogeUploads.splice(0, 0, headers('location'));
						});
					}
				}

			} ]);
});
