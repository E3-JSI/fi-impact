(function () {
	'use strict';
	angular.module('fiBenchmarkApp', []);
}());

(function () {
	'use strict';
	angular.module('fiBenchmarkApp').controller('fiGraphs', ['$scope', 'getData', function($scope, getData) {
		
		var symbols = ['circle', 'diamond', 'square', 'cross', 'triangle-down', 'triangle-up']
		
		var vm = this;
		
		vm.legend = {}
		vm.scatter = {
			abscissa: 'FEASIBILITY',
			ordinate: 'INNOVATION',
			filter: ''
		}
		
		vm.data = []
		vm.total = 0
		vm.filterDictionary = {}
		
		vm.plotChart = function() {
			vm.data = []
			getData.async('data', function(response) {
				vm.total = response.data.total
				var filtering = getFilterGroups()
				console.log(vm.filterDictionary)
				$.each(response.data.surveys, function(type, list) {
					if (!filtering) vm.data.push({ key: type, values: [] })
					$.each(list, function(i, value) {
						var typeIndex = vm.data.length-1
						var index = ( filtering ? getOptionIndex(value.filters[vm.scatter.filter]) : typeIndex )
						vm.data[index].values.push({ shape: symbols[typeIndex], size: 0.5, x: Math.round(value.KPI[vm.scatter.abscissa]*100)/100, y: Math.round(value.KPI[vm.scatter.ordinate]*100)/100 })
					});
				});
				drawGraph(vm.data)
			})
		}
		
		getData.async('legend', function(response) {
			vm.legend = response.data
		})
		vm.plotChart()
		
		var getOptionIndex = function(option) {
			var optionData = vm.filterDictionary[option]
			console.log(option, optionData)
			return optionData.index
		}
		
		var getFilterGroups = function() {
			var filtering = (vm.scatter.filter != '')
			if (filtering) $.each(vm.legend.selections, function(k, v) {
				if (v.type == 'lookup' && v.field == vm.scatter.filter) {
					$.each(v.lookup, function(i, obj) {
						$.each(obj, function(option, name) {
							vm.data.push({ key: option, values: [] })
							vm.filterDictionary[name] = { key: option, label: name, index: vm.data.length-1 }
						})
					})
				}
			})
			else {
				vm.filterDictionary = {}
				vm.scatter.filter = ''
			}
			return filtering
		}
		
		var drawGraph = function(data) {
			$('#scatterplot svg').empty()
			// create the chart
			var chart;
			nv.addGraph(function() {
				chart = nv.models.scatterChart()
					.showDistX(true)
					.showDistY(true)
					.useVoronoi(true)
					.duration(300)
					.showLegend(false)
				;
				chart.dispatch.on('renderEnd', function(){
					console.log('render complete');
				});
				chart.xAxis.tickFormat(d3.format('.02f'));
				chart.yAxis.tickFormat(d3.format('.02f'));
				d3.select('#scatterplot svg').datum(data).call(chart);
				nv.utils.windowResize(chart.update);
				chart.dispatch.on('stateChange', function(e) { ('New State:', JSON.stringify(e)); });
				return chart;
			});
		}
		
	}]);
}());

(function () {
	'use strict';
	angular.module('fiBenchmarkApp').service('getData', ['$http', '$q',  function ($http, $q) {
		return {
			async: function(type, postprocess) {
				var url = ( (type == 'legend') ? '../../service?action=plot-legend' : '../../service?action=plot&id=ca02bb9b-b282-461a-80e3-db77c073971b' )
				$http.get(url).then(postprocess)
			}
		}
	}]);
}());