
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
            .on('click', function(d){ console.log(d.uri); TaxoscopeApi.showTaxonomyDocument( d.uri ); });
  
  },

  displayDimensionsGraph: function( json, graphContainer ) {
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
    var labelLength = 125;    

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
	    .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });
	
    node.append("circle")
	    .attr("r", 4.5);
	
    node.append("text")
	    .attr("dx", function(d) { return d.children ? -8 : 8; })
	    .attr("dy", function(d) { return (d.depth % 2 === 0) ? -5 : 5; })
	    .attr("text-anchor", function(d) { return d.children ? "end" : "start"; })
	    .text(function(d) { return d.localPart; })
            .on('click', function(d){ console.log(d); });
  
  },

  displayPresentationTree: function( json, container ){

   var positionedElrs = _.each(json, function( element, index, list ){
     var xPos = index*40;
     element["x"] = xPos;
     element["y"] = 0;
   });

   var firstElr = json[0]; 
   var positionedConcepts = new Array();
   var totalConcepts = Viz.positionConcepts(firstElr.roots, 0, 0, positionedConcepts);
   var height = totalConcepts * 15
 
   var svg = d3.select( container ).append("svg")
         .attr("width", "500")
         .attr("height", height)
         .append("g")
         .attr("transform", "translate(10,20)");

   var node = svg.selectAll("g.node")
	    .data(positionedConcepts)
	    .enter().append("g")
	    .attr("class", "node")
            .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
	   
	
    node.append("circle")
	    .attr("r", 4.5);
	
    node.append("text")
	    .attr("dx", function(d) { return 7; })
	    .attr("dy", function(d) { return 3; })
	    .attr("text-anchor", "start")
	    .text(function(d) { return d.concept.ename; })
            .on('click', function(d){ console.log(d); });
   
  },

  positionConcepts: function( concepts, depth, totalSeenConcepts, accumulatedConcepts ) {
   function positionConcept( memo, element, index, list){
     var xPos = depth*15;
     var yPos = memo*15;
     element["x"] = xPos;
     element["y"] = yPos;
     accumulatedConcepts.push(element);
     var totalSeenConcepts = Viz.positionConcepts(element.children, depth + 1, memo + 1, accumulatedConcepts);
     console.log("subtotal " + totalSeenConcepts);
     return totalSeenConcepts;
   }
 
   var additionalConcepts = _.reduce(concepts, positionConcept, totalSeenConcepts);   

   return additionalConcepts; 
  }

};
