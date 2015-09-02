var fiData = function (id) { // id is optional
	var locationAverages = '../../service?action=averages';
	var locationData = '../../service?action=results&id=' + id;
	var locationManager = '../../manager?action=list';
	// var model = require(['../../js/fiModel.js']);
	loadMaxValues();
	
	if (typeof id != 'undefined') {
		// load project data
		this.survey = getJSON(locationData);
		this.questions = getQuestions(this.survey.questions);
		this.results = getResults(this.survey.results);
		// load averages
		this.data = getJSON(locationAverages);
		this.averages = getKeys(this.data.results);
		this.scores = getScores(this.averages.keys, this.results); // unit: percent
	}
	else {
		// load list
		this.manager =  getJSON(locationManager);
	}
	
	// * * * * * * * * * *
	// internal functions
	// * * * * * * * * * *
	
	function loadMaxValues() {
		$.each(['A', 'B'], function(i, v) { $.each(model['s6' + v].q1, function(j, w) {
				model.max['social' + v + '_' + j] = model.max.social;
		}); });
	}
	
	function getJSON(jsonUrl) {
		var result = null;
		$.ajax({ url: jsonUrl, type: 'get', dataType: 'json', async: false, success: function(data) { result = data; } });
		return result;
	}
	
	function fiBool(v) {
		if (v == "A") { return "Yes"; }
		if (v == "B") { return "No"; }
		return;
	}
	
	function getQuestions(list) {
		var result = {};
		$.each(list, function(i, v) {
			if (v[0] == "Q2_1") { result.trl = v[1]; }
			var qId = v[0].slice(1);
			var aId = qId.split('_');
			var t = (( model['s' + aId[0]] || {} )['q' + aId[1]] || {} )[v[1]];
			if (inArray(model.bool, qId)) { t = fiBool(v[1]); }
			if (!t) {
				if (inArray(model.list, qId)) { t = v[1].replace(/,/g, ", ").replace(" ,", ","); }
				else { t = v[1]; }
			}
			result[v[0]] = { id: qId, idJSON: v[0], value: v[1], text: t };
		});
		return result;
	}
	
	function getResults(list) {
		var result = {};
		$.each(list, function( i, v ) { result[v[0]] = v[1]; });
		return result;
	}
	
	function scoreKeysReplace(s) {
		$.each(model.scoreKeys, function(i, v) { s = s.replace(i, v); });
		return s;
	}
	
	function cutOff(score, key) {
		m = model.max[key];
		return ( (score > m) ? m : score );
	}
	
	function getKeys(list) {
		var result = { keys: {}, values: {} };
		$.each(list, function( i, v ) {
			key = result.keys[v.id] = scoreKeysReplace(v.id);
			result.values[key] = { average: cutOff(v.average, key), histogram: v.histogram, slot: v.average_slot };
		});
		return result;
	}
	
	function getScores(list, data) {
		var result = {};
		$.each(list, function(i, v) { result[v] = cutOff(data[i], i) });
		return result;
	}
};

fiData.prototype.getVerboseList = function(list, dictionary) {
	var result = [];
	$.each(list, function(i, v) { if (dictionary[v]) { result.push(dictionary[v]); } });
	return result;
}

fiData.prototype.getSpeedometerSettings = function(key) {
	var h = fi.averages.values[key].histogram;
	return {
		levels: [0, model.lmh[key][0], model.lmh[key][1], 1],
		percent: ( (fi.scores[key] < model.max[key]) ? fi.scores[key] / model.max[key] : 1 ),
		list: [h[0]+h[1], h[2], h[3]+h[4]],
		average: fi.averages.values[key].average / model.max[key],
		tooltips: model.tooltips
	};
}

fiData.prototype.verboseFromJSON = function(s, q) {
	var t = [];
	$.each(fi.questions['Q' + s + '_' + q].value.split(','), function(i, v) {
		if (model['s' + s]['q' + q][v]) { t.push(model['s' + s]['q' + q][v]); }
	});
	return t.join( ", " );
}

fiData.prototype.getAnswersList = function(question, A) {
	var result = [];
	var q = 'Q' + question;
	var qId = question.split('_');
	var s = 's' + qId[0];
	var a = 'a' + qId[1];
	$.each(model[q], function(i, v) {
		if ( fi.questions[q] && (fi.questions[q].value == A) ) { result.push(model[s][a][v.slice(1)]); }
	});
	return result.join( ", " );
}

fiData.prototype.getMarketNeedsStarScores = function(key) {
	var result = {};
	$.each(model.s5A.q1, function(i, v) {
		val = ( fi.questions["Q5A_1_" + i] ? parseInt(fi.questions["Q5A_1_" + i].value) : 0 );
		result[i] = { name: v, stars: val, x: val };
	});
	return result;
}

fiData.prototype.getQ3_5text = function(A) {
	var result = [];
	$.each(fi.questions.Q3_5.value.split(','), function(i, v) {
		switch(v) {
			case "A": result.push(model.s3.q5.A + (fi.questions.Q1_17 ? " (" + fi.questions.Q1_17.value + ")" : '')); break;
			case "B": result.push(model.s3.q5.B + (fi.questions.Q1_12 ? " (" + fi.questions.Q1_2.value + ")" : '')); break;
			case "C": result.push(model.s3.q5.C + (fi.questions.Q3_5c ? " (" + fi.questions.Q3_5c.value.replace(/,/g, ", ") + ")" : '')); break;
			case "D": result.push(model.s3.q5.D); break;
			default: result.push(model.s3.q5.E);
		}
	});
	return result.join(", ");
}

fiData.prototype.makeRadarOverviewData = function() {
	data = [[],[]];
	$.each(model.radarOverview, function(i, v) {
		label = v.charAt(0).toUpperCase() + v.slice(1)
		data[0].push({axis: label, value: fi.averages.values[v].average / model.max[key]});
		data[1].push({axis: label, value: fi.scores[v] / model.max[key]});
	});
	return data;
}

fiData.prototype.makeRadarSocialData = function(A) {
	data = [[],[]];
	$.each(model['s6' + A].q1, function(i, v) {
		label = ( (A == "A") ? i : v );
		data[0].push({axis: label, value: fi.averages.values['social' + A + '_' + i].average});
		data[1].push({axis: label, value: fi.results['Q6' + A + '_1_' + i]});
	});
	return data;
}

