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
        var width = (maxDepth+1)*275;
          
        var endLabels = _.filter(nodes, function(node) { return node.depth == maxDepth;  });
        var longestEndLabelLength = d3.max(endLabels, function(node) { 
          return options.extractNodeLabel(node).length;
        });

        var widthAdjustement = (options.extractNodeLabel(nodes[0]).length + longestEndLabelLength)*7;

        //Assume all node labels are about the same length
        var labelLength = options.extractNodeLabel(nodes[0]).length;    

        tree.size([height, width - widthAdjustement]);
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

    function positionHierarchicalENames( enames ) {
       var positionedENames = 
            _.map(enames, function(value, index, list) {
                var y = index*30;
                return { x: 0, y: y, namespace: value.namespace, localName: value.localName };
            });
        return positionedENames;
    }
    
    var line = d3.svg.line()
                    .x(function(d) { return d.x; })
                    .y(function(d) { return d.y; })
                    .interpolate("linear");   

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

          function toggleSidebar() {
            if ($scope.showSidebar) {
              $scope.showSidebar = false;
              $scope.mainOffset = "col-md-offset-1"; 
            } else {
             $scope.showSidebar = true; 
             $scope.mainOffset = "col-md-offset-2";
            }
          }

          $scope.menuItems = menuItems;
          $scope.isActive = isActive;
          $scope.routeMenuItem = routeMenuItem;
          $scope.showSidebar = true;
          $scope.toggleSidebar = toggleSidebar;
          $scope.mainOffset = "col-md-offset-2";

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
                }, 
                function(reason) {
              });
            });
          };
        }]).
        controller('DtsCtrl', ['$scope', '$http', '$routeParams', '$location', function($scope, $http, $routeParams, $location) {
            $scope.isLoading = "loading";
            $http.get('dtsGraph', {
              params: {
                uri: $routeParams.uri
              }
            }).success(function(data) { 
                 
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
                $scope.isLoading = "notLoading";
            }).error(function(error) { 
              $scope.isLoading = "notLoading";
          });
        }]).
        controller('TaxoDocCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {
            $http.get('/taxoDoc', { 
              params: {
                uri: $routeParams.uri,
                docUri: $routeParams.docUri
              }
            }).success(function(data) {
                $scope.toHighlight = data;
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
                
                   
                   
                   $scope.dimGraphs = response.data;

                   /*$scope.dimGraphs = _.map(response.data, function(value, key, list) {
                    
                 
                  var graph = processGraph(value.graph, { extractNodeLabel: extractNodeLabel, widthScale: 50 });
                  return { elr: value.elr, graph: graph };
});*/
            });
                  

        }]).
        controller('SchemaViewCtrl', ['$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {

          $http.get('/concept/schema', { 
              params: {
                entrypointUri: $routeParams.uri,
                conceptNamespace: $routeParams.conceptNamespace,
                conceptLocalName: $routeParams.conceptLocalName
              }
            }).success(function(data) {
                $scope.toHighlight = data.elementDeclaration;
                var positionedTypes = positionHierarchicalENames(data.typeHierarchy);
                var positionedSubstGrps = positionHierarchicalENames(data.substitutionGroupHierarchy);
                $scope.typeHierarchyHeight = positionedTypes.length*30;
                $scope.types = positionedTypes;
                $scope.substitutionGroupHeight = positionedSubstGrps.length*30;
                $scope.substitutionGroups = positionedSubstGrps;
                $scope.line = line;
              });         

        }]).
        directive('highlightCode', function() {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                  scope.$watch('toHighlight', function(value) {
                    if (value) { element.html(hljs.highlight('xml', value).value); }
                  });
                }
            };
        }).
        directive('dimensionsGraph', function() {
          return {
            restrict: 'EA',
            scope: {
              data: '='  
            },
            link: function(scope, element, attrs) {

                function extractNodeLabel(node) {
                   return node.ename.localName;
                }

                // watch for data changes and re-render
                var unbindWatcher = scope.$watch('data', function(newVals, oldVals) {
                  scope.render(newVals);
                }, true);

                var svg = d3.select(element[0])
                  .append("svg")
                  .style('width', '100%');

                scope.render = function(data) { 
                                        
                    if (!data) { return; }

                    /* Since we have data unbind the watcher */
                    unbindWatcher();

                    var tree = d3.layout.tree();
                    var diagonal = d3.svg.diagonal()
                        .projection(function(d) { return [d.y, d.x]; });

                    /* Compute node positions to determine sv height */
                    var nodes = tree.nodes(data);
                    var nodesPerDepth = _.countBy(nodes, function(node){ return node.depth; });
                    var maxNodes = _.max(nodesPerDepth);
                    var height = maxNodes*30;
                    var width = d3.select(element[0]).node().offsetWidth;
                    var labelLength = extractNodeLabel(data).length;
                    tree.size([height, width - labelLength*20*2]);                  

                    nodes = tree.nodes(data);
                    links = tree.links(nodes);

                    svg.selectAll('*').remove();

                    var g = svg.attr("width", width)
                        .attr("height", height)
                        .append("g")
                        .attr("transform", "translate("+labelLength*15+",0)");
                    
                    var link = g.selectAll("path.link")
                        .data(links)
                        .enter().append("path")
                        .attr("class", "link")
                        .attr("d", diagonal);
                    
                    var node = g.selectAll("node")
                        .data(nodes)
                        .enter().append("g")
                        .attr("class", "node")
                        .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });
                    node.append("circle")
                        .attr("r", 4.5);
                    node.append("text")
                        .attr("dx", function(d) { return d.children ? -8 : 8; })
                        .attr("dy", function(d) { return (d.depth % 2 === 0) ? -5 : 5; })
                        .attr("text-anchor", function(d) { return d.children ? "end" : "start"; })
                        .text(function(d) { return d.ename.localName; })
                        .on('click', function(d){ console.log(d); });
                }; 
            }
          };
       });
});
