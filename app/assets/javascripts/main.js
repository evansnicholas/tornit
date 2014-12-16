/*global require, requirejs */

/*jshint strict:false */

requirejs.config({
  paths: {
    'typeahead': ['../lib/typeaheadjs/typeahead.bundle'],
    'd3': ['../lib/d3js/d3'],
    'underscore': ['../lib/underscorejs/underscore'],
    'highlightjs': ['../bower_components/highlightjs/highlight.pack'],
    'bootstrap' : ['../lib/bootstrap/bootstrap'],
    'jquery' : ['../lib/jquery/jquery'],
    'angular': ['../lib/angularjs/angular'],
    'angular-route': ['../lib/angularjs/angular-route'],
    'angular-bootstrap': ['../bower_components/angular-bootstrap/ui-bootstrap-tpls.min']
  },
  shim: {
    'underscore': {
      exports : 'underscore'
    },
    'jquery': {
      exports : 'jquery'
    },
    'typeahead': {
      exports : 'typeahead'
    },
    'angular': {
      exports : 'angular'
    },
    'angular-route': {
      deps: ['angular'],
      exports : 'angular'
    },
    'highlightjs': {
      exports : 'hljs'
    },
    'angular-bootstrap' : {
      exports: 'angular-ui'
    }
  }
});

require(['jquery', './controllers', 'angular', 'angular-route'],
  function($, controllers, angular) {
   
    angular.module('taxoscopeApp', ['ngRoute', 'taxoscopeControllers']).
      config(['$routeProvider', function($routeProvider) {
      $routeProvider.
        when('/concept', { 
          templateUrl: 'assets/html/concept-viewer.html'
        }).
        when('/presentation', { 
          templateUrl: 'assets/html/presentation-viewer.html',
          controller: 'PresCtrl'
        }).
        when('/dtsGraph', { 
          templateUrl: 'assets/html/dts-graph-viewer.html',
          controller: 'DtsCtrl'
        }).
        when('/taxonomyDoc', {
          templateUrl: 'assets/html/taxonomy-document-viewer.html',
          controller: 'TaxoDocCtrl'
        }).
        otherwise({
          redirectTo: '/'
        });
    }]);

    angular.bootstrap(document, ['taxoscopeApp']);
   
    /*
    //Set the selected entrypoint and selected concept states to null
    $("body").data("entrypointUri", null);
    $("body").data("concept", null);

    $("#dts-graph-view-selection").click(function() {
      api.loadDtsGraphPage();
      api.setActiveListItem("#viewer-selection", "#"+$(this).attr("id"));
    });
    $("#presentation-view-selection").click(function() {
      api.loadPresentationViewerPage();
      api.setActiveListItem("#viewer-selection", "#"+$(this).attr("id"));
    });
    $("#concept-view-selection").click(function() {
      api.loadConceptViewerPage();
      api.setActiveListItem("#viewer-selection", "#"+$(this).attr("id"));
    });  

    api.initializeTypeahead();
   */
});
