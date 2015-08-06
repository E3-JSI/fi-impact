function d3Speedometer(elementId, s) {
	var Needle, arc, arcEndRad, arcStartRad, barWidth, chart, chartInset, degToRad, el, endPadRad, height, margin, needle, numSections, padRad, percToDeg, percToRad, percent, radius, sectionIndx, sectionPerc, startPadRad, svg, totalPercent, width, _i;
	
	//s = settings: levels, percent, list, average
	
	pi = Math.PI;
	barWidth = 20;
	numSections = s.list.length;
	sectionPerc = 1 / numSections / 2;
	padRad = 0;
	chartInset = 0;
	totalPercent = .75;
	
	el = d3.select(elementId);

	margin = { top: 120, right: 30, bottom: 0, left: 30 };
	width = 200;
	height = 200;
	radius = 100;

	percToDeg = function(perc) { return perc * 360; };
	percToRad = function(perc) { return degToRad(percToDeg(perc)); };
	degToRad = function(deg) { return deg * pi / 180; };

	svg = el.append('svg').attr('width', width + margin.left + margin.right).attr('height', height);
	chart = svg.append('g').attr('transform', "translate(" + ((width / 2) + margin.left) + ", " + margin.top + ")");

	for (sectionIndx = _i = 1; 1 <= numSections ? _i <= numSections : _i >= numSections; sectionIndx = 1 <= numSections ? ++_i : --_i) {
		arcStartRad = percToRad(s.levels[_i-1]/2) - pi / 2;
		arcEndRad = percToRad(s.levels[_i]/2) - pi / 2;
		totalPercent += sectionPerc;
		startPadRad = sectionIndx === 0 ? 0 : padRad / 2;
		endPadRad = sectionIndx === numSections ? 0 : padRad / 2;
		arc = d3.svg.arc().outerRadius(radius - chartInset).innerRadius(radius - chartInset - barWidth).startAngle(arcStartRad + startPadRad).endAngle(arcEndRad - endPadRad);
		chart.append('path').attr('class', "arc chart-color" + sectionIndx).attr('d', arc);
		sec = d3.svg.arc().outerRadius(radius * s.list[_i-1] + 12).innerRadius(12).startAngle(arcStartRad + startPadRad).endAngle(arcEndRad - endPadRad);
		chart.append('path').attr('class', "arc chart-density" + sectionIndx).attr('d', sec);
	}

	Needle = (function() {
		function Needle(len, radius) { this.len = len; this.radius = radius; }
		Needle.prototype.drawOn = function(el, perc, x, y) {
			el.append('circle').attr('class', 'needle-center').attr('cx', x).attr('cy', y).attr('r', this.radius);
			el.append('path').attr('d', "M 50 50 L 40 40");
			return el.append('path').attr('class', 'needle').attr('d', this.mkCmd(perc));
		};
		Needle.prototype.animateOn = function(el, perc) {
			var self;
			self = this;
			return el.transition().delay(500).ease('elastic').duration(4000).selectAll('.needle').tween('progress', function() {
				return function(percentOfPercent) {
					var progress;
					progress = percentOfPercent * perc;
					return d3.select(this).attr('d', self.mkCmd(progress));
				};
			});
		};
		Needle.prototype.mkCmd = function(perc) {
			var centerX, centerY, leftX, leftY, rightX, rightY, thetaRad, topX, topY;
			thetaRad = percToRad(perc/2);
			centerX = 0;
			centerY = 0;
			topX = centerX - this.len * Math.cos(thetaRad);
			topY = centerY - this.len * Math.sin(thetaRad);
			leftX = centerX - this.radius * Math.cos(thetaRad - pi/2);
			leftY = centerY - this.radius * Math.sin(thetaRad - pi/2);
			rightX = centerX - this.radius * Math.cos(thetaRad + pi/2);
			rightY = centerY - this.radius * Math.sin(thetaRad + pi/2);
			return "M " + leftX + " " + leftY + " L " + topX + " " + topY + " L " + rightX + " " + rightY;
		};
		return Needle;
	})();
	
	Average = (function() {
		function Average(len, radius, external, perc) {
			this.len = len;
			this.radius = radius;
			this.external = external;
			this.perc = perc;
			this.thetaRad = percToRad(this.perc/2);
			this.x = -this.external * Math.cos(this.thetaRad);
			this.y = -this.external * Math.sin(this.thetaRad);
		}
		Average.prototype.drawOn = function(el) {
			el.append('circle').attr('class', 'needle-center').attr('cx', this.x).attr('cy', this.y).attr('r', this.radius);
			el.append('path').attr('d', "M 50 50 L 40 40");
			return el.append('path').attr('class', 'needle').attr('d', this.mkCmd(this.perc));
		};
		Average.prototype.mkCmd = function() {
			var centerX, centerY, leftX, leftY, rightX, rightY, topX, topY;
			centerX = this.x;
			centerY = this.y;
			topX = centerX - this.len * Math.cos(this.thetaRad + pi);
			topY = centerY - this.len * Math.sin(this.thetaRad + pi);
			rightX = centerX - this.radius * Math.cos(this.thetaRad - pi/2);
			rightY = centerY - this.radius * Math.sin(this.thetaRad - pi/2);
			leftX = centerX - this.radius * Math.cos(this.thetaRad + pi/2);
			leftY = centerY - this.radius * Math.sin(this.thetaRad + pi/2);
			return "M " + leftX + " " + leftY + " L " + topX + " " + topY + " L " + rightX + " " + rightY;
		};
		return Average;
	})();

	needle = new Needle(85, 8);
	needle.drawOn(chart, 0, 0, 0);
	needle.animateOn(chart, s.percent);

	avg = new Average(15, 3, radius, s.average);
	avg.drawOn(chart);
}