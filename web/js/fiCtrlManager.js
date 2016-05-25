var fiManagerApp = angular.module('fiManagerApp', []);
fiManagerApp.filter("sanitize", ['$sce', function($sce) {
  return function(htmlCode) { return $sce.trustAsHtml(htmlCode); }
}]);

fiManagerApp.directive('fileModel', ['$parse', function ($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel)
			var modelSetter = model.assign
			element.bind('change', function() { scope.$apply(function(){ modelSetter(scope, element[0].files[0]) }) })
		}
	};
}]);
      
fiManagerApp.service('fileUpload', ['$http', '$q', function ($http, $q) {
	var deffered = $q.defer()
	var data = [];
	var uploadService = {
		async: function(file, uploadUrl) {
			var fd = new FormData();
			fd.append('file', file);
			fd.append('action', 'upload-mattermark')
			
			// $http returns a promise, which has a then function, which also returns a promise
			var promise = $http.post(uploadUrl, fd, {
				transformRequest: angular.identity,
				headers: {'Content-Type': undefined}
			})
			.success(function(d) {
				data = { 'success': true, 'data': d }
				deffered.resolve()
			})
			// Return the promise to the controller
			return deffered.promise;
		},
		data: function() { return data; }
	}
	return uploadService;
}]);

fiManagerApp.service('formSubmit', ['$http', '$q', '$httpParamSerializerJQLike',  function ($http, $q, $httpParamSerializerJQLike) {
	var deffered = $q.defer()
	var response = {}
	var fixSerializedArray = function(serialized) { return serialized.replace(/%5B%5D/g, '') }
	var formService = {
		async: function(action, url, data) {
			data.action = action
			var promise = $http.post(url, fixSerializedArray($httpParamSerializerJQLike(data)), {
				transformRequest: angular.identity,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
			})
			.success(function(d) {
				response = d
				deffered.resolve()
			})
			return deffered.promise;
		},
		response: function() { return response }
	}
	return formService;
}]);

fiManagerApp.controller('fiProjects', ['$scope', 'formSubmit', 'fileUpload', function($scope, formSubmit, fileUpload) {
	$scope.surveys = fi.manager.surveys
	$scope.profile = fi.profile
	$scope.accelerators = fi.accelerators
	$scope.uploadTab = 'tabNotice'

	$scope.password = { 'old': '', 'new': '', 'repeat': '' }
	$scope.passwordRepeat = ( ($scope.password.old != '' && $scope.password.new != '' && $scope.password.repeat != '' && $scope.password.repeat != $scope.password.new) ? true : false )
	$scope.pwdAlert = {
		enterOld: false,
		enterNew: false,
		oldNew: false,
		enterRepeat: false,
		newRepeat: false,
		failedChange: false,
		success: false
	}
	$scope.pwdResponse = ''
	
	$scope.toggleTab = function(tabID) { $scope.uploadTab = tabID }
	
	var ratioToPercent = function(r) { return Math.round(r*100); console.log("test"); }
	$.each($scope.surveys, function( i, v ) {
		var code = '<svg class="chart" width="100" height="29">'
		code += '<g transform="translate(0,0)"><rect width="' + ratioToPercent(v.FEASIBILITY_GRAPH_PERCENT) + '" height="5" fill="steelblue"></rect></g>'
		code += '<g transform="translate(0,6)"><rect width="' + ratioToPercent(v.INNOVATION_GRAPH_PERCENT) + '" height="5" fill="lightskyblue"></rect></g>'
		code += '<g transform="translate(0,12)"><rect width="' + ratioToPercent(v.MARKET_GRAPH_PERCENT) + '" height="5" fill="teal"></rect></g>'
		code += '<g transform="translate(0,18)"><rect width="' + ratioToPercent(v.MARKET_NEEDS_GRAPH_PERCENT) + '" height="5" fill="lightsteelblue"></rect></g>'
		code += '<g transform="translate(0,24)"><rect width="' + ratioToPercent(v.MATTERMARK_GROWTH_GRAPH_PERCENT) + '" height="5" fill="steelblue"></rect></g>'
		code += '</svg>'
		
		$scope.surveys[i].lineChart = code;
		$scope.surveys[i].feasibility = ratioToPercent(v.FEASIBILITY_GRAPH_PERCENT);
		$scope.surveys[i].innovation = ratioToPercent(v.INNOVATION_GRAPH_PERCENT);
		$scope.surveys[i].market = ratioToPercent(v.MARKET_GRAPH_PERCENT);
		$scope.surveys[i].business = ratioToPercent(v.MARKET_NEEDS_GRAPH_PERCENT);
		$scope.surveys[i].mattermark = ratioToPercent(v.MATTERMARK_GROWTH_GRAPH_PERCENT);
	});
	
	$scope.clearUnderscores = function(str) {
		return str.replace(/_/g, " ")
	}
	
	$scope.uploadFile = function() {
		// console.dir(file);       
		$scope.toggleTab('tabUpload')
		fileUpload.async($scope.dataFile, "../../manager").then(function() {
			$scope.uploadResult = fileUpload.data()
			$scope.toggleTab('tabReview')
			window.setTimeout(function() {
				autoContinue = angular.element( document.querySelector( '#autoContinue' ) );
				autoContinue.trigger('click')
			}, 500)
			
		})
	};
	
	$scope.uploadModal = function() {
		delete $scope.dataFile
		console.log($scope.dataFile)
	}
	
	$scope.access = function(role) { return ($scope.profile.access).indexOf(role) >= 0 }
	
	$scope.changePassword = function() {
		$scope.password.old = ''
		$scope.password.new = ''
		$scope.password.repeat = ''
		$scope.pwdAlert = {
			enterOld: false,
			enterNew: false,
			oldNew: false,
			enterRepeat: false,
			newRepeat: false,
			failedChange: false,
			success: false
		}
		$scope.pwdResponse = ''
	}
	$scope.changePwd = function() {
		if ($scope.password.old == '') $scope.pwdAlert.enterOld = true
		else if ($scope.password.new == '') $scope.pwdAlert.enterNew = true
		else if ($scope.password.new == $scope.password.old) $scope.pwdAlert.oldNew = true
		else if  ($scope.password.repeat == '') $scope.pwdAlert.enterRepeat = true
		else if ($scope.password.new != $scope.password.repeat) $scope.pwdAlert.newRepeat = true
		else {
			var change = { 'user': $scope.profile.user, 'password-old': $scope.password.old, 'password-new': $scope.password.new }
			formSubmit.async("user-my-password", "../../manager", change).then(function() {
				var response = formSubmit.response()
				console.log(response)
				if (response.success == 'false') {
					$scope.pwdResponse = response.error
					$scope.pwdAlert.failedChange = true
				}
				else {
					$scope.pwdAlert.success = true
					window.setTimeout(function() {
						closeDialog = angular.element( document.querySelector( '#closePwdChange' ) );
						closeDialog.trigger('click')
						$scope.hideAlerts()
					}, 1000)
				}
			})
		}
	}
	
	$scope.hideAlerts = function() {
		$scope.pwdResponse = ''
		$.each($scope.pwdAlert, function( key, value ) { $scope.pwdAlert[key] = false })
	}
	
}]);

fiManagerApp.controller('fiUsers', ['$http', '$scope', 'formSubmit', function($http, $scope, formSubmit) {
	$scope.profile = fi.profile
	$scope.accelerators = fi.accelerators
	$scope.users = fi.users.users
	$scope.roles = fi.roles
	delete $scope.roles.admin
	$scope.editing = { password: '', roles: {}, pristine: {}, self: false }
	$scope.properties = ['description', 'accelerator', 'access']
	$scope.create = { user: '', password: '', description: '', accelerator: '' }
	$scope.delete = ''
	
	$scope.userAlert = {
		enterUser: false,
		enterPassword: false,
		enterDescription: false,
		successCreate: false,
		successEdit: false
	}
	
	// prepare headers for the table
	var usersHeaders = {
		0: { sorter: "text" }, // user
		1: { sorter: "text" }, // description
		2: { sorter: "text" } // accelerator
	}
	$.each($scope.roles, function(r, description) {
		usersHeaders[Object.keys(usersHeaders).length] = { sorter: false }
		$scope.editing.roles[r] = false
	})
	usersHeaders[Object.keys(usersHeaders).length] = { sorter: false }
	usersHeaders[Object.keys(usersHeaders).length] = { sorter: false }
	fiTableSorter.headers = usersHeaders
	fiTableSorter.sortList = [[0,0]]
	
	// functions
	$scope.editModal = function(user) {
		$scope.hideAlerts()
		$scope.editing.password = ''
		$.each($scope.users, function(i, u) {
			if (u.user == user) {
				$scope.editing.user = user
				$scope.editing.self = ($scope.profile.user == user)
				$.each($scope.properties, function(i, p) {
					$scope.editing[p] = u[p]
					$scope.editing.pristine[p] = u[p]
				})
			}
		})
		$.each($scope.editing.access, function(i, role) { $scope.editing.roles[role] = true })
	}
	
	$scope.deleteModal = function(user) { $scope.delete = user }
	
	$scope.createModal = function(user) {
		$scope.create.user = ''
		$scope.create.password = ''
		$scope.create.description = ''
		$scope.create.accelerator = $scope.profile.accelerator
		$scope.hideAlerts()
	}
	
	$scope.hasAccess = function(user, role) {
		access = false
		$.each($scope.users, function(i, u) {
			if (u.user == user) {
				$.each($scope.roles, function(r, description) {
					access = access || (r == role  && (u.access).indexOf(role) >= 0)
				})
			}
		})
		return access
	}
	
	$scope.editUser = function() {
		var roleArray = []
		var data = { user: $scope.editing.user }
		$.each($scope.editing.roles, function(r, value) { if (value) roleArray.push(r) })
		$scope.editing.access = roleArray
		if ($scope.editing.password != '') data.password = $scope.editing.password
		$.each($scope.properties, function(i, p) { if ($scope.editing.pristine[p] != $scope.editing[p]) data[p] = $scope.editing[p] })
		if ('access' in data) {
			data.role = data.access
			delete data.access
		}
		formSubmit.async("user-edit", "../../manager", data).then(function() {
			$scope.userAlert.successEdit = true
			window.setTimeout(function() {
				closeDialog = angular.element( document.querySelector( '#closeEdit' ) );
				closeDialog.trigger('click')
				$scope.hideAlerts()
			}, 1000)
			updateUsers()
		})
	}
	
	$scope.createUser = function() {
		if ($scope.create.user == '') $scope.userAlert.enterUser = true
		else if ($scope.create.password == '') $scope.userAlert.enterPassword = true
		else if ($scope.create.description == '') $scope.userAlert.enterDescription = true
		else {
			formSubmit.async("user-create", "../../manager", $scope.create).then(function() {
				$scope.userAlert.successCreate = true
				window.setTimeout(function() {
					closeDialog = angular.element( document.querySelector( '#closeCreate' ) );
					closeDialog.trigger('click')
					$scope.hideAlerts()
				}, 1000)
				updateUsers()
			})
		}
	}
	
	$scope.deleteUser = function(user) {
		formSubmit.async("user-delete", "../../manager", { user: $scope.delete }).then(function() {
			window.setTimeout(updateUsers, 1000)
		})
	}
	
	var updateUsers = function() {
		$http({
			method: 'GET',
			url: '../../manager?action=user-list'
		}).then(
			function successCallback(response) { $scope.users = response.data.users },
			function errorCallback(response) { console.log('Failed getting users.') }
		);
	}

	$scope.hideAlerts = function() { $.each($scope.userAlert, function( key, value ) { $scope.userAlert[key] = false }) }
	
}]);
