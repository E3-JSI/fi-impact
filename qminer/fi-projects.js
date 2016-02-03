exports.schema = {
	name: "Projects",
	fields: [
		{ name: 'ID', type: 'string', shortstring: true },
	],
	multicolumns: [],
	features: { scores: [] },
	labels: { scores: [] },
	dataFields: [
		{ // Q0_1	Q0_2	
			colString: "ACCELERATOR	COUNTRY	SELECTED	Respondent	Q2_1	Q2_2	Q2_3	Q2_4	Q2_5	Q3_7	Q3_8	Q3_9	Q3_10	Q3_11	Q4_1	Q4_2	Q4_4	Q4_5	smthng	ContextAwareness",
			format: { type: 'string', codebook: true, shortstring: true },
			multicolumn: false,
			feature: [],
			label: []
		},
		{
			colString: "ORGANIZATION	PROJECT	NAME	FUND	CALL	distance_origin",
			format: { type: 'string', shortstring: true },
			multicolumn: false,
			feature: [],
			label: []
		},
		{
			colString: "ADDRESS	VERTICAL	DESCRIPTION	consumer_sectors",
			format: { type: 'string' },
			multicolumn: false,
			feature: [],
			label: []
		},
		{
			colString: "MEMBERS	Experience	Q1_8	Q1_9	Q1_13	licenses	subscriptions	project_fees	Q3_6	Q4_6	Year_1	Year_2	Year_3	Year_4	smthng1	Q3_3	n57	n58	n59	n60	n61	n62	n63	n64	n65	n66	n67	n69	n70	n71	n72	n73	n74	n75	n1	n2	n3	n4	n5	n6	n7	n8	n9	n10	n77	n78	n79	n80	n81	n82	n83	n84	n85	n86	n87	n88	n89	n90	n91	n92	n93	n94	n95	TechScore	BusinessScore",
			format: { type: 'int' },
			multicolumn: false,
			feature: [],
			label: []
		},
		{
			colString: "Q4_6p	smthng2	TechnicalScore_normalized	AddedScores	FEASIBILITY	INNOVATION	MARKET	MARKET_NEEDS_BUSINESS	AverageTotKPIs",
			format: { type: 'float' },
			multicolumn: false,
			feature: [],
			label: []
		},
		{
			colString: "Q1_12	Q1_18_Manufacturing	Q1_18_Media	Q1_18_eHealth	Q1_18_Energy	Q3_1	Q3_4	Q3_5	countries	sectors",
			format: { type: 'string_v' },
			multicolumn: false,
			feature: [],
			label: []
		}
	],
	
	nameAliases: {
		dictionary: [
			
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