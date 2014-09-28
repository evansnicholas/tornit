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
     
    function processDtsGraph( json ) {
      function processNode( node ) {
        var processedChildrenList = [];
        for ( var i = 0; i < node.children.length; i++ ) {
          var child = node.children[i];
          var processedChild = processNode( child );
          processedChildrenList.push( processedChild );
        }
        var processedChildren = processedChildrenList.join( "" );
        return "<li>" + node.uri + "<ul>" + processedChildren + "</ul></li>";
      }
      var processedGraph = processNode( json );
      return "<ul>"+ processedGraph + "</ul>";
    }


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
         console.log(sug);
         $('.typeahead').typeahead('val', '');
         
      });
  },

  loadDtsGraphPage: function() {
    $("#content").html('<h1 class="page-header">DTS Graph</h1><div id="dts-graph-result"></div>');
    TaxoscopeApi.doForSelectedEntrypoint(function( uri ){ TaxoscopeApi.getDtsGraph(uri); });
  },

  loadPresentationViewerPage: function() {
    $("#content").html('<h1 class="page-header">Presentation Viewer</h1><div id="presentation-viewer"></div>');
  },

  loadConceptViewerPage: function() {
    $("#content").load('/assets/html/concept-viewer.html', function() {
      TaxoscopeApi.doForSelectedEntrypoint(function( uri ){ TaxoscopeApi.initializeConceptTypeahead( uri ); });
    });
  }

};
