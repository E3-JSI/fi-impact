var exports = module.exports = {};
var fs = require('fs');
var config = JSON.parse(fs.readFileSync('./config/main_config.json', 'utf8'));
var data = require('../../data/'+config.prj_data_fn);
exports.projects = data.surveys;
exports.settings = data.settings;
exports.data_settings;
exports.extra_data = [];

exports.savePrjData = function(data) {
	var fs = require('fs');
    fs.writeFile("../data/post_data.json", JSON.stringify(data), function(err) {
        if(err) {
            return console.log(err);
        }
        console.log("The prj data was saved in a file!");
	});
}

exports.getAllFilesFromFolder = function(dir) {

    var filesystem = require("fs");
    var results = [];

    filesystem.readdirSync(dir).forEach(function(file) {

        file = dir+'/'+file;
        var stat = filesystem.statSync(file);

        if (stat && stat.isDirectory()) {
            results = results.concat(_getAllFilesFromFolder(file))
        } else results.push(file);

    });
    return results;
};

exports.graphExists = function(prefix, name) {
    var files = this.getAllFilesFromFolder("../output/");
	var cashed = false;
	for (var i=0; i<files.length; i++) {
	    var arr = files[i].split(prefix);
		if (arr.length > 0) {
		    if (arr[1] == name+".json") {
			    cashed = true;
			}
		}
	}
    return cashed
}

exports.findPrj = function(id) {
    var rec = null;
    var qminer = require('./qminer.js');
    var recset = qminer.prj.allRecords;
    for (var i=0; i<recset.length; i++) {
        if (recset[i].id_internal == id) {
            rec = recset[i];
        }
    }
    return rec;
}

exports.loadFile = function(prefix, name) {
    return JSON.parse(fs.readFileSync('../output/'+prefix+name, 'utf8'));
}

exports.saveFile = function(prefix, name, graph) {
    fs.writeFile("../output/"+prefix+name+".json", JSON.stringify(graph), function(err) {
		if (err) {
			return console.log(err);
		}
		console.log("The file was saved!");
	});
}