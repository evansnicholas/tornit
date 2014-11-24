$( document ).ready(function() {
  //Set the selected entrypoint and selected concept states to null
  $("body").data("entrypointUri", null);
  $("body").data("concept", null);

  $("#dts-graph-view-selection").click(function() {
    TaxoscopeApi.loadDtsGraphPage();
    TaxoscopeApi.setActiveListItem("#viewer-selection", "#"+$(this).attr("id"));
  });
  $("#presentation-view-selection").click(function() {
    TaxoscopeApi.loadPresentationViewerPage();
    TaxoscopeApi.setActiveListItem("#viewer-selection", "#"+$(this).attr("id"));
  });
  $("#concept-view-selection").click(function() {
    TaxoscopeApi.loadConceptViewerPage();
    TaxoscopeApi.setActiveListItem("#viewer-selection", "#"+$(this).attr("id"));
  });  

  TaxoscopeApi.initializeTypeahead();
});
