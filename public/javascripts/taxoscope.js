$( document ).ready(function() {
  $("#dts-graph-view-selection").click(function() {
    TaxoscopeApi.loadDtsGraphPage();
    $("#viewer-selection > li").removeClass("active");
    $(this).addClass("active");
  });
  $("#presentation-view-selection").click(function() {
    TaxoscopeApi.loadPresentationViewerPage();
    $("#viewer-selection > li").removeClass("active");
    $(this).addClass("active");
  });
  $("#concept-view-selection").click(function() {
    TaxoscopeApi.loadConceptViewerPage();
    $("#viewer-selection > li").removeClass("active");
    $(this).addClass("active");
  });  

  TaxoscopeApi.initializeTypeahead();
});
