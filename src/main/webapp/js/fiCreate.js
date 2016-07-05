(function () {
	'use strict';
	angular.module('fiManagerApp').controller('createCtrl', ['$scope', '$uibModalInstance', 'formSubmit', 'username', function ($scope, $uibModalInstance, formSubmit, username) {
		
		var vm = this;	

		vm.dismiss = function () { $uibModalInstance.dismiss(); };
		
		vm.create = { user: '', password: '', description: '', accelerator: $scope.profile.accelerator }
		
		vm.alert = {
			enterUser: false,
			enterPassword: false,
			enterDescription: false,
			successCreate: false
		}
		
		vm.createUser = function() {
			console.log(vm.create)
			if (vm.create.user == '') vm.alert.enterUser = true
			else if (vm.create.password == '') vm.alert.enterPassword = true
			else if (vm.create.description == '') vm.alert.enterDescription = true
			else {
				formSubmit.async("user-create", vm.create, function() {
					vm.alert.successCreate = true
					window.setTimeout(function() {
						vm.hideAlerts()
						$uibModalInstance.dismiss()
					}, 1000)
					$scope.updateUsers()
				})
			}
		}
		
		vm.hideAlerts = function() {
			$.each(vm.alert, function( key, value ) { vm.alert[key] = false })
		}
		
	}]);
}());