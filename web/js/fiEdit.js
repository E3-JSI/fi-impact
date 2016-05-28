(function () {
	'use strict';
	angular.module('fiManagerApp').controller('editCtrl', ['$scope', '$uibModalInstance', 'formSubmit', 'username', function ($scope, $uibModalInstance, formSubmit, username) {
		
		var vm = this;	
		vm.dismiss = function () { $uibModalInstance.dismiss(); };
		vm.edit = { password: '', roles: {}, pristine: {}, self: false }
		vm.alert = { successEdit: false }		
		$.each($scope.roles, function(r, description) { vm.edit.roles[r] = false })
		$.each($scope.users, function(i, u) {
			if (u.user == username) {
				vm.edit.user = username
				vm.edit.self = ($scope.profile.user == username)
				$.each($scope.properties, function(i, p) {
					vm.edit[p] = u[p]
					vm.edit.pristine[p] = u[p]
				})
			}
		})
		$.each(vm.edit.access, function(i, role) { vm.edit.roles[role] = true })
		
		vm.editUser = function() {
			var roleArray = []
			var data = { user: vm.edit.user }
			$.each(vm.edit.roles, function(r, value) { if (value) roleArray.push(r) })
			vm.edit.access = roleArray
			if (vm.edit.password != '') data.password = vm.edit.password
			$.each($scope.properties, function(i, p) { if (vm.edit.pristine[p] != vm.edit[p]) data[p] = vm.edit[p] })
			if ('access' in data) {
				data.role = data.access
				delete data.access
			}
			formSubmit.async("user-edit", data, function() {
				vm.alert.successEdit = true
				window.setTimeout(function() {
					vm.hideAlerts()
					$uibModalInstance.dismiss();
				}, 1000)
				$scope.updateUsers()
			})
		}
		
		vm.hideAlerts = function() {
			$.each(vm.alert, function( key, value ) { vm.alert[key] = false })
		}
		
	}]);
}());