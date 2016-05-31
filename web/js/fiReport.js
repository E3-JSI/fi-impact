(function () {
	'use strict';
	angular.module('fiReportApp', []);
}());

(function () {
	'use strict';
	angular.module('fiReportApp').service('fiReport', function () {
	
			var id = location.search.split('id=')[1]
			
			var makeRadarData = function (object, useId) {
				var data = [[],[]];
				$.each(object, function(i, v) {
					var label = (useId ? v.id : v.label);
					data[0].push({axis: label, value: v.average_percent});
					data[1].push({axis: label, value: v.result_percent});
				});
				return data;
			}
		
			var returnData = {
				getJSON: function (jsonUrl) {
					var result = null;
					$.ajax({ url: jsonUrl, type: 'get', dataType: 'json', async: false, success: function(data) { result = data; } });
					return result;
				},
				model: {
					"speedometers": {
						"S_2": "innovation",
						"S_4": "feasibility",
						"S_3": "market",
						"S_7a": "growth",
						"S_7b": "employees",
						"S_7c": "uniques",
						"S_7d": "twitter",
						"S_7e": "facebook",
						"S_7f": "linkedin",
						"S_7g": "funding"},
					"tooltips": ["Low", "Medium", "High"],
					"radarLevelsSocial": {
						"1": "No Impact",
						"2": "Limited Impact",
						"3": "Impact",
						"4": "Significant Impact",
						"5": "High Impact",
						"num": 5
					}
				}
			}
			
			var loadJSON = function(url, constants) {
				var json = returnData.getJSON(url);
				var result = {
					impact: (json.sections.S_0.answers.Q0_1.value == "Impact Assessment"),
					self: (json.sections.S_0.answers.Q0_1.value == "Self Assessment"),
					enablers: json.sections.S_1.answers.Q1_12,
					enablersSpecific: {},
					revenue: json.sections.S_3.answers.Q3_2[0].answers,
					primaryMarketSector: ( json.sections.S_3.answers.Q3_3a ? true : false ),
					marketNeeds: {},
					socialBenefits: {},
					speedometers: [],
					radarOverview: makeRadarData(json.overview.points, false),
					radarSocialA: makeRadarData(json.sections.S_6A.answers.Q6A_1.answers, true),
					radarSocialB: makeRadarData(json.sections.S_6B.answers.Q6B_1.answers, false)
				};
				if  (json.sections.S_1.answers.Q1_18) { $.each(json.sections.S_1.answers.Q1_18[0].answers, function(i, v) { result.enablersSpecific[v.id] = { label: v.label, value: v.value }; }); }
				$.each(constants.speedometers, function(i, v) {
					result.speedometers.push(v)
					result[v] = {
						score: json.sections[i].result.result_percent,
						average: json.sections[i].result.average_percent,
						histogram: json.sections[i].result.speedometer_histogram,
						interpretation: json.sections[i].result.interpretation,
						bottomHalf: ( (json.sections[i].result.result_percent <= .5) ? [1] : []),
					};
					result[v].speedometer = {
						levels: [0, json.sections[i].result.speedometer_lm, json.sections[i].result.speedometer_mh, 1],
						percent: ( (result[v].score < 1) ? result[v].score : 1 ),
						list: [result[v].histogram[0]+result[v].histogram[1], result[v].histogram[2], result[v].histogram[3]+result[v].histogram[4]],
						average: result[v].average,
						tooltips: constants.tooltips
					}
				});
				$.each(json.sections, function(i, v) {
					$.each(v.answers, function(j, w) { if (w.value) { result[j] = w.value; } });
				});
				var S_5max = json.sections.S_5.result.score_max;
				$.each(json.sections.S_5.answers.Q5_1, function(i, v) {
					result.marketNeeds[v.label] = { label: v.label, score: v.result, max: S_5max, stars: [], answers: {}, top: {} };
					$.each(v.answers, function(j, w) { 
						result.marketNeeds[v.label].answers[w.id] = { value: w.value, label: w.label, stars: [] };
						for (i = 0; i < w.value; i++) { result.marketNeeds[v.label].answers[w.id].stars.push(i); }
					});
					$.each(v.top_list, function(j, w) { result.marketNeeds[v.label].top[j] = { id: w.id, label: w.label }; });
				});
				$.each(json.sections.S_6A.answers.Q6A_1.answers, function(i, v) {
					result.socialBenefits[v.id] = { id: v.id, value: v.result_percent, average: v.average_percent, label: v.label };
				});
				return result;
			}
			
			if (typeof id != 'undefined') {
				returnData.json = loadJSON('../../service?action=resultsnew&id=' + id, returnData.model);
				returnData.id = id;
			}
			
			return returnData
	
	});
}());

(function () {
	'use strict';
	angular.module('fiReportApp').controller('fiCtrl', ['$scope', 'fiReport', function ($scope, fiReport) {
		$scope.fi = fiReport
		$scope.id = $scope.fi.id
		$scope.d = $scope.fi.json
		
		$scope.download = function() {
			$.get( "../../service?action=pdf&id=" + $scope.id, function(data) {
				if ($(data).find('status').attr('status') == "OK") {
					var win = window.open('../../pdf/survey-' + $scope.id + '.pdf', '_blank');
					if (win) { win.focus(); }
				}
			});
		};
		
		$.each($scope.d.speedometers, function(i, v) { d3Speedometer('#' + v + 'Gauge', $scope.d[v].speedometer); });
		window.radar = {
			Overview: new fiRadar("bodyOverview", $scope.d.radarOverview),
			SocialA: new fiRadar("bodySocialA", $scope.d.radarSocialA, {w: 400, h: 400, levels: 5}, $scope.fi.model.radarLevelsSocial),
			SocialB: new fiRadar("bodySocialB", $scope.d.radarSocialB, {w: 400, h: 400, levels: 5}, $scope.fi.model.radarLevelsSocial)
		};
		$.each(window.radar, function(i, v) { v.draw("#radar" + i) });
	}]);
}());