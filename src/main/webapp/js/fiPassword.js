(function () {
	'use strict';
	angular.module('fiManagerApp').controller('passwordCtrl', ['$scope', '$uibModalInstance', 'formSubmit', function ($scope, $uibModalInstance, formSubmit) {
		
		var vm = this;	

		vm.dismiss = function () { $uibModalInstance.dismiss(); };
		
		vm.password = { 'old': '', 'new': '', 'repeat': '' }
		vm.passwordRepeat = ( (vm.password.old != '' && vm.password.new != '' && vm.password.repeat != '' && vm.password.repeat != vm.password.new) ? true : false )
		vm.pwdAlert = {
			enterOld: false,
			enterNew: false,
			oldNew: false,
			enterRepeat: false,
			newRepeat: false,
			failedChange: false,
			success: false
		}
		vm.pwdResponse = ''
		
		vm.changePwd = function() {
			if (vm.password.old == '') vm.pwdAlert.enterOld = true
			else if (vm.password.new == '') vm.pwdAlert.enterNew = true
			else if (vm.password.new == vm.password.old) vm.pwdAlert.oldNew = true
			else if  (vm.password.repeat == '') vm.pwdAlert.enterRepeat = true
			else if (vm.password.new != vm.password.repeat) vm.pwdAlert.newRepeat = true
			else {
				var change = { 'user': $scope.profile.user, 'password-old': vm.password.old, 'password-new': vm.password.new }
				formSubmit.async("user-my-password", change, function(response) {
					console.log(response.data)
					if (response.data.success == 'false') {
						vm.pwdResponse = response.data.error
						vm.pwdAlert.failedChange = true
					}
					else {
						vm.pwdAlert.success = true
						window.setTimeout(function() {
							$uibModalInstance.close();
							vm.hideAlerts()
						}, 1000)
					}
				})
			}
		}
		
		vm.hideAlerts = function() {
			vm.pwdResponse = ''
			$.each(vm.pwdAlert, function( key, value ) { vm.pwdAlert[key] = false })
		}
		
	}]);
}());