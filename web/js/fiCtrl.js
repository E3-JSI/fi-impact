window.fi = new fiReport("99fcfc42-197d-491c-93ce-3df5f2f53b13");
var fiApp = angular.module('fiApp', []);

fiApp.controller('fiCtrl', function ($scope) {
	var colors = d3.scale.category20c();
	$scope.getArray = function(n) { return new Array(n); };
	$scope.aFi = {
		marketNeeds: {},
		socialBenefits: []
	};
	
	// Market Needs
	$.each(fi.questions.Q3_3.value.split(','), function(i, v) {
		$scope.aFi.marketNeeds[v] = {
			sector: model.s3.q3[v],
			score: (fi.results["MARKET_NEEDS_BUSINESS_" + v] - 0.005).toFixed(2),
			scores: fi.getMarketNeedsStarScores(v),
			top5: fi.getVerboseList(model.marketNeedsTop5[v], model.s5A.q1),
			hint: model.graphSlotText[parseInt(fi.results["MARKET_NEEDS_BUSINESS_" + v + "_GRAPH_SLOT"])]
		};
	});
	
	// Social Benefits
	$.each(model.s6A.q1, function(i, v) { $scope.aFi.socialBenefits.push({ key: i, name: v }); });
});

// <i class="fa fa-star-o" ng-repeat="em in getArray(score.empty)"></i>