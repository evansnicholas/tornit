var TaxoscopeApi = {

  test: function() {
    alert("hello");
  },

  getEntrypointsList: function() {
    // Using the core $.ajax() method
    $.ajax({
      // the URL for the request
      url: "/entrypoints",
      // whether this is a POST or GET request
      type: "GET",
      // the type of data we expect back
      dataType : "json",
      // code to run if the request succeeds;
      // the response is passed to the function
      success: function( json ) {
        var myItems = [];
        var myList = $( "#entrypointsList" );
        for ( var i = 0; i < json.length; i++ ) {
          myItems.push( "<li><a href=\"#\">" + json[i].uri + "</a></li>" );
        }
        myList.append( myItems.join( "" ) );
      },
      // code to run if the request fails; the raw request an
      // status codes are passed to the function
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
