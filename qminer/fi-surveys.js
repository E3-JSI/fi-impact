exports.schema = {
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
	],
	
	nameAliases: {
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
}