
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

   console.log(positionedElrs);

   var svg = d3.select( container ).append("svg")
         .attr("width", "500")
         .attr("height", "1000")
         .append("g")
         .attr("transform", "translate(10,20)");

   var node = svg.selectAll("g.node")
	    .data(json)
	    .enter().append("g")
	    .attr("class", "node")
            .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });
	   
	
    node.append("circle")
	    .attr("r", 4.5);
	
    node.append("text")
	    .attr("dx", function(d) { return 7; })
	    .attr("dy", function(d) { return 3; })
	    .attr("text-anchor", "start")
	    .text(function(d) { return d.elr; })
            .on('click', function(d){ console.log(d); });

    node.each(Viz.appendRoots);

  },

  appendRoots: function( d, i ){
    
    var root = d3.select(this);
                  
    var rootNode = root.selectAll("g.node").data(d.roots).enter()
            .append("circle")
	    .attr("r", 4.5)
            .attr("cx", 15)
            .attr("cy", i * 15 + 15);
	
    rootNode.append("text")
	    .attr("dx", function(d) { return 21; })
	    .attr("dy", function(d) { return i*18 + 18; })
	    .attr("text-anchor", "start")
	    .text(function(d) { return d.concept.ename; })
            .on('click', function(d){ console.log(d); });      

    var positionedChildren = new Array();
    Viz.positionChildren(d.roots[0].children, 2, positionedChildren);
    console.log(positionedChildren);

    var child = root.selectAll("g.node").data(positionedChildren).enter()
        .append("g")
	.attr("class", "node")
        .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
      
    child.append("circle")
	    .attr("r", 4.5)
            .attr("cx", 15)
            .attr("cy", 15);
	    
    child.append("text")
	    .attr("dx", function(d) { return 21; })
	    .attr("dy", function(d) { return 18; })
	    .attr("text-anchor", "start")
	    .text(function(d) { return d.concept.ename; })
            .on('click', function(d){ console.log(d); }); 
  },

  positionChildren: function( children, depth, accumulatedChildren ) {
   var seenChildren = accumulatedChildren.length; 
   _.each(children, function( element, index, list ){
     var xPos = depth*15;
     var yPos = seenChildren + (index*50);
     element["x"] = xPos;
     element["y"] = yPos;
     accumulatedChildren.push(element);
     Viz.positionChildren(element.children, depth + 1, accumulatedChildren);
   });
  }
  
};
