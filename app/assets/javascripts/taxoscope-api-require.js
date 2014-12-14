/*global define */

/*jshint strict:false */

define(['./viz-require','./services-require', 'jquery', 'underscore', 'typeahead'], function(Viz, services, $, _) {
  
       function getSelectedEntrypoint() {
            var selectedEntrypoint = $("body").data("entrypointUri");
            return selectedEntrypoint;
        }

        function getSelectedConcept() {
            var concept = $("body").data("concept");
            return concept;
        }

       function  doForSelectedEntrypoint( action ) {
            var selectedEntrypoint = getSelectedEntrypoint();
            if (selectedEntrypoint === null) 
                alert("No entrypoint selected!");
            else {
                action(selectedEntrypoint);
            }
        }

        function doForSelectedConcept( action ) {
            var selectedConcept = this.getSelectedConcept();
            if (selectedConcept !== null)
                doForSelectedEntrypoint(function( uri ) { 
                    action(uri, selectedConcept);
                });
            else 
                alert("No concept selected!");
        }

    return {
        test: function( text ) {
            alert(text);
        },

        setActiveListItem: function( listSelector, listItemSelector) {
            $(listSelector + " > li").removeClass("active");
            $(listItemSelector).addClass("active");
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
                    $('body').data('entrypointUri', entrypointUri);
                    $('#selected-entrypoint').text(entrypointUri);
                    $('#loader-icon').addClass('loading');
                       doForSelectedEntrypoint( services.getEntrypoint );
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

        
        loadDtsGraphPage: function() {
            $("#content").load('/assets/html/dts-graph-viewer.html', function() {
                doForSelectedEntrypoint(function( uri ){ services.getDtsGraph(uri); });
            });
        },

        loadPresentationViewerPage: function() {
            $("#content").load('/assets/html/presentation-viewer.html', function() {
                doForSelectedEntrypoint(function( uri ){ services.getPresentationElrs( uri ); });
            });
        },

        loadConceptViewerPage: function() {
            $("#content").load('/assets/html/concept-viewer.html', function() {
                $("body").data("concept", null);
                doForSelectedEntrypoint(function( uri ) { initializeConceptTypeahead( uri ); });
                $("#resources-view").click(function() {
                    setActiveListItem("#concept-view-selection", "#"+$(this).attr("id"));
                    doForSelectedConcept(function(uri, concept) { 
                        services.getConceptResources(uri, concept);
                    });
                });
                $("#dimensions-view").click(function() { 
                    setActiveListItem("#concept-view-selection", "#"+$(this).attr("id"));
                    doForSelectedConcept(function(uri, concept) {
                        services.getDimensionGraphs( uri, concept );
                    });
                });
                $("#schema-view").click(function() { 
                    setActiveListItem("#concept-view-selection", "#"+$(this).attr("id"));
                    doForSelectedConcept(function(uri, concept) {
                        services.getConceptSchemaInfo( uri, concept );
                    });
                });
            });
        }

    };
});
