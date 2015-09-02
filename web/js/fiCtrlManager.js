var fiManagerApp = angular.module('fiManagerApp', []);
fiManagerApp.filter("sanitize", ['$sce', function($sce) {
  return function(htmlCode) { return $sce.trustAsHtml(htmlCode); }
}]);
fiManagerApp.controller('fiCtrl', function ($scope) {
	$scope.surveys = fi.manager.surveys;
	$.each($scope.surveys, function( i, v ) {
		$scope.surveys[i].lineChart = lineChart(v.FEASIBILITY,v.INNOVATION,v.MARKET,v.MARKET_NEEDS_BUSINESS);
		$scope.surveys[i].feasibility = Math.round(fiPercent(v.FEASIBILITY,'F'));
		$scope.surveys[i].innovation = Math.round(fiPercent(v.INNOVATION,'I'));
		$scope.surveys[i].market = Math.round(fiPercent(v.MARKET,'M'));
		$scope.surveys[i].business = Math.round(fiPercent(v.MARKET_NEEDS_BUSINESS,'MN'));
	});
});