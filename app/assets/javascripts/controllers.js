/*global define */

/*jshint strict:false */

define(['angular', 'd3', 'underscore', 'highlightjs', 'angular-bootstrap'], function(angular, d3, _, hljs, ui) {

    function processDtsGraph(data) {
        function extractName(uri) {
            var array = uri.split("/");
            var length = array.length;
            return array[length - 1];
        }

        var tree = d3.layout.tree();
        
        var diagonal = d3.svg.diagonal()
            .projection(function(d) { return [d.y, d.x]; });

        var nodes = tree.nodes(data),
	links = tree.links(nodes);
	
        var maxDepth = d3.max(nodes, function(node){ return node.depth; });
        var nodesPerDepth = _.countBy(nodes, function(node){ return node.depth; });
        var maxNodes = _.max(nodesPerDepth);
        var height = maxNodes*30;
        var width = (maxDepth+1)*250;
        //Assume all node labels are about the same length
        var labelLength = extractName(nodes[0].uri).length*6;    

        tree.size([height, width - labelLength*2.5]);
        nodes = tree.nodes(data);
        links = tree.links(nodes);

        _.map(links, function(value, key, list) {
            value.d = diagonal(value);
            return value;
        });

        _.map(nodes, function(value, key, list) {
            value.fileName = extractName(value.uri);
            return value;
        });

        return { height: height, width: width, nodes: nodes, links: links };
    }

    positionConcepts: function( concepts, depth, totalSeenConcepts, accumulatedConcepts ) {
        function positionConcept( memo, element, index, list){
            var xPos = depth*15;
            var yPos = memo*20;
            element.x = xPos;
            element.y = yPos;
            accumulatedConcepts.push(element);
            var totalSeenConcepts = Viz.positionConcepts(element.children, depth + 1, memo + 1, accumulatedConcepts);
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
            return $location.path() === item.path;
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
                    var processedGraph = processDtsGraph(data);
                    $scope.height = processedGraph.height;
                    $scope.width = processedGraph.width;
                    $scope.nodes = processedGraph.nodes;
                    $scope.links = processedGraph.links;
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

          function displayPresentationTree(elr) {
            $http.get('/presentationTree', {
              params: {
                uri: $routeParams.uri,
                elr: elr
              }
            })

            var positionedConcepts = [];
            var totalConcepts = Viz.positionConcepts(elrJson.roots, 0, 0, positionedConcepts);
          }
          
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
