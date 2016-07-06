// require main qminer object
var qm = require('qminer');
// load schemas
var schemas = require('../../schema/schema.json');
// load data
var data = require('./data.js');

var base = new qm.Base({
    mode: 'createClean',
    schema: [{
            name: 'prj',
            fields: schemas.prj
        }]
    });

// EXPORT PROPERTIES
//

exports = module.exports = {};
exports.base = base;
exports.data = data;
exports.graph;
exports.prj = base.store("prj");
exports.ftr = new qm.FeatureSpace(base, {type: "text", source: "prj", field: "desc", normalize: true, weight: "tfidf", tokenizer: { type: "simple", stopwords: "en"}});

// EXPORT METHODS
//

exports.fillPrjStore = function(data) {
    var prj = this.prj;
    // push data to store
	
    for (var i=0; i<data.surveys.length; i++) {
	    var extra_data = {};
	    for (var j=0; j<data.settings.length; j++) {
		    if (data.settings[j].usage == "display" || data.settings[j].usage == "selection" || true) {
	            extra_data[data.settings[j].field] = data.surveys[i][data.settings[j].field];
			}
	    }
		exports.data.extra_data.push(extra_data);
        prj.push({"id_internal": data.surveys[i].id_internal, "desc": data.surveys[i].full_text+" ", "node_type":data.surveys[i].node_type });
    }
    this.ftr.updateRecords(prj.allRecords);
}

exports.processRecord = function(data) {
    console.log(data);
}

exports.fillPrjStoreFromFile = function() {
    var prj = this.prj;
    // push data to store
    for (var i=0; i<data.projects.length; i++) {
	    var succ = "none";
	    /*
	    if (data.projects[i]["SUCCESS IDG"] == "X") {
	        succ = "idg";
	    }
	    if (data.projects[i]["SUCCESS VIP"] == "X") {
            succ = "vip";
        }
	    if (data.projects[i]["SUCCESS HPI"] == "X") {
            succ = "hpi";
	    }
	    data.projects[i].succ = succ;
		*/
		succ = data.projects[i].node_type;
        prj.push({"id_internal": data.projects[i].id_internal, "desc": data.projects[i].full_text+" ", "succ": succ});
    }
    this.ftr.updateRecords(prj.allRecords);
}

exports.sendRecord = function(dat) {
    var rec = {"id_internal": dat.id_internal, "desc": dat.full_text, "succ": dat.node_type};
	var mat = exports.ftr.extractMatrix(exports.prj.allRecords).transpose().toArray();
	var vec = exports.ftr.extractVector(rec).toArray();
    var analytics = require('./analytics.js'); 
    return analytics.buildEgoGraphNewRec(rec, dat, mat, vec, this.data.projects, analytics.graph);
}

exports.close = function() {
    base.close();
}

exports.open = function() {
    base = new qm.Base({
    mode: 'open',
    schema: [{
            name: 'prj',
            fields: schemas.prj
        }]
    });
}

exports.init = function() {
    base = new qm.Base({
    mode: 'createClean',
    schema: [{
            name: 'prj',
            fields: schemas.prj
        }]
    });
}

exports.customGraph = function(rec) {
    var mat = exports.ftr.extractMatrix(exports.prj.allRecords).transpose().toArray();
    var vec = exports.ftr.extractVector(rec).toArray();
    var analytics = require('./analytics.js'); 
    return analytics.buildEgoGraph(rec, mat, vec, this.data.projects, analytics.graph);
}

exports.mainGraph = function() {
    // get matrix
    var mat = exports.ftr.extractMatrix(exports.prj.allRecords);
    // import analytics module
    var analytics = require('./analytics.js');
    // construct a MDS instance
    var mat2d = analytics.mds(mat);
    // delaunay triangulation
    var triangles = analytics.triangulate(mat2d);
    // generate graph
    var graph = analytics.buildGraph(mat2d, triangles, this.data.projects);
    this.graph = graph;
    return graph;
}

exports.mainGraph2 = function() {
	var analytics = require('./analytics.js');
    var mat = exports.ftr.extractMatrix(exports.prj.allRecords);
	var nodes = [];
	var prj = this.data.projects;
    console.log(prj.length+" rows "+ mat.rows+" cols "+mat.cols);
    for( var i=0; i<mat.cols; i++) {
        //console.log(prj[i].id_internal);
      	nodes.push({ "x": Math.random(), "y": Math.random(), "id": i, "idx": prj[i].id_internal, "deg": 0, "node_type": prj[i].node_type, "extra_data": exports.data.extra_data[i]});
    }
	var edges = [];
    for (var i=0; i<mat.cols; i++) {
	    for (var j=i; j<mat.cols; j++) {
		    var sim = analytics.cosine(mat.getCol(i).toArray(), mat.getCol(j).toArray());
			if (sim > 0.04) {
                console.log(i+" - "+j+": "+sim);
			    edges.push({"source": nodes[i].id, "source": nodes[j].id});
			}
		}
    }
	this.graph = {"nodes": nodes, "edges": edges};
}

exports.mainGraphAsync = function() {
    // get matrix
    var mat = exports.ftr.extractMatrix(exports.prj.allRecords);
    // import analytics module
    var analytics = require('./analytics.js');
    // construct a MDS instance
	this.graph = analytics.mdsAsync(mat);
}
