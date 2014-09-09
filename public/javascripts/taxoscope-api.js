var TaxoscopeApi = {

  test: function( text ) {
    alert(text);
  },

  getEntrypointsList: function() {
    $.ajax({
      url: "/entrypoints",
      type: "GET",
      dataType : "json",

      success: function( json ) {
        var myItems = [];
        var myList = $( "#entrypointsList" );
        for ( var i = 0; i < json.length; i++ ) {
          var uri = json[i].uri
          myItems.push( "<li onclick=\"TaxoscopeApi.getDtsGraph(" + "\'" + uri + "\'" + ")\"><a href=\"#\">" + uri + "</a></li>" );
        }
        myList.append( myItems.join( "" ) );
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
  }

};
