(function () {
	'use strict';
	angular.module('fiManagerApp').controller('deleteCtrl', ['$scope', '$uibModalInstance', 'formSubmit', 'username', function ($scope, $uibModalInstance, formSubmit, username) {
		
		var vm = this;
		vm.username = username;

		vm.dismiss = function () { $uibModalInstance.dismiss(); };
		
		vm.deleteUser = function() {
			formSubmit.async("user-delete", {user: username}, function() {
				window.setTimeout(function() {
					$uibModalInstance.dismiss()
				}, 1000)
				$scope.updateUsers()
			})
		}
		
	}]);
}());