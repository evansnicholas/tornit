/*global define */

/*jshint strict:false */

define(['angular', 'd3', 'underscore', 'highlightjs','angular-ui'], function(angular, d3, _, hljs, ui) {

    function processGraph(data, options) {
        var tree = d3.layout.tree();
        
        var diagonal = d3.svg.diagonal()
            .projection(function(d) { return [d.y, d.x]; });

        var nodes = tree.nodes(data),
	links = tree.links(nodes);
	
        var maxDepth = d3.max(nodes, function(node){ return node.depth; });
        var nodesPerDepth = _.countBy(nodes, function(node){ return node.depth; });
        var maxNodes = _.max(nodesPerDepth);
        var height = maxNodes*30;
        var width = (maxDepth+1)*300;
        //Assume all node labels are about the same length
        var labelLength = options.extractNodeLabel(nodes[0]).length;    

        tree.size([height, width - labelLength*options.widthScale]);
        nodes = tree.nodes(data);
        links = tree.links(nodes);

        _.map(links, function(value, key, list) {
            value.d = diagonal(value);
            return value;
        });

        _.map(nodes, function(value, key, list) {
            value.label = options.extractNodeLabel(value);
            return value;
        });

        function formatTranslate(x,y){
          return "translate("+x+","+y+")";
        }

        return { height: height, width: width, nodes: nodes, links: links, formatTranslate: formatTranslate };
    }

    function positionConcepts( concepts, depth, totalSeenConcepts, accumulatedConcepts ) {
        function positionConcept( memo, element, index, list){
            var xPos = depth*15;
            var yPos = memo*20;
            element.x = xPos;
            element.y = yPos;
            accumulatedConcepts.push(element);
            var totalSeenConcepts = positionConcepts(element.children, depth + 1, memo + 1, accumulatedConcepts);
            return totalSeenConcepts;
        }
        
        var placedConceptsCount = _.reduce(concepts, positionConcept, totalSeenConcepts);   

        return placedConceptsCount; 
    }

    /* Controllers */
    var taxoscopeControllers = angular.module('taxoscopeControllers', ['ui.bootstrap']);

    taxoscopeControllers.
        controller('MainCtrl', ['$scope', '$routeParams', '$location', function($scope, $routeParams, $location) {
          var menuItems = [
            { 
              label: "DTS graph",
              path: "/dtsGraph",
            },
            {
              label: "Presentation",
              path: "/presentation",
            },
            {
              label: "Concepts",
              path: "/concept",
            }
          ];

          function isActive(item) {
            return $location.path().search(item.path) > -1;
          }
  
          function routeMenuItem(item) {
            if ($location.path() === "/") {
               //do nothing, stay here 
            } else {
              $location.path(item.path).search({uri: $routeParams.uri});
            }
          }

          $scope.menuItems = menuItems;
          $scope.isActive = isActive;
          $scope.routeMenuItem = routeMenuItem;
        }]).
        controller('NavCtrl', ['$scope', '$http', '$location', function($scope, $http, $location) {
          $scope.selectEntrypoint = function(item, model, label) {
            $location.path('/dtsGraph').search({ uri: item });
          };
          $scope.getEntrypoints = function(val) {
            return $http.get('entrypoints', {
                params: {
                  uri: val
                }
              }).then(function(response) {
                return response.data.slice(0, 15).map(function(item) {
                  return item.uri;
                });
            });
          };
        }]).
        controller('DtsCtrl', ['$scope', '$http', '$routeParams', '$location', function($scope, $http, $routeParams, $location) {
           
            $http.get('dtsGraph', {
              params: {
                uri: $routeParams.uri
              }
            }).
                success(function(data) { 
                 
                function extractName(node) {
                  var array = node.uri.split("/");
                  var length = array.length;
                
                  return array[length - 1];
                }

                   var processedGraph = processGraph(data, { extractNodeLabel: extractName, widthScale: 10  });
                    $scope.graph = processedGraph;
                    $scope.getTaxoDoc = function(docUri) {
                      $location.path('/taxonomyDoc').search({ uri: $routeParams.uri, docUri: docUri  });
                    };
                });
        }]).
        controller('TaxoDocCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {
            $http.get('/taxoDoc', { 
              params: {
                uri: $routeParams.uri,
                docUri: $routeParams.docUri
              }
            }).success(function(data) {
                $scope.taxoDoc = data;
                $scope.docLoaded = true;
            });
        }]).
        controller('PresCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
          $http.get('/presentationElrs', {
            params: {
              uri: $routeParams.uri
            }
          }).success(function(data) {
            $scope.elrs = data;
          });

          $scope.displayPresentationTree = function(elr) {
            $http.get('/presentationTree', {
              params: {
                uri: $routeParams.uri,
                elr: elr
              }
            }).success(function(data) {

              var positionedConcepts = [];
              var totalConcepts = positionConcepts(data.roots, 0, 0, positionedConcepts);
              var height = totalConcepts * 25;

              $scope.height = height;
              $scope.nodes = positionedConcepts;
            });
           };
        }]).
        controller('ConceptCtrl', ['$scope', '$http', '$routeParams', '$location', function($scope, $http, $routeParams, $location) {
          var conceptViews = [
            { 
              label: "Resources",
              path: "/concept/reference",
              template: "assets/html/reference-viewer.html"
            },
            {
              label: "Dimensions",
              path: "/concept/dimensions"
            },
            {
              label: "Schema",
              path: "/concept/schema",
            }
          ];
                
          function routeConceptView(view) {
            if ($location.path() === "/") {
               //do nothing, stay here 
            } else {
              $location.path(view.path).search({uri: $routeParams.uri, conceptNamespace: getConceptNamespace(), conceptLocalName: getConceptLocalName()});
            }
          }

          function getConcepts(conceptQuery) {
            return $http.get('concepts', {
                params: {
                  uri: $routeParams.uri,
                  concept: conceptQuery
                }
              }).then(function(response) {
                 return response.data.slice(0, 15).map(function(concept) {
                  return concept;
                });
            });
          }


          function getViewTemplate() {
            if ($routeParams.view) return "assets/html/" + $routeParams.view + "-viewer.html";
          }
  
          function selectConcept(item, model, label) { 
            $location.path(conceptViews[0].path).search({uri: $routeParams.uri, conceptNamespace: item.namespace, conceptLocalName: item.localPart});
          }

          function getConceptLocalName() {
            return $routeParams.conceptLocalName;
          }
    
          function getConceptNamespace() {
            return $routeParams.conceptNamespace;
          }
         
          $scope.getViewTemplate = getViewTemplate;
          $scope.getConceptLocalName = getConceptLocalName;
          $scope.getConceptNamespace = getConceptNamespace;
          $scope.selectConcept = selectConcept;
          $scope.getConcepts = getConcepts;
          $scope.conceptViews = conceptViews;
          $scope.routeConceptView = routeConceptView;
        }]).
        controller('ReferenceCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
        
          $http.get('concept/references', {
                params: {
                  entrypointUri: $routeParams.uri,
                  conceptNamespace: $routeParams.conceptNamespace,
                  conceptLocalName: $routeParams.conceptLocalName
                }
              }).then(function(response) {
                $scope.references = response.data;
            });

        }]).
        controller('DimensionsCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
          $http.get('concept/dimensions', {
                params: {
                  entrypointUri: $routeParams.uri,
                  conceptNamespace: $routeParams.conceptNamespace,
                  conceptLocalName: $routeParams.conceptLocalName
                }
              }).then(function(response) {
                
                   function extractNodeLabel(node) {
                     return node.ename.localName;
                   }
                   $scope.dimGraphs = _.map(response.data, function(value, key, list) {
                    
                 
                  var graph = processGraph(value.graph, { extractNodeLabel: extractNodeLabel, widthScale: 50 });
                  return { elr: value.elr, graph: graph };
});              
            });
                  

        }]).
        directive('highlightCode', function() {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    //Observe the docLoaded attribute to only highlight the xml once
                    // it is actually loaded.
                    attrs.$observe('docLoaded', function(value) {
                        if (value)
                            element.html(hljs.highlight('xml', scope.taxoDoc).value);
                        else {} 
                        //do nothing because the taxoDoc is not yet loaded.
                    });
                }
            };
        });
    

});
