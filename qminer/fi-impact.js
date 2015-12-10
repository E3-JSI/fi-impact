// import modules
var qm = require('qminer');
var la = qm.la;
var analytics = qm.analytics;
var fs = qm.fs;


// create a mapping from excel export column names to their consecutive numbers
function buildCols(colString) {
	var columns = {}
	colString.split("\t").forEach(function(element, index, array) { columns[element] = index })
	return columns
}

function colNum(column) { return ( column in columns ? columns[column] : -1 ) }

function generateCols(schema) {
	schema.dataFields.forEach(function(columns) {
		var colArray = columns.colString.split("\t")
		colArray.forEach(function(column) {
			colName = nameAliases.get(column)
			obj = { name: colName }
			
			for (var p in columns.format) { obj[p] = columns.format[p]; }
			schema.fields.push(obj)
			
			if (columns.multicolumn) schema.multicolumns.push(colName)
			if (columns.feature) columns.feature.forEach(function(ftr) { if (ftr in schema.features) schema.features[ftr].push(colName) })
			if (columns.label) columns.label.forEach(function(lbl) { if (lbl in schema.labels) schema.labels[lbl].push(colName) })
		})
	})
}

function buildFeatureSpace(name, schema) {
	space = []
	schema.dataFields.forEach(function(columns) {
		if (columns.format && columns.feature.length > 0 && columns.feature.indexOf(name) > -1) { // 
			var colArray = columns.colString.split("\t")
			colArray.forEach(function(column) {
				colType = "categorical"
				switch(columns.format.type) {
					case "int": colType = "numeric"; break;
					case "float": colType = "numeric"; break;
					case "string_v": colType = "multinomial"; break;
				}
				if (["int_v", "float_v", "int", "float", "string_v", "bool"].indexOf(columns.format.type) == -1) space.push({ type: colType, source: schema.name, field: nameAliases.get(column) })
			})
		}
	})
	return space
}

function dataVectorFromCols(str, data, type) {
	r = []
	for (var property in columns) if (columns.hasOwnProperty(property) && property.indexOf(str) == 0 && property.length-str.length < 2) r.push(data[columns[property]])
	return r.map(function(i) { return typeTransforms.get(i, type.replace("_v", "")) })
}

function dataVectorFromColumn(str, type) {
	return str.split(",").map(function(i) { return typeTransforms.get(i, type.replace("_v", "")) })
}

var nameAliases = {
	dictionary: [
		{ name: "Q0_1", alias: "type" },
		{ name: "Q0_2", alias: "version" },
		{ name: "Q1_1", alias: "accelerator" },
		{ name: "Q1_2", alias: "country" },
		{ name: "Q1_3", alias: "organisation" },
		{ name: "Q1_4", alias: "project" },
		{ name: "Q1_5", alias: "address" },
		{ name: "Q3_2_", alias: "revenue_division" },
		{ name: "MARKET_NEEDS_", alias: "market_needs_vector" }
	],
	get: function(str) {
		r = this.dictionary.filter(function(o) { return o.alias == str })
		s = this.dictionary.filter(function(o) { return o.name == str })
		if (r.length > 0) return r[0].name
		else if (s.length > 0) return s[0].alias
		else return str
	}
}

var typeTransforms = {
	intTransform: function(input) { return (input.length > 0 ? parseInt(input) : 0) },
	floatTransform: function(input) { return (input.length > 0 ? parseFloat(input) : 0) },
	boolTransform: function(input) { return input == "A" },
	get: function(input, type) {
		if (type+"Transform" in this) return this[type+"Transform"](input)
		else return input
	}
}

var surveySchema = {
	name: "Surveys",
	fields: [
		{ name: 'id_external', type: 'string', shortstring: true },
		{ name: 'id_internal', type: 'string', shortstring: true },
		{ name: 'address', type: 'string' }
	],
	multicolumns: [],
	features: { scores: [] },
	labels: { scores: [] },
	dataFields: [
		{
			colString: "Q3_2_	Q5A_1_	Q5B_1_",
			format: { type: 'int_v' },
			multicolumn: true,
			feature: ["scores"],
			label: []
		},
		{
			colString: "MARKET_NEEDS_",
			format: { type: 'float_v' },
			multicolumn: true,
			feature: ["scores"],
			label: []
		},
		{ // Q0_1	Q0_2	
			colString: "Q1_1	Q1_2	Q1_3	Q1_4	Q2_1	Q2_2	Q2_4	Q2_5	Q3_7	Q3_8	Q3_9	Q4_1	Q4_2	Q4_4	Q4_5",
			format: { type: 'string', codebook: true, shortstring: true },
			multicolumn: false,
			feature: ["scores"],
			label: []
		},
		{
			colString: "Q1_6a	Q1_6b	Q1_6c	Q1_20	Q2_3	Q6A_1_A	Q6A_1_B	Q6A_1_C	Q6A_1_D	Q6A_1_E	Q6A_1_F	Q6A_1_G	Q6A_1_H	Q6A_1_I	Q6A_1_J	Q6A_1_K	Q6B_1_A	Q6B_1_B	Q6B_1_C	Q6B_1_D	Q6B_1_E	Q6B_1_F",
			format: { type: 'bool' },
			multicolumn: false,
			feature: ["scores"],
			label: []
		},
		{
			colString: "Q1_7	Q1_8	Q1_9	Q1_13	Q1_16	Q3_6	Q4_3a	Q4_3b	Q4_3c	Q4_3d	Q4_6",
			format: { type: 'int' },
			multicolumn: false,
			feature: ["scores"],
			label: []
		},
		{
			colString: "FEASIBILITY	INNOVATION	MARKET	MARKET_NEEDS",
			format: { type: 'float' },
			multicolumn: false,
			feature: [],
			label: ["scores"]
		},
		{
			colString: "Q3_1	Q3_3	Q3_4	Q3_5",
			format: { type: 'string_v' },
			multicolumn: false,
			feature: ["scores"],
			label: []
		}
	]
}
generateCols(surveySchema)

// create a new base containing the store
var base = new qm.Base({
    mode: 'createClean',
    schema: [
		{
			name: 'Surveys',
			fields: surveySchema.fields
		}
	]
});
surveys = base.store("Surveys")

// add values to store
var fin = fs.openRead('./export_2015_12_02_test.txt');
var columns = buildCols(fin.readLine())

while (!fin.eof) {
    var line = fin.readLine();
    if (line == "") { continue; }
	var cells = line.split("\t")
    try {
		var schemaObject = {}
		surveySchema.fields.forEach(function(s) {
			schemaObject[s.name] = surveySchema
			if (surveySchema.multicolumns.indexOf(s.name) > -1)  {
				schemaObject[s.name] = dataVectorFromCols(nameAliases.get(s.name), cells, s.type)
			}
			else {
				col = colNum(nameAliases.get(s.name))
				if (s.type.indexOf("_v") > -1) schemaObject[s.name] = dataVectorFromColumn(cells[col], s.type)
				else if (col >= 0) schemaObject[s.name] = typeTransforms.get(cells[col], s.type)
			}
		})
		surveys.push(schemaObject)
		//console.log(schemaObject)
    }
	catch (err) {
        console.log('Surveys', err);
    }
};

// build feature space
var surveyRecords = surveys.allRecords;
var ftr = new qm.FeatureSpace(base, buildFeatureSpace("scores", surveySchema));
ftr.updateRecords(surveyRecords);
var ftrMatrix = ftr.extractSparseMatrix(surveyRecords);


// Train regression for INNOVATION

var scoreVectorInnovation = surveyRecords.getVector("INNOVATION");
var scoreModel = new analytics.SVR();

scoreModel.fit(ftrMatrix, scoreVectorInnovation);

var test = surveys.last
var testScoreVector = ftr.extractSparseVector(test);

console.log("Dimensionality of feature space: " + ftr.dim);
console.log("Predicted score: " + scoreModel.predict(testScoreVector).toFixed(1));
console.log("True score:      " + test.INNOVATION);

// Train regression for FEASIBILITY

var scoreVectorInnovation = surveyRecords.getVector("FEASIBILITY");
var scoreModel = new analytics.SVR();

scoreModel.fit(ftrMatrix, scoreVectorInnovation);

var test = surveys.last
var testScoreVector = ftr.extractSparseVector(test);

console.log("Dimensionality of feature space: " + ftr.dim);
console.log("Predicted score: " + scoreModel.predict(testScoreVector).toFixed(1));
console.log("True score:      " + test.FEASIBILITY);

// Train regression for MARKET

var scoreVectorInnovation = surveyRecords.getVector("MARKET");
var scoreModel = new analytics.SVR();

scoreModel.fit(ftrMatrix, scoreVectorInnovation);

var test = surveys.last
var testScoreVector = ftr.extractSparseVector(test);

console.log("Dimensionality of feature space: " + ftr.dim);
console.log("Predicted score: " + scoreModel.predict(testScoreVector).toFixed(1));
console.log("True score:      " + test.MARKET);

// TODO: detect which fields are correlated with INNOVATION, MARKET, FEASIBILITY and MARKET_NEEDS

base.close();