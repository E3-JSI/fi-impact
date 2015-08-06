//Practically all this code comes from https://github.com/alangrafu/radar-chart-d3
//For a bit of extra information check the blog about it:
//http://nbremer.blogspot.nl/2013/09/making-d3-radar-chart-look-bit-better.html

var fiRadar = function (id, data, options) {
	var opt = {
		r: 5, w: 500, h: 500, rad: 2 * Math.PI,
		factor: 1, factorLegend: .85, levels: 5,
		max: Math.max(0, d3.max(data, function(i) {	return d3.max(i.map( function(o) {return o.value;} )); })),
		opacityArea: 0.5,
		ToRight: 5,
		TranslateX: 80, TranslateY: 30,
		ExtraWidthX: 150, ExtraWidthY: 100,
		// color: d3.scale.category20c().domain([0, 1])
		color: ['#6baed6', '#1f3e6f']
	};
	if (options) { for (var i in options) {if (options[i]) { opt[i] = options[i]; }} }
	opt.axesNum = data[0].length;
	opt.axesNames = data[0].map(function(i, j){return i.axis});
	opt.radius = opt.factor * Math.min(opt.w, opt.h) / 2;
	
	function levelFactor(i) {
		return (i+1) * opt.factor * opt.radius / opt.levels;
	}
	function baseTranslate(axis, i) {
		var val = ( (axis == "x") ? opt.w : opt.h );
		return val / 2 - levelFactor(i, opt);
	}
	function angle(axis, i) {
		var val = i * opt.rad / opt.axesNum;
		if (axis == "x") { return Math.sin(val); }
		else { return Math.cos(val); }
	}
	
	this.draw = function(id) {
		var Format = d3.format('%');
		d3.select(id).select("svg").remove();
		var g = d3.select(id).append("svg")
			.attr("width", opt.w + opt.ExtraWidthX).attr("height", opt.h + opt.ExtraWidthY)
			.append("g").attr("transform", "translate(" + opt.TranslateX + "," + opt.TranslateY + ")");
		var tooltip;
		series = 0;
		
		//Circular segments
		for ( var j = 0; j < (opt.levels - 1); j++ ) {
			fac = levelFactor(j, opt);
			g.selectAll(".levels").data(opt.axesNames).enter().append("svg:line")
				.attr("x1", function(d, i) { return fac * (1 - opt.factor * angle('x', i, opt)); })
				.attr("y1", function(d, i) { return fac * (1 - opt.factor * angle('y', i, opt)); })
				.attr("x2", function(d, i) { return fac * (1 - opt.factor * angle('x', i+1, opt)); })
				.attr("y2", function(d, i) { return fac * (1 - opt.factor * angle('y', i+1, opt)); })
				.attr("class", "line")
				.style("stroke", "grey").style("stroke-opacity", "0.75").style("stroke-width", "0.3px")
				.attr("transform", "translate(" + (opt.w/2-fac) + ", " + (opt.h/2-fac) + ")");
		}
		
		//Text indicating at what % each level is
		for ( var j = 0; j < opt.levels; j++ ) {
			g.selectAll(".levels").data([1]) //dummy data
				.enter().append("svg:text")
				.attr("x", function(d) { return levelFactor(j)*(1-opt.factor*Math.sin(0)); })
				.attr("y", function(d) { return levelFactor(j)*(1-opt.factor*Math.cos(0)); })
				.attr("class", "legend")
				.style("font-family", "sans-serif").style("font-size", "10px")
				.attr("transform", "translate(" + (baseTranslate('x', j) + opt.ToRight) + ", " + baseTranslate('y', j) + ")")
				.attr("fill", "#737373").text(Format((j+1) * opt.max/opt.levels));
		}
		
		var axis = g.selectAll(".axis").data(opt.axesNames).enter().append("g").attr("class", "axis");
		axis.append("line").attr("x1", opt.w/2).attr("y1", opt.h/2)
			.attr("x2", function(d, i){return opt.w/2*(1-opt.factor * angle('x', i));})
			.attr("y2", function(d, i){return opt.h/2*(1-opt.factor * angle('y', i));})
			.attr("class", "line").style("stroke", "grey").style("stroke-width", "1px");
		axis.append("text").attr("class", "legend").text(function(d) {return d})
			.style("font-family", "sans-serif").style("font-size", "11px")
			.attr("text-anchor", "middle").attr("dy", "1.5em")
			.attr("transform", "translate(0, -10)")
			.attr("x", function(d, i) { return opt.w/2*(1-opt.factorLegend*angle('x', i))-60*angle('x', i); })
			.attr("y", function(d, i) { return opt.h/2*(1-angle('y', i))-20*angle('y', i); });
		
		data.forEach( function(y, x) {
			dataValues = [];
			g.selectAll(".nodes").data(y, function(j, i){
				var modifier = (parseFloat(Math.max(j.value, 0))/opt.max) * opt.factor;
				dataValues.push([opt.w / 2 * (1 - modifier * angle('x', i)), opt.h / 2 * (1 - modifier * angle('y', i))]);
			});
			dataValues.push(dataValues[0]);
			g.selectAll(".area").data([dataValues]).enter().append("polygon").attr("class", "radar-chart-serie"+series)
				.style("stroke-width", "2px").style("stroke", opt.color[series]).attr("points", function(d) {
					var str = "";
					for ( var pti = 0; pti < d.length; pti++ ) { str = str + d[pti][0] + "," + d[pti][1] + " "; }
					return str;
				}).style("fill", function(j, i){return opt.color[series]}).style("fill-opacity", opt.opacityArea)
				.on('mouseover', function (d) {
					z = "polygon." + d3.select(this).attr("class");
					g.selectAll("polygon").transition(200).style("fill-opacity", 0.1); 
					g.selectAll(z).transition(200).style("fill-opacity", .7);
				}).on('mouseout', function() { g.selectAll("polygon").transition(200).style("fill-opacity", opt.opacityArea); });
		  series++;
		});
		series = 0;
		
		data.forEach(function(y, x){
			g.selectAll(".nodes").data(y).enter().append("svg:circle")
			.attr("class", "radar-chart-serie"+series).attr('r', opt.r).attr("alt", function(j){return Math.max(j.value, 0)})
			.attr("cx", function(j, i) {
				modifier = (parseFloat(Math.max(j.value, 0))/opt.max)*opt.factor;
				dataValues.push([opt.w / 2 * (1 - modifier * angle('x', i)), opt.h / 2 * (1 - modifier * angle('y', i))]);
				return opt.w/2*(1-(Math.max(j.value, 0)/opt.max)*opt.factor * angle('x', i));
			}).attr("cy", function(j, i){ return opt.h / 2 * (1 - (Math.max(j.value, 0)/opt.max) * opt.factor * angle('y', i)); })
			.attr("data-id", function(j){return j.axis}).style("fill", opt.color[series]).style("fill-opacity", .9)
			.on('mouseover', function (d) {
				newX =  parseFloat(d3.select(this).attr('cx')) - 10;
				newY =  parseFloat(d3.select(this).attr('cy')) - 5;
				tooltip.attr('x', newX).attr('y', newY).text(Format(d.value)).transition(200).style('opacity', 1);
				z = "polygon."+d3.select(this).attr("class");
				g.selectAll("polygon").transition(200).style("fill-opacity", 0.1);
				g.selectAll(z).transition(200).style("fill-opacity", .7);
			}).on('mouseout', function(){
				tooltip.transition(200).style('opacity', 0);
				g.selectAll("polygon").transition(200).style("fill-opacity", opt.opacityArea);
			}).on('click', function() {
				window.location.href = "http://www.google.com";
				//window.open(url,'_blank');
			}).append("svg:title").text(function(j){return Math.max(j.value, 0)});
			series++;
		});

		//Tooltip
		tooltip = g.append('text').style('opacity', 0).style('font-family', 'sans-serif').style('font-size', '13px');
	}
};

