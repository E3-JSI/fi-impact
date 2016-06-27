(function () {
	'use strict';
	angular.module('fiBenchmarkApp', ['ngSanitize', 'ui.bootstrap']);
}());

(function () {
	'use strict';
	angular.module('fiBenchmarkApp').controller('fiGraphs', ['$scope', 'getData', function($scope, getData) {
	
		var vm = this;
		
		vm.legend = {}
		
		vm.scatter = { abscissa: '', ordinate: '' }
		vm.filter = ''
		vm.symbols = ['circle', 'diamond', 'square', 'cross']
		vm.symbolLegend = {
			'circle': { 'code': '&#9679;', 'color': '', 'size': 0.1 },
			'diamond': { 'code': '&#9830;', 'color': '', 'size': 0.2 },
			'square': { 'code': '&#9632;', 'color': '', 'size': 0.3 },
			'cross': { 'code': '&#10010;', 'color': '', 'size': 0.4 }
		}
		vm.colors = ['#cccccc', '#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b', '#e377c2', '#7f7f7f', '#bcbd22', '#17becf', '#aec7e8', '#ffbb78', '#98df8a', '#ff9896', '#c5b0d5', '#c49c94', '#f7b6d2', '#c7c7c7', '#dbdb8d', '#9edae5']
		
		vm.raw = {}
		vm.total = 0
		vm.showNA = false
		vm.tabs = []
		
		var checkboxDisplayCategories = ['checked', 'unchecked']
		
		getData.async('legend', function(response) {
			vm.legend = response.data
			vm.legend.selections.unshift({
				'field': 'none',
				'label': 'None',
				'lookup': [],
				'type': 'none'
			})
			vm.filter = vm.legend.selections[0]
			vm.legend.slices = []
			for (var i = 0; i < vm.legend.KPI.length; i++) for (var j = i+1; j < vm.legend.KPI.length; j++) 
				(vm.legend.slices).push({abscissa: vm.legend.KPI[i], ordinate: vm.legend.KPI[j]})
			vm.legend.type = {}
			vm.scatter.slice = vm.legend.slices[0]
		})
		
		getData.async('data', function(response) {
			var order =  []
			vm.total = response.data.total
			$.each(response.data.surveys, function(type, list) { order.push( {type: type, list: list} ) })
			order.sort(function(a, b) {return b.list.length - a.list.length})
			$.each(order, function(index, data) {
				vm.raw[data.type] = data.list
				vm.legend.type[data.type] = { index: Object.keys(vm.legend.type).length, checked: true }
			})
			vm.selected = vm.raw.SELECTED[0]
			vm.plotChart()
		})
		
		// $.getJSON( "../../service?id=" + location.search.split('id=')[1] + "&action=q-get-graph", function(data) { drawSimilarityGraph(data); });
		
		/* 
			Fill data groups.
			If no filter is chosen, the groups are types. For a lookup filter the groups are options. For a categorical filter the groups are checked vs. unchecked categories.
		*/
		vm.plotChart = function() {
			var filtering = getFilterGroups()
			// dataLengths()
			$.each(vm.raw, function(type, list) {
				var thisType = vm.legend.type[type]
				var typeIndex = thisType.index
				var typeShape = ( (type == 'SELECTED') ? 'circle' : vm.symbols[typeIndex] )
				if (!filtering) {
					vm.data.push({ key: type, values: [] })
					vm.symbolLegend[typeShape].color = vm.colors[1+typeIndex]
				}
				if (thisType.checked) $.each(list, function(i, value) {
					var ind = ( filtering ? getOptionIndex(value) : 1+typeIndex )
					vm.data[( vm.data[ind] ? ind : 0 )].values.push({
						label: value.info.Q1_3,
						size: typeSize(type),
						shape: typeShape,
						indicatorX: Math.round(value.KPI[vm.scatter.slice.abscissa.field]*100)/100,
						indicatorY: Math.round(value.KPI[vm.scatter.slice.ordinate.field]*100)/100,
						x: Math.round(value.KPI[vm.scatter.slice.abscissa.field]*100)/100,
						y: Math.round(value.KPI[vm.scatter.slice.ordinate.field]*100)/100
					})
				})
			});
			vm.showNA = ( vm.data[0].values.length > 0 )
			// dataLengths()
			drawGraph(vm.data)
		}
		
		vm.getColor = function(obj) {
			if (obj.key == 'NA') return vm.colors[0]
			else return ( (obj.checked) ? vm.colors[1] : vm.colors[2] )
		}
		vm.getTypeColor = function(obj) {
			if (vm.filter.field == 'none' && (obj.index < vm.symbols.length)) return vm.symbolLegend[vm.symbols[obj.index]].color
			else return '#999999'
		}
		vm.showPlot = function(tab) {
			$('.fi-plot').hide()
			$('#' + tab + 'plot').show()
		}
		
		var typeSymbol = function(type) { return ( (type == 'SELECTED') ? 'circle' : vm.symbols[vm.legend.type[type].index] ) }
		var typeSize = function(type) { return ( type=='SELECTED' ? 1 : vm.symbolLegend[typeSymbol(type)].size ) }
		
		// Determine the index of a group a value should go into
		var getOptionIndex = function(value) {
			var option = value.filters[vm.filter.field]
			if (!option) return false
			if (vm.filter.displayType == 'checkbox') {
				var options = []
				var checked = false
				if (vm.filter.type == 'multi') $.each(option.split(','), function(i, opt) { options.push(opt) })
				else options.push(option)
				$.each(options, function(i, opt) {
					if (!vm.filter.dictionary[option]) return false
					checked = checked || vm.filter.dictionary[option].checked
				})
				return ( checked ? 1 : 2 )
			}
			else return vm.filter.dictionary[option].index
		}
		
		/* 
			Clear vm.data object. Determine if a filter is chosen and prepare data groups.
			Build filter option dictionary.
			If the filter has more than 3 options, add property checked to the dictionary and prepare two data groups (not/selected categories)
		*/
		var getFilterGroups = function() {
			vm.data = [{ key: 'NA', values: [] }]
			var filtering = (vm.filter.field != 'none')
			// build the dictionary if it does not exist yet
			if (filtering) {
				var selectedValue = vm.selected.filters[vm.filter.field]
				vm.filter.displayType = ( vm.filter.lookup.length > 3 ? 'checkbox' : 'list' )
				if (vm.filter.displayType == 'checkbox') $.each(checkboxDisplayCategories, function(i, cat) {vm.data.push({ key: cat, values: [] })})
				if (!('dictionary' in vm.filter)) {
					vm.filter.dictionary = { NA: { key: 'NA', label: 'NA', index: 0 } }
					$.each(vm.filter.lookup, function(i, obj) {
						$.each(obj, function(option, name) {
							if (vm.filter.displayType == 'list') vm.data.push({ key: option, values: [] })
							vm.filter.dictionary[option] = { key: option, label: name, index: Object.keys(vm.filter.dictionary).length }
							if (vm.filter.displayType == 'checkbox') {
								var isChecked = false
								if (vm.filter.type == 'multi') isChecked = (selectedValue.split(',').indexOf(option) >= 0)
								else isChecked = ( selectedValue == (vm.filter.type=='lookup' ? option : name) )
								vm.filter.dictionary[option].checked = isChecked
							}
						})
					})
				}
			}
			return filtering
		}
		
		var dataLengths = function() {
			var sum = 0
			$.each(vm.raw, function(key, list) {
				// console.log(key + ': ' + list.length)
				sum += list.length
			})
			console.log('raw: ' + sum)
			sum = 0
			$.each(vm.data, function(i, object) {
				console.log(object.key + ': ' + object.values.length)
				// console.log(object.key)
				sum += object.values.length
			})
			console.log('data: ' + sum)
		}
		
		var drawGraph = function(data) {
			$('#scatterplot svg').empty()
			// create the chart
			var chart;
			nv.addGraph(function() {
				chart = nv.models.scatterChart()
					.useVoronoi(true)
					.color(vm.colors)
					.duration(300)
					.showLegend(false)
				chart.xAxis.tickFormat(d3.format('.02f')).axisLabel(vm.scatter.slice.abscissa.label)
				chart.yAxis.tickFormat(d3.format('.02f')).axisLabel(vm.scatter.slice.ordinate.label)
				chart.tooltip.contentGenerator(fiContentGenerator)
				d3.select('#scatterplot svg').datum(data).call(chart);
				nv.utils.windowResize(chart.update);
				return chart;
			});
		}
		
		var fiContentGenerator = function(d) {
			if (d === null) return '';

			var table = d3.select(document.createElement("table"));
			var tbodyEnter = table.selectAll("tbody").data([d]).enter().append("tbody");

			var trowEnter = tbodyEnter.append("tr").classed("highlight", function(p) { return p.highlight});

			trowEnter.append("td")
				.classed("key",true)
				.classed("total",function(p) { return !!p.total})
				.html(function(p, i) { return vm.scatter.slice.abscissa.label});

			trowEnter.append("td")
				.classed("value",true)
				.html(function(p, i) { return d.point.indicatorX });

			trowEnter = tbodyEnter.append("tr").classed("highlight", function(p) { return p.highlight});
			
			trowEnter.append("td")
				.classed("key",true)
				.classed("total",function(p) { return !!p.total})
				.html(function(p, i) { return vm.scatter.slice.ordinate.label});

			trowEnter.append("td")
				.classed("value",true)
				.html(function(p, i) { return d.point.indicatorY });

			trowEnter.selectAll("td").each(function(p) {
				if (p.highlight) {
					var opacityScale = d3.scale.linear().domain([0,1]).range(["#fff",p.color]);
					var opacity = 0.6;
					d3.select(this)
						.style("border-bottom-color", opacityScale(opacity))
						.style("border-top-color", opacityScale(opacity))
					;
				}
			});

			var html = ( (d.point.label) ? '<p style="text-align: left; font-weight: bold;">' + d.point.label + "</p>" : '' );
				html += table.node().outerHTML;
			return html;

		};

		
	}]);
}());

(function () {
	'use strict';
	angular.module('fiBenchmarkApp').service('getData', ['$http', '$q',  function ($http, $q) {
		var id = location.search.split('id=')[1]
		return {
			async: function(type, postprocess) {
				var url = ( (type == 'legend') ? '../../service?action=plot-legend' : '../../service?action=plot&id='+id )
				$http.get(url).then(postprocess)
			}
		}
	}]);
}());