{
	"sections" : [{
			"id" : "0",
			"questions" : [{
					"id" : "1",
					"default" : "I",
					"lookup" : [{
							"I" : "Impact Assessment"
						}, {
							"S" : "Self Assessment"
						}
					]
				}
			]
		}, {
			"id" : "1",
			"questions" : [{
					"id" : "1"
				}, {
					"id" : "2",
					"merge" : [{
							"id" : "17"
						}, {
							"id" : "2"
						}
					]
				}, {
					"id" : "3"
				}, {
					"id" : "4"
				}, {
					"id" : "5"
				}, {
					"id" : "6",
					"merge" : [{
							"id" : "6a",
							"lookup" : [{
									"A" : "SME"
								}, {
									"B" : ""
								}
							]
						}, {
							"id" : "6b",
							"lookup" : [{
									"A" : "Self-employed entrepreneur"
								}, {
									"B" : ""
								}
							]
						}, {
							"id" : "6c",
							"lookup" : [{
									"A" : "Owned by a large organisation"
								}, {
									"B" : ""
								}
							]
						}
					]
				}, {
					"id" : "7"
				}, {
					"id" : "8"
				}, {
					"id" : "9"
				}, {
					"id" : "10",
					"lookup" : [{
							"A" : "Tech provider"
						}, {
							"B" : "Service provider"
						}
					]
				}, {
					"id" : "11",
					"lookup" : [{
							"A" : "Purely software"
						}, {
							"B" : "Software and Hardware"
						}
					]
				}, {
					"id" : "12",
					"links" : true,
					"lookup" : [{
							"id" : "Big Data Analysis",
							"label" : "http://catalogue.fi-ware.eu/enablers/bigdata-analysis-cosmos"
						}, {
							"id" : "Complex Event Processing (CEP)",
							"label" : "http://catalogue.fiware.org/enablers/complex-event-processing-cep-proactive-technology-online"
						}, {
							"id" : "Publish/Subscribe Context Broker",
							"label" : "http://catalogue.fi-ware.eu/enablers/publishsubscribe-context-broker-orion-context-broker"
						}, {
							"id" : "Stream-oriented",
							"label" : "http://catalogue.fiware.org/enablers/stream-oriented-kurento"
						}, {
							"id" : "Backend Device Management",
							"label" : "http://catalogue.fiware.org/enablers/backend-device-management-idas"
						}, {
							"id" : "Configuration Manager-IoT Discovery",
							"label" : "http://catalogue.fiware.org/enablers/iot-discovery"
						}, {
							"id" : "Configuration Manager-Orion Context Broker",
							"label" : ""
						}, {
							"id" : "Gateway Data Handling GE",
							"label" : "http://catalogue.fiware.org/enablers/gateway-data-handling-ge-espr4fastdata"
						}, {
							"id" : "IoT Broker",
							"label" : "http://catalogue.fiware.org/enablers/iot-broker"
						}, {
							"id" : "Protocol Adapter",
							"label" : "http://catalogue.fiware.org/enablers/protocol-adapter-mr-coap"
						}, {
							"id" : "2D/3D Capture",
							"label" : "http://catalogue.fiware.org/enablers/2d3d-capture"
						}, {
							"id" : "2D-UI",
							"label" : "http://catalogue.fiware.org/enablers/2d-ui"
						}, {
							"id" : "3D-UI-WebTundra",
							"label" : "http://catalogue.fiware.org/enablers/3dui-webtundra"
						}, {
							"id" : "3D-UI-XML3D",
							"label" : "http://catalogue.fiware.org/enablers/3d-ui-xml3d"
						}, {
							"id" : "Augmented Reality",
							"label" : "http://catalogue.fiware.org/enablers/augmented-reality"
						}, {
							"id" : "Cloud Rendering",
							"label" : "http://catalogue.fiware.org/enablers/cloud-rendering"
						}, {
							"id" : "GIS Data Provider",
							"label" : "http://catalogue.fiware.org/enablers/gis-data-provider-geoserver3d"
						}, {
							"id" : "Interface Designer",
							"label" : "http://catalogue.fiware.org/enablers/interface-designer"
						}, {
							"id" : "POI Data Provider",
							"label" : "http://catalogue.fiware.org/enablers/poi-data-provider"
						}, {
							"id" : "Real Virtual Interaction",
							"label" : "http://catalogue.fiware.org/enablers/real-virtual-interaction"
						}, {
							"id" : "Synchronization",
							"label" : "http://catalogue.fiware.org/enablers/synchronization"
						}, {
							"id" : "Virtual Characters",
							"label" : "http://catalogue.fiware.org/enablers/virtual-characters"
						}, {
							"id" : "Authorization PDP",
							"label" : "http://catalogue.fiware.org/enablers/authorization-pdp-authzforce"
						}, {
							"id" : "Identity Management",
							"label" : "http://catalogue.fiware.org/enablers/identity-management-keyrock"
						}, {
							"id" : "PEP Proxy",
							"label" : "http://catalogue.fiware.org/enablers/pep-proxy-wilma"
						}, {
							"id" : "Security Monitoring",
							"label" : "http://catalogue.fiware.org/enablers/security-monitoring"
						}, {
							"id" : "Network Information and Control",
							"label" : "http://catalogue.fiware.org/enablers/network-information-and-control-ofnic"
						}, {
							"id" : "Repository",
							"label" : "http://catalogue.fiware.org/enablers/repository-repository-ri"
						}, {
							"id" : "Marketplace",
							"label" : "http://catalogue.fiware.org/enablers/marketplace-wmarket"
						}, {
							"id" : "Store",
							"label" : "http://catalogue.fi-ware.eu/enablers/store-wstore"
						}, {
							"id" : "Revenue Settlement and Sharing System",
							"label" : "http://catalogue.fiware.org/enablers/revenue-settlement-and-sharing-system-rss-ri"
						}, {
							"id" : "Application Mashup",
							"label" : "http://catalogue.fi-ware.eu/enablers/application-mashup-wirecloud"
						}, {
							"id" : "IaaS Resource Management GE",
							"label" : "http://catalogue.fiware.org/enablers/iaas-resource-management-ge-fiware-implementation"
						}, {
							"id" : "Monitoring GE",
							"label" : "http://catalogue.fiware.org/enablers/monitoring-ge-fiware-implementation"
						}, {
							"id" : "Object Storage GE",
							"label" : "http://catalogue.fiware.org/enablers/object-storage-ge-fiware-implementation"
						}, {
							"id" : "PaaS Manager",
							"label" : "http://catalogue.fi-ware.eu/enablers/paas-manager-pegasus"
						}, {
							"id" : "Policy Manager",
							"label" : "http://catalogue.fiware.org/enablers/policy-manager-bosun"
						}, {
							"id" : "Self-Service Interfaces",
							"label" : "http://catalogue.fiware.org/enablers/self-service-interfaces-cloud-portal"
						}, {
							"id" : "Software Deployment and Configuration",
							"label" : "http://catalogue.fiware.org/enablers/software-deployment-configuration-sagitta"
						}, {
							"id" : "Content Based Security - CBS",
							"label" : ""
						}, {
							"id" : "Data Viz - SpagoBI",
							"label" : "http://catalogue.fiware.org/enablers/data-visualization-spagobi"
						}
					]
				}, {
					"id" : "13"
				}, {
					"id" : "14"
				}, {
					"id" : "15"
				}, {
					"id" : "16"
				}, {
					"id" : "18",
					"answers_list" :
					[{
							"id" : "18a",
							"label" : "Manufacturing specific enablers",
							"list" : "true"
						}, {
							"id" : "18b",
							"label" : "Media specific enablers",
							"list" : "true"
						}, {
							"id" : "18c",
							"label" : "eHealth specific enablers",
							"list" : "true"
						}, {
							"id" : "18d",
							"label" : "Energy specific enablers",
							"list" : "true"
						}
					]
				}, {
					"id" : "19",
					"lookup" : [{
							"A" : "Project under preparation"
						}, {
							"B" : "Running project"
						}
					]
				}, {
					"id" : "20",
					"lookup" : [{
							"A" : "Yes"
						}, {
							"B" : "No"
						}
					]
				}, {
					"id" : "21"
				}

			]
		}, {
			"id" : "2",
			"name" : "INNOVATION",
			"label" : "innovation",
			"label_graph" : "Innovation",
			"questions" : [{
					"id" : "1",
					"lookup" : [{
							"TRL1" : "Basic principles observed (TRL1)."
						}, {
							"TRL2" : "Technology concept formulated (TRL2)."
						}, {
							"TRL3" : "Experimental proof of concept (TRL3)."
						}, {
							"TRL4" : "Product/service validated in lab (TRL4)."
						}, {
							"TRL5" : "Product/service validated in operational environment (TRL5)."
						}, {
							"TRL6" : "Product/service demonstrated in operational environment (TRL6)."
						}, {
							"TRL7" : "Product/service prototype demonstration in operational environment to client (TRL7)."
						}, {
							"TRL8" : "Product/service market ready (TRL8)."
						}, {
							"TRL9" : "Product/service sold in marketplace (TRL9)."
						}
					]
				}, {
					"id" : "2",
					"lookup" : [{
							"A" : "Incremental Innovation. Our business idea involves changes and improvements to existing products and services."
						}, {
							"B" : "Disruptive innovation. Our business idea radically changes existing products and services and creates new markets by discovering new categories of customers."
						}
					]
				}, {
					"id" : "3",
					"lookup" : [{
							"A" : "Yes"
						}, {
							"B" : "No"
						}
					]

				}, {
					"id" : "4",
					"lookup" : [{
							"A" : "Single person."
						}, {
							"B" : "Group effort."
						}
					]

				}, {
					"id" : "5",
					"lookup" : [{
							"A" : "Standalone offering."
						}, {
							"B" : "Fits into an existing commercial strategy."
						}
					]
				}
			]
		}, {
			"id" : "3",
			"name" : "MARKET",
			"label" : "market focus",
			"label_graph" : "Market focus",
			"questions" : [{
					"id" : "1",
					"lookup" : [{
							"A" : "Production model"
						}, {
							"B" : "Markup model"
						}, {
							"C" : "Subscription model"
						}, {
							"D" : "Usage fees model"
						}, {
							"E" : "Rental model"
						}, {
							"F" : "License model"
						}, {
							"G" : "Advertising model"
						}, {
							"H" : "Transactions/Intermediation model"
						}, {
							"I" : "Freemium model"
						}, {
							"J" : "Customer analysis model"
						}
					]
				}, {
					"id" : "2",
					"answers_list" : [{
							"id" : "2a",
							"label" : "licenses"
						}, {
							"id" : "2b",
							"label" : "subscriptions"
						}, {
							"id" : "2c",
							"label" : "project fees"
						}, {
							"id" : "2d",
							"label" : "production income"
						}, {
							"id" : "2e",
							"label" : "markup income"
						}, {
							"id" : "2f",
							"label" : "usage fees"
						}, {
							"id" : "2g",
							"label" : "rental income"
						}, {
							"id" : "2h",
							"label" : "advertising"
						}, {
							"id" : "2i",
							"label" : "transactions income"
						}, {
							"id" : "2j",
							"label" : "freemium income"
						}, {
							"id" : "2k",
							"label" : "customer analysis income"
						}
					],

				}, {
					"id" : "3",
					"lookup" : [{
							"A" : "Accommodation and Food Service Activities"
						}, {
							"B" : "Agriculture, Forestry and Fishing"
						}, {
							"C" : "Arts, Entertainment and Recreation"
						}, {
							"D" : "Business Services"
						}, {
							"E" : "Construction"
						}, {
							"G" : "Education"
						}, {
							"H" : "Financial Services"
						}, {
							"I" : "Government"
						}, {
							"J" : "Healthcare"
						}, {
							"K" : "Horizontal"
						}, {
							"L" : "Manufacturing"
						}, {
							"M" : "Mining and Quarrying"
						}, {
							"N" : "Retail and Wholesale"
						}, {
							"O" : "Telecom and Media"
						}, {
							"P" : "Transport and Logistics"
						}, {
							"Q" : "Utilities"
						}
					]

				}, {
					"id" : "3a",
					"lookup" : [{
							"A" : "Accommodation and Food Service Activities"
						}, {
							"B" : "Agriculture, Forestry and Fishing"
						}, {
							"C" : "Arts, Entertainment and Recreation"
						}, {
							"D" : "Business Services"
						}, {
							"E" : "Construction"
						}, {
							"G" : "Education"
						}, {
							"H" : "Financial Services"
						}, {
							"I" : "Government"
						}, {
							"J" : "Healthcare"
						}, {
							"K" : "Horizontal"
						}, {
							"L" : "Manufacturing"
						}, {
							"M" : "Mining and Quarrying"
						}, {
							"N" : "Retail and Wholesale"
						}, {
							"O" : "Telecom and Media"
						}, {
							"P" : "Transport and Logistics"
						}, {
							"Q" : "Utilities"
						}
					]

				}, {
					"id" : "3b"
				}, {
					"id" : "3c"
				}, {
					"id" : "4",
					"lookup" : [{
							"A" : "App-stores"
						}, {
							"B" : "E-mail/Phone-call marketing"
						}, {
							"C" : "Other external websites"
						}, {
							"D" : "Personal website"
						}, {
							"E" : "Public tenders notices"
						}, {
							"F" : "Sales agents"
						}, {
							"G" : " Shops"
						}
					]
				}, {
					"id" : "5",
					"custom" : "Q3_5",
					"lookup" : [{
							"A" : "My City or Region"
						}, {
							"B" : "My country"
						}, {
							"C" : "Multiple Countries"
						}, {
							"D" : "Global"
						}, {
							"E" : "Other"
						}
					]
				}, {
					"id" : "6"
				}, {
					"id" : "7",
					"lookup" : [{
							"A" : "No competition."
						}, {
							"B" : "Medium competition."
						}, {
							"C" : "High competition."
						}
					]

				}, {
					"id" : "8",
					"lookup" : [{
							"A" : "Our value proposition is based on vision and internal discussion."
						}, {
							"B" : "Our value proposition is validated through surveys and market studies."
						}, {
							"C" : "Our value proposition is validated through interviews and meetings with customers."
						}
					]

				}, {
					"id" : "9",
					"lookup" : [{
							"A" : "Preparing sales materials and channels."
						}, {
							"B" : "Sales materials available and channels activated."
						}, {
							"C" : "Acquired first customers through established channels."
						}
					]

				}, {
					"id" : "10",
					"lookup" : [{
							"A" : "Defining a market strategy to create demand."
						}, {
							"B" : "Started promoting the vision."
						}, {
							"C" : "Early adopter customers acquired."
						}
					]

				}, {
					"id" : "11",
					"lookup" : [{
							"A" : "Defining our competitive position on the market."
						}, {
							"B" : "Company positioned and sales strategy defined."
						}, {
							"C" : "Executing a sales strategy to gain market share."
						}
					]

				}, {
					"id" : "12",
					"lookup" : [{
							"A" : "Consumer: Health and wellness"
						}, {
							"B" : "Consumer: Transport and logistics"
						}, {
							"C" : "Consumer: Energy and home automation"
						}, {
							"D" : "Consumer: Leisure and gaming"
						}, {
							"E" : "Consumer: DYI and design"
						}, {
							"F" : "Consumer: Shopping"
						}, {
							"G" : "Consumer: Education and culture"
						}, {
							"H" : "Consumer: Citizen Engagement"
						}, {
							"I" : "Consumer: Environment and nature"
						}, {
							"J" : "Consumer: Other"
						}
					]
				}, {
					"id" : "13",
					"lookup" : [{
							"A" : "Consumer: Health and wellness"
						}, {
							"B" : "Consumer: Transport and logistics"
						}, {
							"C" : "Consumer: Energy and home automation"
						}, {
							"D" : "Consumer: Leisure and gaming"
						}, {
							"E" : "Consumer: DYI and design"
						}, {
							"F" : "Consumer: Shopping"
						}, {
							"G" : "Consumer: Education and culture"
						}, {
							"H" : "Consumer: Citizen Engagement"
						}, {
							"I" : "Consumer: Environment and nature"
						}, {
							"J" : "Consumer: Other"
						}
					]
				}
			]
		}, {
			"id" : "4",
			"name" : "FEASIBILITY",
			"label" : "feasibility",
			"label_graph" : "Feasibility",
			"questions" : [{
					"id" : "1",
					"lookup" : [{
							"A" : "In the process of estimating the investment required."
						}, {
							"B" : "Capital requirements estimated and contacted investors."
						}, {
							"C" : "Capital requirements covered until self-sustainable."
						}
					]
				}, {
					"id" : "2",
					"lookup" : [{
							"A" : "We are evaluating what the potential growth rate could be."
						}, {
							"B" : "We are committed to a growth rate in the business plan."
						}, {
							"C" : "We have validated growth rate with sales and market data."
						}
					]
				}, {
					"id" : "3",
					"postfix" : "%",
					"merge" : [{
							"id" : "3a"
						}, {
							"id" : "3b"

						}, {
							"id" : "3c"
						}, {
							"id" : "3d"
						}
					]

				}, {
					"id" : "4",
					"lookup" : [{
							"A" : "We have not yet analyzed the customer acquisition proces."
						}, {
							"B" : "We have estimated customer acquisition cost and time."
						}, {
							"C" : "We have verified customer acquisition cost and time through real sales."
						}
					]
				}, {
					"id" : "5",
					"lookup" : [{
							"A" : "We have no plans for sales force hiring and increased marketing activities."
						}, {
							"B" : "We have defined scale-up plans but have not yet launched them."
						}, {
							"C" : "We have launched scale-up plans or set to start scale-up plans at a definite date, including hiring plan for salespeople."
						}
					]

				}, {
					"id" : "6"
				}
			]
		}, {
			"id" : "5",
			"name" : "MARKET_NEEDS",
			"label" : "market needs",
			"label_graph" : "Market needs",
			"complex_result" : {
				"id": "1",
				"multiply" : [{
						"section" : "3",
						"question" : "3a",
						"stars" : "Q5A_1"
					}, {
						"section" : "3",
						"question" : "12",
						"prefix_result" : "F",
						"stars" : "Q5B_1"
					}, {
						"section" : "3",
						"question" : "3",
						"stars" : "Q5A_1"
					}, {
						"section" : "3",
						"question" : "13",
						"prefix_result" : "F",
						"stars" : "Q5B_1"
					}
				]
			},

			"questions" : [{
					"id" : "Q5A_1",
					"top_list" : "marketNeedsTop5",
					"multiple_fields" : [{
							"id" : "A",
							"label" : "Reducing operational costs"
						}, {
							"id" : "B",
							"label" : "Improving sales performance"
						}, {
							"id" : "C",
							"label" : "Improving marketing effectiveness"
						}, {
							"id" : "D",
							"label" : "Enhancing customer care"
						}, {
							"id" : "E",
							"label" : "Innovating the product or service companies sell/provide"
						}, {
							"id" : "F",
							"label" : "Strenghtening multi-channel delivery strategy"
						}, {
							"id" : "G",
							"label" : "Simplifying regulatory tasks and complying with regulations"
						}, {
							"id" : "H",
							"label" : "Improving data protection"
						}, {
							"id" : "I",
							"label" : "Increasing use and distribution of open data and transparency"
						}, {
							"id" : "J",
							"label" : "Improving scalability of existing tools"
						}, {
							"id" : "K",
							"label" : "Improving operational efficiency"
						}
					]
				}, {
					"id" : "Q5B_1",
					"top_list" : "marketNeedsTop5F",
					"multiple_fields" : [{
							"id" : "A",
							"label" : "Answering communication or collaboration needs"
						}, {
							"id" : "B",
							"label" : "Providing better entertainment"
						}, {
							"id" : "C",
							"label" : "Improving quality of life"
						}, {
							"id" : "D",
							"label" : "Simplifying daily tasks"
						}, {
							"id" : "E",
							"label" : "Reducing or saving time"
						}, {
							"id" : "F",
							"label" : "Having easier and faster access to information or services"
						}, {
							"id" : "G",
							"label" : "Saving money"
						}
					]
				}
			]
		}, {
			"id" : "6A",
			"questions" : [{
					"id" : "1",
					"multiple_fields" : [{
							"id" : "A",
							"label" : "Perceived security of communities, neighbourhoods and housing"
						}, {
							"id" : "B",
							"label" : "Protection of privacy and security of personal digital data"
						}, {
							"id" : "C",
							"label" : "Citizens involvement and participation in open government"
						}, {
							"id" : "D",
							"label" : "E-inclusion"
						}, {
							"id" : "E",
							"label" : "Fitness and well-being"
						}, {
							"id" : "F",
							"label" : "Health"
						}, {
							"id" : "G",
							"label" : "Quality of life in urban areas"
						}, {
							"id" : "H",
							"label" : "Quality of life as a result of better access to information and data"
						}, {
							"id" : "I",
							"label" : "Social inclusion"
						}, {
							"id" : "J",
							"label" : "Access and use of e-learning and innovative learning methodologies"
						}, {
							"id" : "K",
							"label" : "Demand and use of sustainable transport solutions"
						}
					]
				}
			]
		}, {
			"id" : "6B",
			"questions" : [{
					"id" : "1",
					"multiple_fields" : [{
							"id" : "A",
							"label" : "Disabled"
						}, {
							"id" : "B",
							"label" : "Elderly"
						}, {
							"id" : "C",
							"label" : "Ethnic or cultural minorities"
						}, {
							"id" : "D",
							"label" : "Low income"
						}, {
							"id" : "E",
							"label" : "Socially excluded groups"
						}, {
							"id" : "F",
							"label" : "Unemployed"
						}
					]
				}
			]
		}, {
			"id" : "7a",
			"name" : "MATTERMARK_GROWTH",
			"label" : "Growth Score",
			"label_graph" : "Growth Score",
			"questions" : []
		},
        {
			"id" : "7b",
			"name" : "MATTERMARK_EMPLOYEE_COUNT",
			"label" : "Employee Count",
			"label_graph" : "Employee Count",
			"questions" : []
		},
		{
			"id" : "7c",
			"name" : "MATTERMARK_MONTHLY_UNIQUES",
			"label" : "Est. Monthly Uniques",
			"label_graph" : "Est. Monthly Uniques",
			"questions" : []
		},
		{
			"id" : "7d",
			"name" : "MATTERMARK_TWITTER_FOLLOWERS",
			"label" : "Twitter Followers",
			"label_graph" : "Twitter Followers",
			"questions" : []
		},
		{
			"id" : "7e",
			"name" : "MATTERMARK_FACEBOOK_LIKES",
			"label" : "Facebook Likes",
			"label_graph" : "Facebook Likes",
			"questions" : []
		},
         {
			"id" : "7f",
			"name" : "MATTERMARK_LINKEDIN_FOLLOWS",
			"label" : "LinkedIn Follows",
			"label_graph" : "LinkedIn Follows",
			"questions" : []
		},
		{
			"id" : "7g",
			"name" : "MATTERMARK_TOTAL_FUNDING",
			"label" : "Total Funding",
			"label_graph" : "Total Funding",
			"questions" : []
		}
	],

	"tooltips" : ["Low", "Medium", "High"],
	"radarLevelsSocial" : {
		"1" : "No Impact",
		"2" : "Limited Impact",
		"3" : "Impact",
		"4" : "Significant Impact",
		"5" : "High Impact",
		"num" : 5
	},

	"marketNeedsTop5" : {
		"A" : ["D", "A", "B", "C", "F"],
		"B" : ["G", "A", "B", "K", "D"],
		"C" : ["D", "B", "A", "G", "H"],
		"D" : ["H", "D", "G", "A", "K"],
		"E" : ["H", "A", "D", "G", "B"],
		"G" : ["G", "A", "H", "I", "B"],
		"H" : ["H", "G", "A", "E", "K"],
		"I" : ["I", "G", "H", "B", "A"],
		"J" : ["H", "D", "G", "I", "B"],
		"K" : ["H", "G", "B", "K", "A"],
		"L" : ["H", "K", "A", "G", "E"],
		"M" : ["G", "D", "H", "B", "K"],
		"N" : ["B", "C", "A", "D", "K"],
		"O" : ["H", "B", "D", "G", "E"],
		"P" : ["G", "H", "A", "D", "K"],
		"Q" : ["H", "C", "F", "B", "E"]
	},

	"marketNeedsTop5F" : {
		"A" : ["C", "B", "F", "D", "A"],
		"B" : ["D", "F", "C", "G", "E"],
		"C" : ["D", "C", "G", "E", "F"],
		"D" : ["B", "C", "F", "A", "G"],
		"E" : ["B", "D", "C", "G", "E"],
		"F" : ["E", "G", "F", "D", "C"],
		"G" : ["F", "C", "G", "B", "E"],
		"H" : ["A", "F", "C", "D", "E"],
		"I" : ["C", "F", "A", "B", "D"],
		"J" : ["C", "F", "D", "B", "G"]
	},

	"ranking" : {
		"0" : "Low",
		"1" : "Low",
		"2" : "Medium",
		"3" : "High",
		"4" : "High"
	},

	"score_in_words" : "Your ranking for %s based on the data submitted is currently %s. In this section you scored better than %s%% of the %s (total) projects and proposals that have answered this survey.",

	"interpretation" : {
		"ll" : "The level of %s of your project matches that of your peers. Still, you should consider ways of further strengthening the %s element of your project.",
		"lm" : "Compared to the average of current completed surveys, you should consider ways of further strengthening the %s element of your project.",
		"lh" : "Compared to the average of current completed surveys, you should consider ways of further strengthening the %s element of your project.",
		"ml" : "Your project is outperforming your peers in terms of %s – congratulations. Still, you should consider ways of further strengthening the %s element of your project.",
		"mm" : "The level of %s of your project matches that of your peers. Still, you should consider ways of further strengthening the %s element of your project.",
		"mh" : "Compared to the average of current completed surveys, you should consider ways of further strengthening the %s element of your project.",
		"hl" : "low	Your project is outperforming your peers in terms of %s – congratulations.",
		"hm" : "Your project is outperforming your peers in terms of %s – congratulations.",
		"hh" : "The level of %s of your project matches that of your peers and is high – congratulations."
	},

	"score_in_words_mattermark" : "Your Mattermark ranking for %s is currently %s. For this indicator you scored better than %s%% of the %s (total) projects and proposals that have answered this survey.",

	"interpretation_mattermark" : {
		"ll" : "The level of %s of your project matches that of your peers. Still, you should consider ways of further strengthening the %s element of your project.",
		"lm" : "Compared to the average of current completed surveys, you should consider ways of further strengthening the %s element of your project.",
		"lh" : "Compared to the average of current completed surveys, you should consider ways of further strengthening the %s element of your project.",
		"ml" : "Your project is outperforming your peers in terms of %s – congratulations. Still, you should consider ways of further strengthening the %s element of your project.",
		"mm" : "The level of %s of your project matches that of your peers. Still, you should consider ways of further strengthening the %s element of your project.",
		"mh" : "Compared to the average of current scores of your peers, you should consider ways of further strengthening the %s element of your project.",
		"hl" : "low	Your project is outperforming your peers in terms of %s – congratulations.",
		"hm" : "Your project is outperforming your peers in terms of %s – congratulations.",
		"hh" : "The level of %s of your project matches that of your peers and is high – congratulations."
	}

}
