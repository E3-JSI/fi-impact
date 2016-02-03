var qm = require('qminer');
var fs = qm.fs;

function TabSeparated(filename, schema) {
	
	this.file = fs.openRead(filename)
	this.columns = buildCols(this.file.readLine())
	this.schema = schema
	generateCols()
	
	function buildCols(colString) {
		var cols = {}
		colString.split("\t").forEach(function(element, index, array) { cols[element] = index })
		return cols
	}
	
	function generateCols() {
		schema.dataFields.forEach(function(cols) {
			var colArray = cols.colString.split("\t")
			colArray.forEach(function(column) {
				colName = schema.nameAliases.get(column)
				obj = { name: colName }
				
				for (var p in cols.format) { obj[p] = cols.format[p]; }
				schema.fields.push(obj)
				
				if (cols.multicolumn) schema.multicolumns.push(colName)
				if (cols.feature) cols.feature.forEach(function(ftr) { if (ftr in schema.features) schema.features[ftr].push(colName) })
				if (cols.label) cols.label.forEach(function(lbl) { if (lbl in schema.labels) schema.labels[lbl].push(colName) })
			})
		})
	}

}

TabSeparated.prototype.addValuesToStore = function(db) {
	
	var columns = this.columns
	
	var typeTransforms = {
		intTransform: function(input) { return (input.length > 0 ? parseInt(input) : 0) },
		floatTransform: function(input) { return (input.length > 0 ? parseFloat(input) : 0) },
		boolTransform: function(input) { return input == "A" },
		get: function(input, type) {
			if (type+"Transform" in this) return this[type+"Transform"](input)
			else return input
		}
	}
	
	function colNum(column) { return ( column in columns ? columns[column] : -1 ) }
	
	function dataVectorFromColumn(str, type) {
		return str.split(",").map(function(i) { return typeTransforms.get(i, type.replace("_v", "")) })
	}
	
	function dataVectorFromCols(str, data, type) {
		r = []
		for (var property in columns) if (columns.hasOwnProperty(property) && property.indexOf(str) == 0 && property.length-str.length < 2) r.push(data[columns[property]])
		return r.map(function(i) { return typeTransforms.get(i, type.replace("_v", "")) })
	}
	
	Object.keys(columns).forEach(function(key) { console })
	
	
	while (!this.file.eof) {
		var line = this.file.readLine();
		if (line == "") { continue; }
		var cells = line.split("\t")
		try {
			var schemaObject = {}
			var multicols = this.schema.multicolumns
			var nameAliases = this.schema.nameAliases
			this.schema.fields.forEach(function(s) {
				if (multicols.indexOf(s.name) > -1)  {
					schemaObject[s.name] = dataVectorFromCols(nameAliases.get(s.name), cells, s.type)
				}
				else {
					col = colNum(nameAliases.get(s.name))
					if (s.type.indexOf("_v") > -1) schemaObject[s.name] = dataVectorFromColumn(cells[col], s.type)
					else if (col >= 0) schemaObject[s.name] = typeTransforms.get(cells[col], s.type)
				}
			})
			db.push(schemaObject)	
		}
		catch (err) {
			console.log(this.schema.name, err);
		}
	}
};

module.exports = TabSeparated;