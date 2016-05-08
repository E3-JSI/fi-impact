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

fiManagerApp.controller('fiCtrl', ['$scope', 'fileUpload', function($scope, fileUpload) {
	$scope.surveys = fi.manager.surveys
	$scope.profile = fi.profile
	$scope.uploadTab = 'tabNotice'
	$scope.toggleTab = function(tabID) { $scope.uploadTab = tabID }
	
	var ratioToPercent = function(r) { return Math.round(r*100); console.log("test"); }
	$.each($scope.surveys, function( i, v ) {
		var code = '<svg class="chart" width="100" height="23">';
		code += '<g transform="translate(0,0)"><rect width="' + ratioToPercent(v.FEASIBILITY_GRAPH_PERCENT) + '" height="5" fill="steelblue"></rect></g>';
		code += '<g transform="translate(0,6)"><rect width="' + ratioToPercent(v.INNOVATION_GRAPH_PERCENT) + '" height="5" fill="lightskyblue"></rect></g>';
		code += '<g transform="translate(0,12)"><rect width="' + ratioToPercent(v.MARKET_GRAPH_PERCENT) + '" height="5" fill="teal"></rect></g>';
		code += '<g transform="translate(0,18)"><rect width="' + ratioToPercent(v.MARKET_NEEDS_GRAPH_PERCENT) + '" height="5" fill="lightsteelblue"></rect></g>';
		code += '</svg>';
		
		$scope.surveys[i].lineChart = code;
		$scope.surveys[i].feasibility = ratioToPercent(v.FEASIBILITY_GRAPH_PERCENT);
		$scope.surveys[i].innovation = ratioToPercent(v.INNOVATION_GRAPH_PERCENT);
		$scope.surveys[i].market = ratioToPercent(v.MARKET_GRAPH_PERCENT);
		$scope.surveys[i].business = ratioToPercent(v.MARKET_NEEDS_GRAPH_PERCENT);
	});
	
	$scope.uploadFile = function(){
		// console.dir(file);       
		$scope.toggleTab('tabUpload')
		fileUpload.async($scope.dataFile, "../../manager").then(function() {
			$scope.toggleTab('tabReview')
			autoContinue = angular.element( document.querySelector( '#autoContinue' ) );
			autoContinue.trigger('click')
			$scope.uploadResult = fileUpload.data()
		})
	};
	
}]);
