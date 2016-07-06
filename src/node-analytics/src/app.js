var express = require('express');
var app = express();
var qminer = require('./lib/qminer.js');
var qdata = require('./lib/data.js');
var bodyParser = require('body-parser');
var fs = require('fs');
var config = JSON.parse(fs.readFileSync('./config/main_config.json', 'utf8'));

var status = [];

app.use(express.static('public'));
app.use(express.static('files'));
app.use(bodyParser.json({limit: '50mb', parameterLimit: 100000}));       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({limit: '50mb', parameterLimit: 100000, extended: true})); // to support URL-encoded bodies

console.log({'time':new Date().toString(), "status":'init', 'ms': new Date().getTime()});
status.push({'time':new Date().toString(), "status":'init', 'ms': new Date().getTime()});	
	
app.post('/post_data', function(req, res) {
    var data = req.body;
	console.log({'time':new Date().toString(), "status":'recieved post data', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'recieved post data', 'ms': new Date().getTime()});
	qminer.fillPrjStore(data);
	qdata.savePrjData(data);
	console.log({'time':new Date().toString(), "status":'posted data stored', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'posted data stored', 'ms': new Date().getTime()});
	res.send({"status":"done"});
});

app.post('/send_record', function(req, res) {
    var data = req.body;
	console.log({'time':new Date().toString(), "status":'sending new record', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'sending new record', 'ms': new Date().getTime()});
	
	var inputId = data.id_internal;
		
	var files = qdata.getAllFilesFromFolder("../output/");
	var cashed = false;
	for (var i=0; i<files.length; i++) {
	    var arr = files[i].split('custom_graph_');
		if (arr.length > 0) {
		    if (arr[1] == inputId+".json") {
			    var graph = JSON.parse(fs.readFileSync('../output/custom_graph_'+arr[1], 'utf8'));
				console.log({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
	            status.push({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
			    cashed = true;
				res.send(graph);
			}
		}
	}
	
	if (!cashed) {
		var rec = null;
		var recset = qminer.prj.allRecords;
		for (var i=0; i<recset.length; i++) {
			if (recset[i].id_internal == inputId) {
				rec = recset[i];
			}
		}
		if (rec) {
			var graph = qminer.customGraph(rec);
			fs.writeFile("../output/custom_graph_"+inputId+".json", JSON.stringify(graph), function(err) {
				if (err) {
					return console.log(err);
				}
				console.log("The file was saved!");
			});
			console.log({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
			status.push({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
			res.send(graph);
		}
		else {
		    // new record
		    rec = data;
			console.log("send rec "+rec);
			var graph = qminer.sendRecord(rec);
			fs.writeFile("../output/custom_graph_"+inputId+".json", JSON.stringify(graph), function(err) {
				if (err) {
					return console.log(err);
				}
				console.log("The file was saved!");
			});
			console.log({'time':new Date().toString(), "status":'similarity graph for id '+ inputId + ' computed', 'ms': new Date().getTime()});
			status.push({'time':new Date().toString(), "status":'similarity graph for id '+ inputId + ' computed', 'ms': new Date().getTime()});
			res.send(graph);
		}
	}
});
 
app.get('/', function (req, res) {
    res.send('Hello World')
});

app.get('/fill_from_file', function (req, res) {
    status.push({'time':new Date().toString(), "status":'fill project store from data input file', 'ms': new Date().getTime()});
    qminer.fillPrjStoreFromFile();
    res.send({"status":"done"});
});

app.get('/status', function(req,res) {
    var stat = status[status.length-1];
	var msg = stat.status;
	out = {}
	if (stat.status == 'computing main similarity graph') {
	    var start = stat.ms;
		var end = new Date().getTime();
		var duration = end - start;
	    var progress = (duration/1000)/parseInt(config.MDS_time_sec);
		if (progress > 1.0) {
		    progress = 1;
		}
		out.progress = progress*100+"%";
	}
	out.status = msg;
    res.send(out);
});

app.get('/main_graph_async', function (req, res) {
    console.log({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
    qminer.mainGraphAsync();
	res.send({"status":'started computing main graph'});
});

app.get('/main_graph_full', function (req, res) {
    console.log({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
	qminer.mainGraph2();
	res.send({"status":'started computing main graph'});
});

app.get('/main_graph', function (req, res) {
    console.log({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
    var graph = qminer.mainGraph();
    // saving graph to file
    fs.writeFile("../output/"+config.main_graph_fn, JSON.stringify(graph), function(err) {
        if(err) {
            return console.log(err);
        }
        console.log("The file was saved!");
    });
    res.send({"status":'started computing main graph'});
});

app.get('/custom_graph/:n', function (req, res) {
	console.log({'time':new Date().toString(), "status":'computing similarity graph for id '+req.params.n, 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'computing similarity graph for id '+req.params.n, 'ms': new Date().getTime()});
	
	var inputId = req.params.n;
    //var rec = qminer.prj.allRecords[parseInt(req.params.n)];    
    //var rec = qminer.prj.allRecords.filter(function (rec) { return rec.internal_id == req.params.n; });
	
	var files = qdata.getAllFilesFromFolder("../output/");
	var cashed = false;
	for (var i=0; i<files.length; i++) {
	    var arr = files[i].split('custom_graph_');
		if (arr.length > 0) {
		    if (arr[1] == inputId+".json") {
			    var graph = JSON.parse(fs.readFileSync('../output/custom_graph_'+arr[1], 'utf8'));
				console.log({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
	            status.push({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
			    cashed = true;
				res.send(graph);
			}
		}
	}
	
	if (!cashed) {
		var rec = null;
		var recset = qminer.prj.allRecords;
		for (var i=0; i<recset.length; i++) {
			if (recset[i].id_internal == inputId) {
				rec = recset[i];
			}
		}
		if (rec) {
			var graph = qminer.customGraph(rec);
			fs.writeFile("../output/custom_graph_"+inputId+".json", JSON.stringify(graph), function(err) {
				if (err) {
					return console.log(err);
				}
				console.log("The file was saved!");
			});
			console.log({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
			status.push({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
			res.send(graph);
		}
		else {
			res.status(404).send('Not found');
		}
	}
});

app.get('/save_status', function(req, res) {
    console.log({'time':new Date().toString(), "status":'saving status log', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'saving status log', 'ms': new Date().getTime()});
    fs.writeFile("../output/log.json", JSON.stringify(status), function(err) {
        if(err) {
            return console.log(err);
        }
        console.log("The log file was saved!");
    });
	console.log({'time':new Date().toString(), "status":'status log saved', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'status log saved', 'ms': new Date().getTime()});
	res.send({"status":"success"});
});

app.get('/log_status', function(req, res) {
	res.send({"status":status});
});

app.get('/init', function (req, res) {
    qminer.init();
    res.send({"success":"done"});
});

app.get('/open', function (req, res) {
    qminer.open();
    res.send({"success":"done"});
});


app.get('/close', function (req, res) {
    qminer.close();
    res.send({"success":"done"});
});
 
app.listen(3000)