var appName = 'monitor';

/*******************************************************************************
 * Displays newly uploaded images communicated using websockets
 */
require.config({
	paths: {
		doge: 'doge',
		stomp: doge.jsUrl('stomp-websocket/lib/stomp'),
		sockjs: doge.jsUrl('sockjs/sockjs'),
		angular: doge.jsUrl('angular/angular'),
		domReady: doge.jsUrl('requirejs-domready/domReady')
	},
	shim: {
		angular: {
			exports: 'angular'
		}
	}
});

define([ 'require', 'angular' ], function (require, angular) {
	'use strict';
	require([ 'sockjs', 'angular', 'stomp', 'domReady!' ], function (sockjs, angular, stomp) {
		angular.bootstrap(document, [ appName ]);
	});

	var doge = angular.module(appName, []);

	doge.controller('MonitorController', [
		'$scope',
		'$http',
		'$log',
		function ($scope, $http, $log) {

			$scope.imgSource = "";
			$scope.uploads = [];
			$scope.size = 0;

			require([ 'sockjs', 'stomp' ], function (sockjs, stomp) {
				var socket = new SockJS('/doge');
				var client = Stomp.over(socket);
				client.connect({}, function (frame) {
					console.log('Connected ' + frame);
					client.subscribe("/topic/alarms", function (message) {
						var body = JSON.parse(message.body);
						$scope.$apply(function () {
							$scope.onDoge(body);
						});
					});
				}, function (error) {
					console.log("STOMP protocol error " + error);
				});
			});

			$scope.onDoge = function (msg) {
				$scope.uploads.unshift(msg);
				$scope.size = $scope.uploads.length
			};

		} ]);
});
