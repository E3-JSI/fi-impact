var fiManagerApp = angular.module('fiManagerApp', []);
fiManagerApp.filter("sanitize", ['$sce', function($sce) {
  return function(htmlCode) { return $sce.trustAsHtml(htmlCode); }
}]);
fiManagerApp.controller('fiCtrl', function ($scope) {
	$scope.surveys = fi.manager.surveys;
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
});