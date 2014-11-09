var TaxoscopeApi = {

  test: function( text ) {
    alert(text);
  },

  setActiveListItem: function( listSelector, listItemSelector) {
    $(listSelector + " > li").removeClass("active");
    $(listItemSelector).addClass("active");
  },

  getSelectedEntrypoint: function() {
    var selectedEntrypoint = $("body").data("entrypointUri");
    return selectedEntrypoint;
  },

  getSelectedConcept: function() {
    var concept = $("body").data("concept");
    return concept;
  },

  doForSelectedEntrypoint: function( action ) {
    var selectedEntrypoint = TaxoscopeApi.getSelectedEntrypoint();
    if (selectedEntrypoint === null) 
      alert("No entrypoint selected!");
    else {
      action(selectedEntrypoint);
    }
  },

  doForSelectedConcept: function( action ) {
    var selectedConcept = TaxoscopeApi.getSelectedConcept();
    if (selectedConcept != null)
      TaxoscopeApi.doForSelectedEntrypoint(function( uri ) { 
        action(uri, selectedConcept);
      });
    else 
      alert("No concept selected!");
  },

  getDtsGraph: function(entrypointUri) {
    
    $.ajax({
      url: "dtsGraph",
      type: "GET",
      dataType : "json",
      data: {
        uri: encodeURI(entrypointUri)
      },

      success: function( json ) {
        var myItems = [];
        var resultsDiv = $( "#dts-graph-result" );
        resultsDiv.html( "" );
        Viz.displayDtsGraph( json, "#dts-graph-result" );
      },

      error: function( xhr, status, errorThrown ) {
        alert( "Sorry, there was a problem!" );
        console.log( "Error: " + errorThrown );
        console.log( "Status: " + status );
        console.dir( xhr );
      },
      // code to run regardless of success or failure
      complete: function( xhr, status ) {
      }
    });
  },

  initializeTypeahead:  function() {
    var entrypoints = new Bloodhound({
      datumTokenizer: Bloodhound.tokenizers.obj.whitespace('uri'),
      queryTokenizer: Bloodhound.tokenizers.whitespace,
      limit: 15,
      remote: '/entrypoints?uri=%QUERY'
    });   
       
    entrypoints.initialize();

    $('#entrypoint-select .typeahead')
      .typeahead({
        minLength: 1,
        highlight: true,
      },
      {
        name: 'entrypoints',
        displayKey: 'uri',
        source: entrypoints.ttAdapter() 
      })
      .on("typeahead:selected", function( event, sug, data ) {
        //This is the only place the selected entrypoint can and should be updated.
        var entrypointUri = sug.uri;
        $("body").data("entrypointUri", entrypointUri);
        $("#selected-entrypoint").text(entrypointUri);
        $('.typeahead').typeahead('val', '');
        $('#content').html("");
      });
  },

  initializeConceptTypeahead:  function( uri ) {
    var concepts = new Bloodhound({
      datumTokenizer: Bloodhound.tokenizers.obj.whitespace('localPart'),
      queryTokenizer: Bloodhound.tokenizers.whitespace,
      limit: 15,
      remote: '/concepts?uri='+ uri + '&concept=%QUERY'
    });   
       
    concepts.initialize();

    $('#concept-select .typeahead')
      .typeahead({
        minLength: 3,
        highlight: true,
      },
      {
        name: 'concepts',
        displayKey: 'localPart',
        source: concepts.ttAdapter() 
      })
      .on("typeahead:selected", function( event, sug, data ) {
         //This is the only place the selected concept can and should be updated.
         $('.typeahead').typeahead('val', '');
         var namespace = sug.namespace;
         var localPart = sug.localPart;
         $("body").data("concept", { namespace: namespace, localPart: localPart });
         $("#concept-header").html("<h1><span id=\"selected-concept-localPart\">" + localPart + " </span><small><span id=\"selected-concept-namespace\">" + namespace + "</span></small></h1>");
         $("#concept-info").html("");
      });
  },

  getDimensionGraphs: function( uri, concept ) {
    $.ajax({
      url: "dimensionsGraph",
      type: "GET",
      dataType : "json",
      data: {
        uri: encodeURI(uri),
        namespace: encodeURI(concept.namespace),
        localPart: concept.localPart         
      },

      success: function( json ) {
        var resultsDiv = $( "#concept-info" );
        resultsDiv.html( "" ); 
        _.each( json, function( element, index, list ) { 
           var divId = "dim-graph-result-"+ index
           resultsDiv.append("<div id='"+ divId + "' />");
           resultsDiv.append("<h4>ELR: "+element.elr+"</h4>");
           Viz.displayDimensionsGraph( element.graph, "#"+divId );
        });
      },

      error: function( xhr, status, errorThrown ) {
        alert( "Sorry, there was a problem!" );
        console.log( "Error: " + errorThrown );
        console.log( "Status: " + status );
        console.dir( xhr );
      },
      // code to run regardless of success or failure
      complete: function( xhr, status ) {
      }
    });
  },

  getTaxoDoc: function( entrypointUri, docUri ) {
    
    $.ajax({
      url: "taxoDoc",
      type: "GET",
      dataType : "text",
      data: {
        uri: encodeURI(entrypointUri),
        docUri: encodeURI(docUri)
      },

      success: function( json ) {
        var resultsDiv = $( "#doc-display pre code" );
        resultsDiv.text( json );
        resultsDiv.each(function(i, block){
         hljs.highlightBlock(this);
        });
      },

      error: function( xhr, status, errorThrown ) {
        alert( "Sorry, there was a problem!" );
        console.log( "Error: " + errorThrown );
        console.log( "Status: " + status );
        console.dir( xhr );
      },
      // code to run regardless of success or failure
      complete: function( xhr, status ) {
      }
    });
  },

  getPresentationElrs: function( entrypointUri ) {
    
    $.ajax({
      url: "presentationElrs",
      type: "GET",
      dataType : "json",
      data: {
        uri: encodeURI(entrypointUri)
      },

      success: function( elrs ) {
        var elrBtns = $("#presentation-elrs");
        _.each(elrs, function( element, index, list ) {
          var btn = "<li><a href=\"#\" class=\"elr-selector\">" + element + "</a></li>";
          elrBtns.append(btn)
          });
         $(".elr-selector").click(function(){
            var elr = $( this ).text();
            TaxoscopeApi.doForSelectedEntrypoint(function( uri ) { TaxoscopeApi.getPresentationTree( uri, elr ) });
        });
      },

      error: function( xhr, status, errorThrown ) {
        alert( "Sorry, there was a problem!" );
        console.log( "Error: " + errorThrown );
        console.log( "Status: " + status );
        console.dir( xhr );
      },
      // code to run regardless of success or failure
      complete: function( xhr, status ) {
      }
    });
  },

  getPresentationTree: function( entrypointUri, elr ) {
    $.ajax({
      url: "presentationTree",
      type: "GET",
      dataType : "json",
      data: {
        uri: encodeURI(entrypointUri),
        elr: elr
      },
    
      success: function( json ) {
        $("#selected-elr").text(elr);
        $("#presentation-display").html("");
        Viz.displayPresentationTree( json, "#presentation-display" );
      },

      error: function( xhr, status, errorThrown ) {
        alert( "Sorry, there was a problem!" );
        console.log( "Error: " + errorThrown );
        console.log( "Status: " + status );
        console.dir( xhr );
      },

      // code to run regardless of success or failure
      complete: function( xhr, status ) {
      }
    });
  },

  getConceptResources: function( entrypointUri, concept ) {
    
    $.ajax({
      url: "concept/references",
      type: "GET",
      dataType : "json",
      data: {
        entrypointUri: encodeURI(entrypointUri),
        conceptNamespace: encodeURI(concept.namespace),
        conceptLocalName: concept.localPart
      },

      success: function( jsonReferences ) {
       var formattedReferences =  
         _.map(jsonReferences, function(reference, index, list) {
           var parts = _.map(reference.parts, function(part, index, list) {       
             return "<dt>" + part.localName + "</dt><dd>" + part.value + "</dd>";
           });
           var formattedParts = "<dl class=\"dl-horizontal\">" + parts.join("") + "</dl>";
           return { role: reference.role, formattedParts: formattedParts };
         });
       
       var referencePanels = 
         _.map(formattedReferences, function(reference, index, list) { 
           return "<div class=\"panel panel-default\">" + 
                          "<div class=\"panel-heading\">" + 
                            "<h3 class=\"panel-title\">"+ reference.role + "</h3>"+ 
                          "</div>" + 
                          "<div class=\"panel-body\">" + 
                            reference.formattedParts + 
                          "</div>" + 
                        "</div>"
         });
       
       $("#concept-info").html(referencePanels.join(""));
      },

      error: function( xhr, status, errorThrown ) {
        alert( "Sorry, there was a problem!" );
        console.log( "Error: " + errorThrown );
        console.log( "Status: " + status );
        console.dir( xhr );
      },
      // code to run regardless of success or failure
      complete: function( xhr, status ) {
      }
    });
  },

  showTaxonomyDocument: function( docUri ) {
    $("#content").html('<h1 class="page-header">Doc Viewer</h1><div id="doc-display"><pre><code></code></pre></div>');
    TaxoscopeApi.doForSelectedEntrypoint(function( uri ){ TaxoscopeApi.getTaxoDoc(uri, docUri); });
  },

  loadDtsGraphPage: function() {
    $("#content").html('<h1 class="page-header">DTS Graph</h1><div id="dts-graph-result"></div>');
    TaxoscopeApi.doForSelectedEntrypoint(function( uri ){ TaxoscopeApi.getDtsGraph(uri); });
  },

  loadPresentationViewerPage: function() {
    $("#content").load('/assets/html/presentation-viewer.html', function() {
      TaxoscopeApi.doForSelectedEntrypoint(function( uri ){ TaxoscopeApi.getPresentationElrs( uri ); });
    });
  },

  loadConceptViewerPage: function() {
    $("#content").load('/assets/html/concept-viewer.html', function() {
      $("body").data("concept", null);
      TaxoscopeApi.doForSelectedEntrypoint(function( uri ) { TaxoscopeApi.initializeConceptTypeahead( uri ); });
      $("#resources-view").click(function() {
        TaxoscopeApi.setActiveListItem("#concept-view-selection", "#"+$(this).attr("id"));
        TaxoscopeApi.doForSelectedConcept(function(uri, concept) { 
          TaxoscopeApi.getConceptResources(uri, concept);
        });
      });
      $("#dimensions-view").click(function() { 
        TaxoscopeApi.setActiveListItem("#concept-view-selection", "#"+$(this).attr("id"));
        TaxoscopeApi.doForSelectedConcept(function(uri, concept) {
          TaxoscopeApi.getDimensionGraphs( uri, concept );
        });
      });
      $("#schema-view").click(function() { 
        alert("This functionality is not yet implemented");
      });
    });
  }

};
