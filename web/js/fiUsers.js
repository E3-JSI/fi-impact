(function () {
	'use strict';
	angular.module('fiManagerApp').controller('fiUsers', ['$http', '$scope', '$uibModal', 'formSubmit', function($http, $scope, $uibModal, formSubmit) {
		var vm = this;
		
		vm.open = function(modal, user) {
			var modalInstance = $uibModal.open({
				backdrop: 'static',
				templateUrl: '../../ui/admin/'+modal+'Modal.html',
				controller: modal+'Ctrl as vm',
				scope: $scope,
				resolve: { username: function () { return user } }
			});
		}
		
		$scope.updateUsers = function() {
			$http.get('../../manager?action=user-list').then(
				function successCallback(response) { $scope.users = response.data.users },
				function errorCallback(response) { console.log('Failed getting users.') }
			);
		}
		
		$scope.profile = fi.profile
		$scope.accelerators = fi.accelerators
		$scope.users = []
		$scope.updateUsers()
		$scope.roles = fi.getJSON('../../manager?action=roles')
		delete $scope.roles.admin
		$scope.properties = ['description', 'accelerator', 'access']
		
		// prepare headers for the table
		var usersHeaders = {
			0: { sorter: "text" }, // user
			1: { sorter: "text" }, // description
			2: { sorter: "text" } // accelerator
		}
		$.each($scope.roles, function(r, description) {
			usersHeaders[Object.keys(usersHeaders).length] = { sorter: false }
		})
		usersHeaders[Object.keys(usersHeaders).length] = { sorter: false }
		usersHeaders[Object.keys(usersHeaders).length] = { sorter: false }
		fiTableSorter.headers = usersHeaders
		fiTableSorter.sortList = [[0,0]]
		
		// functions
		$scope.hasAccess = function(user, role) {
			var access = false
			$.each($scope.users, function(i, u) {
				if (u.user == user) {
					$.each($scope.roles, function(r, description) {
						access = access || (r == role  && (u.access).indexOf(role) >= 0)
					})
				}
			})
			return access
		}
		
	}]);
}());