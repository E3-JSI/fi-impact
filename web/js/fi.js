var fiReport = function (id) {
	var locationAverages = '../../service?action=averages';
	var locationData = '../../service?action=results&id=' + id;
	var locationManager = '../../manager?action=list';
	// var model = require(['../../js/fiModel.js']);
	
	// load project data
	this.survey = getJSON(locationData);
	this.questions = getQuestions(this.survey.questions);
	this.results = getResults(this.survey.results);
	
	// load averages
	this.data = getJSON(locationAverages);
	this.averages = getKeys(this.data.results);
	this.scores = getScores(this.averages.keys, this.results);
	
	// load list
	this.manager =  getJSON(locationManager);
	
	// * * * * * * * * * *
	// internal functions
	// * * * * * * * * * *
	
	function getJSON(jsonUrl) {
		var result = null;
		$.ajax({ url: jsonUrl, type: 'get', dataType: 'json', async: false, success: function(data) { result = data; } });
		return result;
	}
	
	function getQuestions(list) {
		var result = {};
		$.each(list, function(i, v) {
			var qId = v[0].slice(1);
			var aId = qId.split('_');
			var t = (( model['s' + aId[0]] || {} )['q' + aId[1]] || {} )[v[1]];
			if (!t) {
				if (inArray(model.bool, qId)) { t = fiBool(v[1]); }
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
	
	function getKeys(list) {
		var result = { keys: {}, values: {} };
		$.each(list, function( i, v ) {
			key = result.keys[v.id] = scoreKeysReplace(v.id);
			if (!model.max[key]) { model.max[key] = 5; }
			s = v.average / model.max[key];
			result.values[key] = { average: ( (s > 1) ? 1 : s ), histogram: v.histogram };
		});
		return result;
	}
	
	function getScores(list, data) {
		var result = {};
		$.each(list, function(i, v) {
			s = data[i] / model.max[v];
			result[v] = ( (s > 1) ? 1 : s );
		});
		return result;
	}
};

fiReport.prototype.getVerboseList = function(list, dictionary) {
	var result = [];
	$.each(list, function(i, v) { if (dictionary[v]) { result.push(dictionary[v]); } });
	return result;
}

fiReport.prototype.getSpeedometerSettings = function(key) {
	var h = fi.averages.values[key].histogram;
	var x = .65 / fi.data.total;
	return {
		levels: [0, model.lmh[key][0], model.lmh[key][1], 1],
		percent: fi.scores[key],
		list: [(h[0]+h[1])*x, h[2]*x, (h[3]+h[4])*x],
		average: fi.averages.values[key].average
	};
}

fiReport.prototype.getSpeedometerText = function(key) {
	score = fi.scores[key] * model.max[key];
	avg = fi.averages.values[key].average * model.max[key];
	return "Your score is " + score + " out of " + model.max[key] + ". The average score of " + fi.data.total + " surveys is " + avg + ".";
}
// c. Where do I fit (compared to other – namesto xx% je boljših od vas rečemo je nn% projektov imelo podoben rezultat)
// d. Če je score pod polovico speedometra: You should look at improving “sector name” of your project
// e. če je score pod polovico speedometra: You might want to look at this documents for assistance (potem pa bi navedli povezave do nekaterih dokumentov: imeli bi fiksen seznam za vsako področje ter en dinamični querry, ki bi pobrskal po FI-IMPACT katalogu dokumentov in ponudil link na opravljen search znotraj portal – o tem delu se še pogovorimo kako si zamišljam)

fiReport.prototype.verboseFromJSON = function(s, q) {
	var t = [];
	$.each(fi.questions['Q' + s + '_' + q].value.split(','), function(i, v) {
		if (model['s' + s]['q' + q][v]) { t.push(model['s' + s]['q' + q][v]); }
	});
	return t.join( ", " );
}

fiReport.prototype.getAnswersList = function(question, A) {
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

fiReport.prototype.getMarketNeedsStarScores = function(key) {
	var result = {};
	$.each(model.s5A.q1, function(i, v) {
		val = ( fi.questions["Q5A_1_" + i] ? parseInt(fi.questions["Q5A_1_" + i].value) : 0 );
		result[i] = { name: v, stars: val, x: val };
	});
	return result;
}

fiReport.prototype.getQ3_5text = function(A) {
	var result = [];
	$.each(fi.questions.Q3_5.value.split(','), function(i, v) {
		switch(v) {
			case "A": result.push(model.s3.q5.A + (fi.questions.Q1_17 ? " (" + fi.questions.Q1_17.value + ")" : '')); break;
			case "B": result.push(model.s3.q5.B + (fi.questions.Q1_12 ? " (" + fi.Q1_12.value + ")" : '')); break;
			case "C": result.push(model.s3.q5.C + (fi.questions.Q3_5c ? " (" + fi.questions.Q3_5c.value.replace(/,/g, ", ") + ")" : '')); break;
			case "D": result.push(model.s3.q5.D); break;
			default: result.push(model.s3.q5.E);
		}
	});
	return result.join(", ");
}

fiReport.prototype.makeRadarSocialData = function(A) {
	data = [[],[]];
	$.each(model['s6' + A].q1, function(i, v) {
		label = ( (A == "A") ? i : v );
		data[0].push({axis: label, value: fi.questions['Q6' + A + '_1_' + i].value});
		data[1].push({axis: label, value: fi.averages.values['social' + A + '_' + i].average});
	});
	return data;
}

