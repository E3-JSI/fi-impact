var exports = module.exports = {};
var fs = require('fs');
var config = JSON.parse(fs.readFileSync('./config/main_config.json', 'utf8'));
var data = require('../../data/'+config.prj_data_fn);
exports.projects = data.surveys;
exports.settings = data.settings;
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