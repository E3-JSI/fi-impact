// import modules
var qm = require('qminer');
var la = qm.la;
var analytics = qm.analytics;
var fs = qm.fs;

var TabSeparated = require('./TabSeparated');

var surveys = require('./fi-surveys')
var surveySchema = new TabSeparated('./export_2015_12_02_test.txt', surveys.schema) // modifies surveys.schema
var projects = require('./fi-projects')
var projectsSchema = new TabSeparated('./15_12_2015 Unique database v2 - export.txt', projects.schema) // modifies projects.schema

var base = new qm.Base({
    mode: 'createClean',
    schema: [
		{
			name: 'Surveys',
			fields: surveys.schema.fields
		},
		{
			name: 'Projects',
			fields: projects.schema.fields
		}
	]
})
surveyBase = base.store("Surveys")
surveySchema.addValuesToStore(surveyBase)
projectsBase = base.store("Projects")
projectsSchema.addValuesToStore(projectsBase)

function buildFeatureSpace(name, schema) {
	space = []
	schemaName = schema.schema.name
	columns = schema.schema.columns
	nameAliases = schema.schema.nameAliases
	schema.schema.dataFields.forEach(function(columns) {
		if (columns.format && columns.feature.length > 0 && columns.feature.indexOf(name) > -1) { // 
			var colArray = columns.colString.split("\t")
			colArray.forEach(function(column) {
				colType = "categorical"
				switch(columns.format.type) {
					case "int": colType = "numeric"; break;
					case "float": colType = "numeric"; break;
					case "string_v": colType = "multinomial"; break;
				}
				if (["int_v", "float_v", "int", "float", "string_v", "bool"].indexOf(columns.format.type) == -1) space.push({ type: colType, source: schemaName, field: nameAliases.get(column) })
			})
		}
	})
	return space
}

// build feature space
var surveyRecords = surveyBase.allRecords;
var ftr = new qm.FeatureSpace(base, buildFeatureSpace("scores", surveySchema));
ftr.updateRecords(surveyRecords);
var ftrMatrix = ftr.extractSparseMatrix(surveyRecords);


// Train regression for INNOVATION

var scoreVectorInnovation = surveyRecords.getVector("INNOVATION");
var scoreModel = new analytics.SVR();

scoreModel.fit(ftrMatrix, scoreVectorInnovation);

var test = surveyBase.last
var testScoreVector = ftr.extractSparseVector(test);

console.log("Dimensionality of feature space: " + ftr.dim);
console.log("Predicted score: " + scoreModel.predict(testScoreVector).toFixed(1));
console.log("True score:      " + test.INNOVATION);

// Train regression for FEASIBILITY

var scoreVectorInnovation = surveyRecords.getVector("FEASIBILITY");
var scoreModel = new analytics.SVR();

scoreModel.fit(ftrMatrix, scoreVectorInnovation);

var test = surveyBase.last
var testScoreVector = ftr.extractSparseVector(test);

console.log("Dimensionality of feature space: " + ftr.dim);
console.log("Predicted score: " + scoreModel.predict(testScoreVector).toFixed(1));
console.log("True score:      " + test.FEASIBILITY);

// Train regression for MARKET

var scoreVectorInnovation = surveyRecords.getVector("MARKET");
var scoreModel = new analytics.SVR();

scoreModel.fit(ftrMatrix, scoreVectorInnovation);

var test = surveyBase.last
var testScoreVector = ftr.extractSparseVector(test);

console.log("Dimensionality of feature space: " + ftr.dim);
console.log("Predicted score: " + scoreModel.predict(testScoreVector).toFixed(1));
console.log("True score:      " + test.MARKET);

base.close();
// baseP.close();