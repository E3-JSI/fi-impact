function drawSimilarityGraph(data) {
        var nodes = data.nodes;
	var edges = data.edges;
	var isRunning = false;
	var minNodeSizeVar = 2;
	if (data.nodes.length < 10 || data.nodes.length < 20)
		minNodeSizeVar = 8;
	$('#similarityGraph').empty();
	var sigRoot = document.getElementById('similarityGraph');
	var sigInst = sigma.init(sigRoot).drawingProperties({
		defaultLabelColor: "black",
		font: 'Arial',
		edgeColor: 'source',
		defaultEdgeType: 'curve'
	}).graphProperties({
		minNodeSize: minNodeSizeVar,
		maxNodeSize: 6	});

	// Bind events :
	var greyColor = '#666';

	var popUp;
	
	sigInst.bind('upnodes', function (event) {
		var node;
		sigInst.iterNodes(function (n) { node = n; }, [event.content[0]]);
		app_click(node['id']);
    	});
	
	for (var i = 0; i < nodes.length; i++) {
		var color = "gray";
		if (nodes[i].succ != "none") {
			color = "green";
		}
		if (nodes[i].hasOwnProperty("succ")) {
			if (nodes[i].succ!="normal")
			color = "blue";
		}
		if (nodes[i].hasOwnProperty("ego")) {
			color = "red";
		}
		var size = nodes[i].deg;
		sigInst.addNode(nodes[i].id, { label: nodes[i].extra_data.Q1_3, 'x': nodes[i].x, 'y': nodes[i].y, color: color, size: size })
	}

	var uedges = [];
	for (var i = 0; i < edges.length; i++) {
		if (uedges.indexOf(edges[i].n1 + "_" + edges[i].n2) == -1) {
			sigInst.addEdge(edges[i].n1 + "_" + edges[i].n2, edges[i].n1, edges[i].n2);
			uedges.push(edges[i].n1 + "_" + edges[i].n2);
		}
	}

	//sigInst.draw();

	sigInst.startForceAtlas2();
	isRunning = true;
	if (isRunning)
		setTimeout(function () {
			if (isRunning) {
				isRunning = false;
				sigInst.stopForceAtlas2();
				sigInst.refresh();
			}
		}, 6000);
	
};
