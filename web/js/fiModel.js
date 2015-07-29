var model = {
"a1": [ "1", "2", "3", "4", "5", "6a", "6b", "6c", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" ],
"a2": [ "1", "2", "3", "4", "5" ],
"a3": [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" ],
"a4": [ "1", "2", "3", "4", "5", "6" ],
"a5A": [ "1" ],
"a5B": [ "1" ],
"a6A": [ "1", "2" ],
"a6B": [ "1", "2", "3", "4", "5", "6a", "6b", "6c", "7", "8", "9", "10", "11", "12", "13", "14" ],
"Q1_6": ["6a", "6b", "6c"],
"bool": ["1_6a", "1_6b", "1_6c", "2_3", "6A_1", "6A_2"],
"list": ["1_12", "1_18a", "1_18b", "1_18c", "1_18d", "3_4"],

"marketNeedsTop5": {
	"A": ["D", "A", "B", "C", "F"],
	"B": ["G", "A", "B", "K", "D"],
	"C": ["D", "B", "A", "G", "H"],
	"D": ["H", "D", "G", "A", "K"],
	"E": ["H", "A", "D", "G", "B"],
	"G": ["G", "A", "H", "I", "B"],
	"H": ["H", "G", "A", "E", "K"],
	"I": ["I", "G", "H", "B", "A"],
	"J": ["H", "D", "G", "I", "B"],
	"K": ["H", "G", "B", "K", "A"],
	"L": ["H", "K", "A", "G", "E"],
	"M": ["G", "D", "H", "B", "K"],
	"N": ["B", "C", "A", "D", "K"],
	"O": ["H", "B", "D", "G", "E"],
	"P": ["G", "H", "A", "D", "K"],
	"Q": ["H", "C", "F", "B", "E"]
},

'graphSlotText': {
	'1': "You should try to improve this sector.",
	'2': "Room for improvement here!",
	'3': "Well done!",
	'4': "Well done!"
},

"Q1_18text": {
	"a": "Manufacturing specific enablers",
	"b": "Media specific enablers",
	"c": "eHealth specific enablers",
	"d": "Energy specific enablers"
},

"scoreKeys": {
	"MARKET_NEEDS_BUSINESS": "business",
	"FEASIBILITY": "feasibility",
	"INNOVATION": "innovation",
	"MARKET": "market",
	"Q6A_1": "socialA",
	"Q6B_1": "socialB"
},

"max": {
	"feasibility": 5,
	"innovation": 5,
	"market": 5,
	"business": 10
},

"lmh": {
	"feasibility": [.32, .67], // 1.6/5, 3.35/5
	"innovation": [.32, .67],
	"market": [.32, .67],
	"business": [.4, .75] // x/10
},

"s1": {
	"q1": {
		"A": "Ceedtech",
		"B": "Creatifi",
		"C": "European Pioneers",
		"D": "Fabulous",
		"E": "FI-Adopt",
		"F": "FI-C3",
		"G": "Fiche",
		"H": "Finish",
		"I": "Finodex",
		"J": "Fractals",
		"K": "FrontierCities",
		"L": "Impact",
		"M": "Incense",
		"N": "Smart Agri-food",
		"O": "Soul-fi",
		"P": "Speedup Europe"
	},
	"a6": {
		"a": "SME",
		"b": "Self-employed entrepreneur",
		"c": "Owned by a large organisation"
	},
	"q10": {
		"A": "Tech provider",
		"B": "Service provider"
	},
	"q11": {
		"A": "Purely software",
		"B": "Software and Hardware"
	}
},
"s2": {
	"q1": {
		"TRL1": "Basic principles observed.",
		"TRL2": "Technology concept formulated.",
		"TRL3": "Experimental proof of concept.",
		"TRL4": "Product/service validated in lab.",
		"TRL5": "Product/service validated in operational environment.",
		"TRL6": "Product/service demonstrated in operational environment.",
		"TRL7": "Product/service prototype demonstration in operational environment to client.",
		"TRL8": "Product/service market ready.",
		"TRL9": "Product/service sold in marketplace."
	},
	"q2": {
		"A": "Our business idea involves changes and improvements to existing products and services (Incremental Innovation).",
		"B": "Our business idea radically changes existing products and services and creates new markets by discovering new categories of customers (Disruptive innovation)."
	},
	"q4": {
		"A": "Single person.",
		"B": "Group effort."
	},
	"q5": {
		"A": "Standalone offering.",
		"B": "Fits into an existing commercial strategy."
	}
},
"s3": {
	"q1": {
		"A": "Production model",
		"B": "Markup model",
		"C": "Subscription model",
		"D": "Usage fees model",
		"E": "Rental model",
		"F": "License model",
		"G": "Advertising model",
		"H": "Transactions/Intermediation model",
		"I": "Freemium model",
		"J": "Customer analysis model"
	},
	"q3": {
		"A": "Accommodation and Food Service Activities",
		"B": "Agriculture, Forestry and Fishing",
		"C": "Arts, Entertainment and Recreation",
		"D": "Business Services",
		"E": "Construction",
		"F": "Consumer",
		"G": "Education",
		"H": "Financial Services",
		"I": "Government",
		"J": "Healthcare",
		"K": "Horizontal",
		"L": "Manufacturing",
		"M": "Mining and Quarrying",
		"N": "Retail and Wholesale",
		"O": "Telecom and Media",
		"P": "Transport and Logistics",
		"Q": "Utilities"
	},
	"q4": {
		"A": "Sales agents",
		"B": "Shops",
		"C": "App-stores",
		"D": "Personal website",
		"E": "Other external websites",
		"F": "Public tenders notices ",
		"G": " E-mail/Phone-call marketing"
	},
	"q5": {
		"A": "My City or Region",
		"B": "My country",
		"C": "Multiple Countries",
		"D": "Global",
		"E": "Other"
	},
	"q7": {
		"A": "No competition.",
		"B": "Medium competition.",
		"C": "High competition."
	},
	"q8": {
		"A": "Our value proposition is based on vision and internal discussion.",
		"B": "Our value proposition is validated through surveys and market studies.",
		"C": "Our value proposition is validated through interviews and meetings with customers."
	},
	"q9": {
		"A": "Preparing sales materials and channels.",
		"B": "Sales materials available and channels activated.",
		"C": "Acquired first customers through established channels."
	},
	"q10": {
		"A": "Defining a market strategy to create demand.",
		"B": "Started promoting the vision.",
		"C": "Early adopter customers acquired."
	},
	"q11": {
		"A": "Defining our competitive position on the market.",
		"B": "Company positioned and sales strategy defined.",
		"C": "Executing a sales strategy to gain market share."
	}
},
"s4": {
	"q1": {
		"A": "In the process of estimating the investment required.",
		"B": "Capital requirements estimated and contacted investors.",
		"C": "Capital requirements covered until self-sustainable."
	},
	"q2": {
		"A": "We are evaluating what the potential growth rate could be.",
		"B": "We are committed to a growth rate in the business plan.",
		"C": "We have validated growth rate with sales and market data."
	},
	"q4": {
		"A": "We have not yet analyzed the customer acquisition proces.",
		"B": "We have estimated customer acquisition cost and time.",
		"C": "We have verified customer acquisition cost and time through real sales."
	},
	"q5": {
		"A": "We have no plans for sales force hiring and increased marketing activities.",
		"B": "We have defined scale-up plans but have not yet launched them.",
		"C": "We have launched scale-up plans or set to start scale-up plans at a definite date, including hiring plan for salespeople."
	}
},
"s5A": {
	"q1": {
		"A": "Reducing operational costs",
		"B": "Improving sales performance",
		"C": "Improving marketing effectiveness",
		"D": "Enhancing customer care",
		"E": "Innovating the product or service companies sell/provide",
		"F": "Strenghtening multi-channel delivery strategy",
		"G": "Simplifying regulatory tasks and complying with regulations",
		"H": "Improving data protection",
		"I": "Increasing use and distribution of open data and transparency",
		"J": "Improving scalability of existing tools",
		"K": "Improving operational efficiency"
	}
},
"s5B": {
	"q1": {
		"A": "Answering communication/collaboration needs",
		"B": "Providing better entertainment",
		"C": "Improving quality of life",
		"D": "Simplifying daily tasks",
		"E": "Reducing/Saving time",
		"F": "Having easier and faster access to information/services",
		"G": "Saving money"
	}
},
"s6A": {
	"q1": {
		"A": "Perceived security of communities, neighbourhoods and housing",
		"B": "Protection of privacy and security of personal digital data",
		"C": "Citizens involvement and participation in open government",
		"D": "E-inclusion",
		"E": "Fitness and well-being",
		"F": "Health",
		"G": "Quality of life in urban areas",
		"H": "Quality of life as a result of better access to information and data",
		"I": "Social inclusion",
		"J": "Access and use of e-learning and innovative learning methodologies",
		"K": "Demand and use of sustainable transport solutions"
	}
},
"s6B": {
	"q1": {
		"A": "Disabled",
		"B": "Elderly",
		"C": "Ethnic or cultural minorities",
		"D": "Low income",
		"E": "Socially excluded groups",
		"F": "Unemployed"
	}
}

}