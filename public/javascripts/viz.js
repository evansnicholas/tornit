
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

  displayPresentationTree: function( elrJson, container ){
 
   var positionedConcepts = new Array();
   var totalConcepts = Viz.positionConcepts(elrJson.roots, 0, 0, positionedConcepts);
   var height = totalConcepts * 25;

   var svg = d3.select( container ).append("svg")
         .attr("width", "1000")
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
	   // .attr("text-anchor", "start")
	    .text(function(d) { return d.concept.ename; })
            .each(function(d) { 
              var labelHtml = Viz.formatLabelPopoverHtml(d.concept.labels);
              $(this).popover({title: "Labels", html: true, container: "#presentation-display", content: labelHtml, placement: "right", trigger: "click" }); 
            });
   
  },

  positionConcepts: function( concepts, depth, totalSeenConcepts, accumulatedConcepts ) {
   function positionConcept( memo, element, index, list){
     var xPos = depth*15;
     var yPos = memo*20;
     element["x"] = xPos;
     element["y"] = yPos;
     accumulatedConcepts.push(element);
     var totalSeenConcepts = Viz.positionConcepts(element.children, depth + 1, memo + 1, accumulatedConcepts);
     return totalSeenConcepts;
   }
 
   var placedConceptsCount = _.reduce(concepts, positionConcept, totalSeenConcepts);   

   return placedConceptsCount; 
  },

  formatLabelPopoverHtml: function( labels ) {
    var formattedLabels = _.map(labels, function(label, key, list) {
      return "<dl><dt>Language</dt><dd>"+ label.language + "</dd><dt>Role</dt><dd>" + label.role + "</dd><dt>Text</dt><dd>" + label.text + "</dd></dl>";
    });
    
    var labelHtml = _.reduce(formattedLabels, function(memo, value, index, list) {
      return memo + "<hr/>" + value;
    });
   
    return labelHtml;
  }

};
