var Viz = {
  
  displayDtsGraph: function( json, graphContainer ) {
    
    function extractName(uri) {
      var array = uri.split("/");
      var length = array.length;
      return array[length - 1];
    }

    var width = 1000,
        height = 500;

    var tree = d3.layout.tree()
         .size([height, width - 400]);

    var diagonal = d3.svg.diagonal()
         .projection(function(d) { return [d.y, d.x]; });

    var svg = d3.select( graphContainer ).append("svg")
         .attr("width", width)
         .attr("height", height)
         .append("g")
         .attr("transform", "translate(200,0)");

	var nodes = tree.nodes(json),
	    links = tree.links(nodes);
	
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
	    .text(function(d) { return extractName(d.uri); });
  
  }
  
};
