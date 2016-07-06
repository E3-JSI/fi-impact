(function () {
	'use strict';
	angular.module('fiBenchmarkApp', ['ngSanitize', 'ui.bootstrap']);
}());

(function () {
	'use strict';
	angular.module('fiBenchmarkApp').controller('fiGraphs', ['$scope', 'getData', function($scope, getData) {
	
		var vm = this;
		
		vm.id = location.search.split('id=')[1]
		vm.active = 'scatter'
		vm.loaded = { 'scatter': false, 'similarity': false }
		vm.legend = {}
		vm.raw = { scatter: {}, similarity: {} }
		vm.similarity = {}
		vm.filter = ''
		vm.scatter = { abscissa: '', ordinate: '', data: '' }
		vm.symbols = ['circle', 'diamond', 'square', 'cross']
		vm.symbolLegend = {
			'circle': { code: '&#9679;', color: '', size: 0.1 },
			'diamond': { code: '&#9830;', color: '', size: 0.2 },
			'square': { code: '&#9632;', color: '', size: 0.3 },
			'cross': { code: '&#10010;', color: '', size: 0.4 }
		}
		vm.colors = ['#cccccc', '#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b']
		vm.lights = ['#E6E6E6', '#3991CE', '#FF9928', '#46BA46', '#F04142', '#AE81D7', '#A67065']
		
		vm.getColor = function(obj) {
			if (obj.key == 'NA') return vm.colors[0]
			else return ( (obj.checked) ? vm.colors[1] : vm.colors[2] )
		}
		vm.getTypeColor = function(obj) {
			if (vm.filter.field == 'none' && (obj.index < vm.symbols.length)) return vm.symbolLegend[vm.symbols[obj.index]].color
			else return '#999999'
		}
		vm.showPlot = function(tab) {
			vm.active = tab
			$('.fi-plot').hide()
			$('#' + tab + 'plot').show()
			if (vm.loaded[tab]) drawGraph()
		}
		vm.plotChart = function() {
			if (vm.active == 'scatter') updateScatter()
			else vm.similarity = updateSimilarity()
			drawGraph()
		}
		
		var checkboxDisplayCategories = ['checked', 'unchecked']
		
		getData.async('plot-legend', function(response) {
			vm.legend = response.data
			vm.legend.selections.unshift({
				field: 'none',
				label: 'None',
				lookup: [],
				type: 'none'
			})
			vm.filter = vm.legend.selections[0]
			vm.legend.slices = []
			for (var i = 0; i < vm.legend.KPI.length; i++) for (var j = i+1; j < vm.legend.KPI.length; j++) 
				(vm.legend.slices).push({abscissa: vm.legend.KPI[i], ordinate: vm.legend.KPI[j], index: i + '-' + j })
			vm.legend.type = {}
			vm.scatter.slice = vm.legend.slices[0]
			
			getData.async('plot', function(response) {
				var order =  []
				$.each(response.data.surveys, function(type, list) { order.push( {type: type, list: list} ) })
				order.sort(function(a, b) {return b.list.length - a.list.length})
				$.each(order, function(index, data) {
					vm.raw.scatter[data.type] = data.list
					vm.legend.type[data.type] = { index: Object.keys(vm.legend.type).length, checked: true }
				})
				vm.selected = vm.raw.scatter.SELECTED[0]
				vm.selected.nodeShape = vm.symbols[vm.legend.type[vm.selected.info.node_type].index]
				vm.loaded.scatter = true
				updateScatter()
				drawGraph()
			})
			
			getData.async('q-get-graph', function(response) {
				sigma.utils.pkg('sigma.canvas.nodes');
				vm.raw.similarity = response.data
				vm.similarity = updateSimilarity()			   
				vm.loaded.similarity = true
				drawGraph()
			})
		})
		
		/* 
			Clear vm.scatter.data object. Determine if a filter is chosen and prepare data groups.
			Build filter option dictionary.
			If no filter is chosen, the groups are types. For a lookup filter the groups are options. For a categorical filter the groups are checked vs. unchecked categories.
			If the filter has more than 3 options, add property checked to the dictionary and prepare two data groups (not/selected categories)
		*/
		var updateScatter = function() {
			vm.scatter.data = [{ key: 'NA', values: [] }]
			var filtering = (vm.filter.field != 'none')
			// build the dictionary if it does not exist yet
			if (filtering) {
				var selectedValue = vm.selected.filters[vm.filter.field]
				vm.filter.displayType = ( (vm.filter.lookup.length > 5) || (vm.filter.type == 'multi') ? 'checkbox' : 'list' )
				if (vm.filter.displayType == 'checkbox') $.each(checkboxDisplayCategories, function(i, cat) {vm.scatter.data.push({ key: cat, values: [] })})
				if (vm.filter.displayType == 'list') $.each(vm.filter.lookup, function(i, cat) {
					$.each(cat, function(key, label) { vm.scatter.data.push({ key: key, values: [] }) })
				})
				if (!('dictionary' in vm.filter)) {
					vm.filter.dictionary = { NA: { key: 'NA', label: 'NA', index: 0 } }
					$.each(vm.filter.lookup, function(i, obj) {
						$.each(obj, function(option, name) {
							if (vm.filter.displayType == 'list') vm.scatter.data.push({ key: option, values: [] })
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
			// process the data
			$.each(vm.raw.scatter, function(type, list) {
				var thisType = vm.legend.type[type]
				var typeIndex = thisType.index
				var typeShape = ( (type == 'SELECTED') ? vm.selected.nodeShape : vm.symbols[typeIndex] )
				if (type == 'SELECTED') vm.selected.nodeColor = vm.colors[1+typeIndex]
				if (!filtering) {
					vm.scatter.data.push({ key: type, values: [] })
					if (type != 'SELECTED') vm.symbolLegend[typeShape].color = vm.colors[1+typeIndex]
					if (type != 'SELECTED') vm.symbolLegend[typeShape].light = vm.lights[1+typeIndex]
				}
				if (thisType.checked) $.each(list, function(i, value) {
					var ind = ( filtering ? getOptionIndex(value) : 1+typeIndex )
					
					vm.scatter.data[( vm.scatter.data[ind] ? ind : 0 )].values.push({
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
		}
		
		var updateSimilarity = function() {
			var data = vm.raw.similarity
			var dict = {};
			var uniqEdges = [];
			for (var i = 0; i < data.edges.length; i++) {
				if (!dict.hasOwnProperty(data.edges[i].id)) {
					dict[data.edges[i].id] = 1;
					uniqEdges.push(data.edges[i]);
				}
			}
			for (var i = 0; i < data.nodes.length; i++) {
				var thisSymbol = typeSymbol(data.nodes[i].node_type)
				var d = data.nodes[i].deg
				data.nodes[i].type = thisSymbol
				data.nodes[i].color = ( (data.nodes[i].node_type == 'SELECTED') ? '#000000' : vm.symbolLegend[thisSymbol].light)
				data.nodes[i].borderColor = ( (data.nodes[i].node_type == 'SELECTED') ? '#000000' : vm.symbolLegend[thisSymbol].color)
				data.nodes[i].label = data.nodes[i].extra_data.Q1_4
				data.nodes[i].size = ( (data.nodes[i].node_type == 'SELECTED') ? 1 : d*d*d)
				data.nodes[i].hidden = !vm.legend.type[data.nodes[i].node_type].checked
				if (vm.filter.field != 'none') {
					var ind = getOptionIndex(data.nodes[i].extra_data)
					if (ind == false) ind = 0
					data.nodes[i].color = vm.lights[ind]
					data.nodes[i].borderColor = vm.colors[ind]
				}
			}
			return {nodes: data.nodes, edges: uniqEdges}
		}
		
		var typeSymbol = function(type) { return ( (type == 'SELECTED') ? vm.selected.nodeShape : vm.symbols[vm.legend.type[type].index] ) }
		var typeSize = function(type) { return ( type=='SELECTED' ? 1 : vm.symbolLegend[typeSymbol(type)].size ) }
		
		// Determine the index of a group a value should go into
		var getOptionIndex = function(value) {
			var option = ( (value.filters) ? value.filters[vm.filter.field] : value[vm.filter.field] )
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
		
		var dataLengths = function() {
			var sum = 0
			$.each(vm.raw.scatter, function(key, list) {
				// console.log(key + ': ' + list.length)
				sum += list.length
			})
			console.log('raw: ' + sum)
			sum = 0
			$.each(vm.scatter.data, function(i, object) {
				console.log(object.key + ': ' + object.values.length)
				// console.log(object.key)
				sum += object.values.length
			})
			console.log('data: ' + sum)
		}
		
		var drawGraph = function() {
			if (vm.active == 'scatter') {
				$('#scatterplot svg').empty()
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
					d3.select('#scatterplot svg').datum(vm.scatter.data).call(chart);
					nv.utils.windowResize(chart.update);
					return chart;
				});
			}
			if (vm.active == 'similarity') {
				$('#similarityplot').empty()
				var similarity = new sigma({
					graph: vm.similarity,
					renderer: { container: document.getElementById('similarityplot'), type: 'canvas' },
					settings: { minNodeSize: 4, maxNodeSize: 16, labelThreshold: 12 }
				})
				CustomShapes.init(similarity)
				similarity.startForceAtlas2({worker: true, barnesHutOptimize: false, gravity: 5, strongGravityMode: false});
				setTimeout(function(){ similarity.stopForceAtlas2(); }, 3000); 
			}
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
				var url = '../../service?action=' + type + ( (type != 'legend') ? '&id='+id : '' )
				$http.get(url).then(postprocess)
			}
		}
	}]);
}());