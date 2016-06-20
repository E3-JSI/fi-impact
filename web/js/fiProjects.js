(function () {
	'use strict';
	angular.module('fiManagerApp').controller('fiProjects', ['$scope', 'formSubmit', '$uibModal', 'fileUpload', function($scope, formSubmit, $uibModal, fileUpload) {
		var vm = this;
		
		var getJSON = function (jsonUrl) {
			var result = null;
			$.ajax({ url: jsonUrl, type: 'get', dataType: 'json', async: false, success: function(data) { result = data; } });
			return result;
		}
		
		var manager = getJSON('../../manager?action=list')
		
		$scope.surveys = manager.surveys
		$scope.profile = getJSON('../../manager?action=user-profile')
		$scope.accelerators = getJSON('../..//manager?action=accelerators')
		$scope.firstLogin = ($scope.profile.first_login == "true")
		
		vm.open = function(modal) {
			var modalInstance = $uibModal.open({
				backdrop: 'static',
				templateUrl: '../../ui/admin/'+modal+'Modal.html',
				controller: modal+'Ctrl as vm',
				scope: $scope
			});
		}
		
		if ($scope.firstLogin) vm.open('password')
		
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
		
		$scope.access = function(role) { return ($scope.profile.access).indexOf(role) >= 0 }
		
	}]);
}());