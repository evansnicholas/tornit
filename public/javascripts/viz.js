var Viz = {
  
  displayDtsGraph: function( json, graphContainer ) {
    
    function extractName(uri) {
      var array = uri.split("/");
      var length = array.length;
      return array[length - 1];
    }

    var tree = d3.layout.tree();
     
    var diagonal = d3.svg.diagonal()
         .projection(function(d) { return [d.y, d.x]; });

    var nodes = tree.nodes(json),
	links = tree.links(nodes);
	
    var maxDepth = d3.max(nodes, function(node){ return node.depth });
    var nodesPerDepth = _.countBy(nodes, function(node){ return node.depth });
    var maxNodes = _.max(nodesPerDepth);
    var height = maxNodes*30;
    var width = (maxDepth+1)*250;
    //Assume all node labels are about the same length
    var labelLength = extractName(nodes[0].uri).length*6;    

    tree.size([height, width - labelLength*2.5]);
    nodes = tree.nodes(json);
    links = tree.links(nodes);
       
    var svg = d3.select( graphContainer ).append("svg")
         .attr("width", width)
         .attr("height", height)
         .append("g")
         .attr("transform", "translate("+labelLength+",0)");

    var link = svg.selectAll("path.link")
	    .data(links)
	    .enter().append("path")
	    .attr("class", "link")
	    .attr("d", diagonal);
	
    var node = svg.selectAll("g.node")
	    .data(nodes)
	    .enter().append("g")
	    .attr("class", "node")
	    .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
	
    node.append("circle")
	    .attr("r", 4.5);
	
    node.append("text")
	    .attr("dx", function(d) { return d.children ? -8 : 8; })
	    .attr("dy", function(d) { return (d.depth % 2 === 0) ? -5 : 5; })
	    .attr("text-anchor", function(d) { return d.children ? "end" : "start"; })
	    .text(function(d) { return extractName(d.uri); })
            .on('click', function(d){ console.log(d); });
  
  }
  
};
