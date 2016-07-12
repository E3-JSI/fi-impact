// require main qminer object
var qm = require('qminer');
// load schemas
var schemas = require('../../schema/schema.json');
// load data
var qdata = require('./data.js');

// EXPORT PROPERTIES
//
exports = module.exports = {};
exports.data = qdata;
exports.base;
exports.prj;
exports.graph;
exports.graph_allftr;
exports.ftr;
exports.ftrAll;

/*
 * Create base 
 */
exports.createBase = function(schema) {
    exports.base = new qm.Base({
        mode: 'createClean',
        schema: [{
                name: 'prj',
                fields: schema
            }]
    });
    exports.prj = exports.base.store("prj");    
}
 
/*
 * Set project shema
 */
 exports.setPrjShema = function(data) {
    var prj = [];	
    for (var j=0; j<data.settings.length; j++) {
        var seed_object = {};
        var type = "string";
        if (data.settings[j].type == "text") {
            type = "string";
        }
        else if (data.settings[j].type == "int" || data.settings[j].type == "numeric" || data.settings[j].type == "float") {
            type = "int";
        } 
        else if (data.settings[j].type == "numeric" || data.settings[j].type == "float" || data.settings[j].type == "num") {
            type = "float";
        }
        else {
            type = "string";
        }
        
        if (data.settings[j].usage == "id") {
            seed_object = {"name": data.settings[j].field, "type": type, "primary": true};
            prj.push(seed_object);
        }     
        else if (data.settings[j].usage == "feature" || data.settings[j].usage == "node_type") {
            seed_object = {"name": data.settings[j].field, "type": type, "primary": false};
            prj.push(seed_object);
        }
        else {}       
    }
    return prj;
}

/*
 * Set feature spaces
 */
exports.setTextFts = function() {
    exports.ftr = new qm.FeatureSpace(exports.base, {type: "text", source: "prj", field: "full_text", normalize: true, weight: "tfidf", tokenizer: { type: "simple", stopwords: "en"}});
}
exports.setAllFts = function() {
    exports.ftrAll = new qm.FeatureSpace(exports.base,{type: "text", source: "prj", field: "full_text", normalize: true, weight: "tfidf", tokenizer: { type: "simple", stopwords: "en"}});
    for (var i=0; i<qdata.data_settings.length; i++) {
        if (qdata.data_settings[i].usage == "feature") {
            if (qdata.data_settings[i].type == "numeric" || qdata.data_settings[i].type == "int" || qdata.data_settings[i].type == "num") {
                exports.ftrAll.addFeatureExtractor({type: "numeric", source: "prj", field: qdata.data_settings[i].field});
            }
            if (qdata.data_settings[i].type == "text" && qdata.data_settings[i].field == "full_text") {
                exports.ftrAll.addFeatureExtractor({type: "text", source: "prj", field: qdata.data_settings[i].field, normalize: true, weight: "tfidf", tokenizer: { type: "simple", stopwords: "en"}});
            }
        }
    }
}

// EXPORT METHODS
//
exports.createRecord = function (data) {
    var seed_object = {};
    for (var j=0; j<qdata.data_settings.length; j++) {
        if (qdata.data_settings[j].usage == "feature" || qdata.data_settings[j].usage == "id" || qdata.data_settings[j].usage == "node_type") {
            if (qdata.data_settings[j].type == "int") {
                seed_object[qdata.data_settings[j].field] = 0;
            }
            else if (qdata.data_settings[j].type == "numeric" || qdata.data_settings[j].type == "num") {
                seed_object[qdata.data_settings[j].field] = 0.0;
            }
            else {
                seed_object[qdata.data_settings[j].field] = "";
            }
        }
    }
    
    var extra_data = {};
    for (var j=0; j<qdata.data_settings.length; j++) {
        if (data.hasOwnProperty(qdata.data_settings[j].field)) {
            if (qdata.data_settings[j].usage == "feature" || qdata.data_settings[j].usage == "id" || qdata.data_settings[j].usage == "node_type") {
                if (qdata.data_settings[j].type == "int") {
                    seed_object[qdata.data_settings[j].field] = parseInt(data[qdata.data_settings[j].field]);
                }
                else if (qdata.data_settings[j].type == "numeric" || qdata.data_settings[j].type == "num") {
                    seed_object[qdata.data_settings[j].field] = parseFloat(data[qdata.data_settings[j].field]);
                }
                else {
                    if (seed_object[qdata.data_settings[j].field] !== undefined) {
                        seed_object[qdata.data_settings[j].field] = data[qdata.data_settings[j].field];
                    }
                }
            }
            if (qdata.data_settings[j].usage == "display" || qdata.data_settings[j].usage == "selection" || true) {
                extra_data[qdata.data_settings[j].field] = data[qdata.data_settings[j].field];
            }
        }
    }
    seed_object.extra_data = extra_data;
    return seed_object;
}

exports.fillPrjStore = function(data) {
    var prj = this.prj;	
    for (var i=0; i<data.surveys.length; i++) {
	    var extra_data = {};
		var seed_object = {};
        
        for (var j=0; j<data.settings.length; j++) {
		    if (data.settings[j].usage == "feature" || data.settings[j].usage == "id" || data.settings[j].usage == "node_type") {
                if (data.settings[j].type == "int") {
                    seed_object[data.settings[j].field] = 0;
                }
                else if (data.settings[j].type == "numeric" || data.settings[j].type == "num") {
                    seed_object[data.settings[j].field] = 0.0;
                }
                else {
                    seed_object[data.settings[j].field] = "";
                }
            }
        }
        
	    for (var j=0; j<data.settings.length; j++) {
            if (data.surveys[i].hasOwnProperty(data.settings[j].field)) {
                if (data.settings[j].usage == "feature" || data.settings[j].usage == "id" || data.settings[j].usage == "node_type") {
                    if (data.settings[j].type == "int") {
                        seed_object[data.settings[j].field] = parseInt(data.surveys[i][data.settings[j].field]);
                    }
                    else if (data.settings[j].type == "numeric" || data.settings[j].type == "num") {
                        seed_object[data.settings[j].field] = parseFloat(data.surveys[i][data.settings[j].field]);
                    }
                    else {
                        if (seed_object[data.settings[j].field] !== undefined) {
                            seed_object[data.settings[j].field] = data.surveys[i][data.settings[j].field];
                        }
                    }
                }
                if (data.settings[j].usage == "display" || data.settings[j].usage == "selection" || true) {
                    extra_data[data.settings[j].field] = data.surveys[i][data.settings[j].field];
                }
            }
	    }
        
        prj.push(seed_object);
		exports.data.extra_data.push(extra_data);
        //prj.push({"id_internal": data.surveys[i].id_internal, "desc": data.surveys[i].full_text+" ", "node_type":data.surveys[i].node_type });
    }
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

exports.sendRecord = function(ftr, recs, dat) {
    var analytics = require('./analytics.js'); 
    //var rec = {"id_internal": dat.id_internal, "desc": dat.full_text, "succ": dat.node_type};
	var rec = exports.createRecord(dat);
    var mat = ftr.extractMatrix(exports.prj.allRecords).transpose().toArray();
	var vec = ftr.extractVector(rec).toArray();
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

exports.customGraph = function(ftr, recs, rec, type) {
    var analytics = require('./analytics.js');
    var mat = ftr.extractMatrix(recs).transpose().toArray();
    var vec = ftr.extractVector(rec).toArray();
    // 1 - refresh graph from analytics
    exports.graph = analytics.graph;
    exports.graph_allftr = analytics.graph_allftr;
    // 2 - compute
    if (type == "text") {
        return analytics.buildEgoGraph(rec, mat, vec, this.data.projects, exports.graph);
    }
    else if (type == "all") {
        return analytics.buildEgoGraph(rec, mat, vec, this.data.projects, analytics.graph_allftr);
    }
    else {
        return analytics.buildEgoGraph(rec, mat, vec, this.data.projects, analytics.graph);
    }
    
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

exports.mainGraphAsync = function(ftr, recs, type) {
    // import analytics module
    var analytics = require('./analytics.js');  
    // get matrix
    var mat = ftr.extractMatrix(recs);
    // construct a MDS instance
	analytics.mdsAsync(mat, type); 
}
