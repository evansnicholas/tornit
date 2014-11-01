var TaxoscopeApi = {

  test: function( text ) {
    alert(text);
  },

  getSelectedEntrypoint: function() {
    var selectedEntrypoint = $("#selected-entrypoint").text();
    return selectedEntrypoint;
  },

  doForSelectedEntrypoint: function( action ) {
    var selectedEntrypoint = TaxoscopeApi.getSelectedEntrypoint();
    if (selectedEntrypoint.length === 0) 
      alert("No entrypoint selected!");
    else {
      action(selectedEntrypoint);
    }
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
        $('#selected-entrypoint').html(sug.uri);
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
         $('.typeahead').typeahead('val', '');
         TaxoscopeApi.getDimensionGraphs( sug.namespace, sug.localPart );
      });
  },

  getDimensionGraphs: function( namespace, localPart ) {
    TaxoscopeApi.doForSelectedEntrypoint( function( entrypointUri ) {
      $.ajax({
        url: "dimensionsGraph",
        type: "GET",
        dataType : "json",
        data: {
          uri: encodeURI(entrypointUri),
          namespace: encodeURI(namespace),
          localPart: localPart         
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
      TaxoscopeApi.doForSelectedEntrypoint(function( uri ){ TaxoscopeApi.initializeConceptTypeahead( uri ); });
    });
  }

};
