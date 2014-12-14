/*global define */

/*jshint strict:false */

define(['./viz-require', 'jquery', 'underscore'], function(Viz, $, _) {

   return {

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
    
        getEntrypoint: function(entrypointUri) {
            
            $.ajax({
                url: "entrypoint",
                type: "GET",
                dataType : "text",
                data: {
                    entrypointUri: encodeURI(entrypointUri)
                },

                success: function( json ) {
                    
                },

                error: function( xhr, status, errorThrown ) {
                    alert( "Sorry, there was a problem!" );
                    console.log( "Error: " + errorThrown );
                    console.log( "Status: " + status );
                    console.dir( xhr );
                },
                // code to run regardless of success or failure
                complete: function( xhr, status ) {
                    $('#loader-icon').removeClass('loading');
                }
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
                        var divId = "dim-graph-result-"+ index;
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
                        hljs.highlightBlock(block);
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
                        elrBtns.append(btn);
                    });
                    $(".elr-selector").click(function(){
                        var elr = $( this ).text();
                        doForSelectedEntrypoint(function( uri ) { getPresentationTree( uri, elr ); });
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
                                "</div>";
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

        getConceptSchemaInfo: function( entrypointUri, concept ) {
            $.ajax({
                url: "concept",
                type: "GET",
                dataType : "json",
                data: {
                    entrypointUri: encodeURI(entrypointUri),
                    conceptNamespace: encodeURI(concept.namespace),
                    conceptLocalName: concept.localPart         
                },
                
                success: function( json ) {
                    $("#concept-info").load('/assets/html/concept-schema-detail.html', function() {
                        var elementDeclaration = json.elementDeclaration;
                        $('#element-declaration')
                            .text(elementDeclaration) 
                            .each(function(i, block) {
                                hljs.highlightBlock(block);
                            });
                        Viz.displayENameHierarchy( json.substitutionGroupHierarchy, '#sub-hierarchy-graph' );
                        Viz.displayENameHierarchy( json.typeHierarchy, '#type-hierarchy-graph' );
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
        }

   };

});
