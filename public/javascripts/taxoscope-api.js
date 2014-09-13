var TaxoscopeApi = {

  test: function( text ) {
    alert(text);
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
        //resultsDiv.append( processDtsGraph( json ) );
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
      remote: '/entrypoints?query=%QUERY'
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
        TaxoscopeApi.getDtsGraph(sug.uri);
        $('#selected-entrypoint').html(sug.uri);
        $('.typeahead').typeahead('val', 'Find entrypoint...');
      });
  },

  loadDtsGraphPage: function() {
    $("#content").html('<h1 class="page-header">DTS Graph</h1><div id="dts-graph-result"></div>');
    var selectedEntrypoint = $("#selected-entrypoint").text();
    if (selectedEntrypoint.length > 0) {
     TaxoscopeApi.getDtsGraph(selectedEntrypoint);
    }
  },

  loadPresentationViewerPage: function() {
    $("#content").html('<h1 class="page-header">Presentation Viewer</h1><div id="presentation-viewer"></div>');
  }

};
