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

/*
 * Importing data into the qminer db.
 */
app.post('/post_data', function(req, res) {
    var data = req.body;
	console.log({'time':new Date().toString(), "status":'recieved post data', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'recieved post data', 'ms': new Date().getTime()});
    
    // 1 = define schema based on data
    var schema = qminer.setPrjShema(data);
    // 2 - create qminer base based on schema
    qminer.createBase(schema);
    // 3 - fill qminer store with data
	qminer.fillPrjStore(data);
    // 4 - set new settings based on the posted settings
    qdata.data_settings = data.settings;
    // 5 - set feature space definitions
    qminer.setTextFts();
    qminer.setAllFts();
    // 6 - update feature space with data
    qminer.ftr.updateRecords(qminer.prj.allRecords);
    qminer.ftrAll.updateRecords(qminer.prj.allRecords);
    // 7 - save data to post_data file
	qdata.savePrjData(data);
	
    console.log({'time':new Date().toString(), "status":'posted data stored', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'posted data stored', 'ms': new Date().getTime()});
	res.send({"status":"done"});
});

/*
 * Computing similarity graph for a custum input record that doesent have to be a part of the model and db. 
 */
app.post('/custom_graph_full_record', function(req, res) {
    var data = req.body;
	console.log({'time':new Date().toString(), "status":'sending new record', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'sending new record', 'ms': new Date().getTime()});
	var inputId = data.id_internal;
    var files = qdata.getAllFilesFromFolder("../output/");
	// new record
	var	rec = data;
	console.log("send rec "+rec);
	var graph = qminer.sendRecord(rec);
	console.log({'time':new Date().toString(), "status":'similarity graph for a new record with id '+ inputId + ' computed', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'similarity graph for a new record with id '+ inputId + ' computed', 'ms': new Date().getTime()});
	res.send(graph);
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
    
    // 1 - compute main graph based on full_text feature
    qminer.mainGraphAsync(qminer.ftr, qminer.prj.allRecords, "text");
    
	res.send({"status":'started computing main graph'});
});

app.get('/main_graph_all_async', function (req, res) {
    console.log({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'computing main similarity graph', 'ms': new Date().getTime()});
    
    // 1 - compute main graph based on full_text feature
    qminer.mainGraphAsync(qminer.ftrAll, qminer.prj.allRecords, "all");
	
    res.send({"status":'started computing main graph based on all features'});
});

app.get('/custom_graph/:n', function (req, res) {
	console.log({'time':new Date().toString(), "status":'computing similarity graph for id '+req.params.n, 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'computing similarity graph for id '+req.params.n, 'ms': new Date().getTime()});
	
    // 1 - read input
	var inputId = req.params.n;
	
    // 2 - check if the graph is already computed
    var cashed = qdata.graphExists('custom_graph_', inputId);
    {
        var graph = null;
        // 2.1 - if its cashed, load it from file
        if (cashed) {
            // 2.1.1 - load it
            var graph = qdata.loadFile('custom_graph_', inputId);
        }
        // 2.2 - if its not, find the record and computed
        else {
            var rec = qdata.findPrj(inputId);
            // 2.2.1 - find the record
            if (rec) {
                // 2.2.1.1 - compute it
                var qminer = require('./lib/qminer.js');
                var graph = qminer.customGraph(qminer.ftr, qminer.prj.allRecords, rec, "text");
            }
        }               
    }
    
    // 3 - return result
    if (graph) {
        qdata.saveFile('custom_graph_', inputId, JSON.stringify(graph));
        console.log({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
        status.push({'time':new Date().toString(), "status":'similarity graph for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
        res.send(graph);
    }
    else {
        res.status(404).send('Not found');
    }
            
});

app.get('/custom_graph_all/:n', function (req, res) {
	console.log({'time':new Date().toString(), "status":'computing similarity graph for id '+req.params.n, 'ms': new Date().getTime()});
	status.push({'time':new Date().toString(), "status":'computing similarity graph for id '+req.params.n, 'ms': new Date().getTime()});
	
    // 1 - read input
	var inputId = req.params.n;
	
    // 2 - check if the graph is already computed
    var cashed = qdata.graphExists('custom_graph_all_', inputId);
    {
        var graph = null;
        // 2.1 - if its cashed, load it from file
        if (cashed) {
            // 2.1.1 - load it
            var graph = qdata.loadFile('custom_graph_all_', inputId);
        }
        // 2.2 - if its not, find the record and computed
        else {
            var rec = qdata.findPrj(inputId);
            // 2.2.1 - find the record
            if (rec) {
                // 2.2.1.1 - compute it
                var graph = qminer.customGraph(qminer.ftrAll, qminer.prj.allRecords, rec, "all");
            }
        }               
    }
    
    // 3 - return result
    if (graph) {
        qdata.saveFile('custom_graph_all_', inputId, inputId, JSON.stringify(graph));
        console.log({'time':new Date().toString(), "status":'similarity graph (all feature) for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
        status.push({'time':new Date().toString(), "status":'similarity graph (all feature) for id '+req.params.n + ' computed', 'ms': new Date().getTime()});
        res.send(graph);
    }
    else {
        res.status(404).send('Not found');
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