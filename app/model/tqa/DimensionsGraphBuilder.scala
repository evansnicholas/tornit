package model.tqa

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import model.{ DimensionsGraph, DimensionsGraphNode }
import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import nl.ebpi.tqa.model.dimrelationship.DimensionalLinkRelationship

object DimensionsGraphBuilder {
  
  def findDimensionsGraphs(qt: RelationshipAwareTaxonomy, conceptEName: EName): List[DimensionsGraph] = {
    
    val inheritedRelationshipChainsByBaseSetElr = 
      qt.findInheritedDimensionalRelationshipChains(conceptEName).groupBy(_.relationships.head.extendedLinkRole)
    
    
    val inheritedEdgesByElr = inheritedRelationshipChainsByBaseSetElr map {
      case (elr, chains) => 
        val edges = chains flatMap { chain =>
          chain.relationships map { rel => relationshipToGraphEdge(rel, false)}
        }
        (elr -> edges)
      }
    
    val incomingChainsByElr = 
      qt.findIncomingDomainMemberRelationshipChains(conceptEName).groupBy(_.relationships.head.extendedLinkRole)
      
    val incomingEdgesByElr = incomingChainsByElr map {
      case (elr, chains) =>
        val edges = chains flatMap { chain =>
          chain.relationships map { rel => relationshipToGraphEdge(rel, true)}
        }
        (elr -> edges)
    }
    
    val allEdges = inheritedEdgesByElr map {
      case (elr, edges) => 
        val newEdges = incomingEdgesByElr.get(elr).getOrElse(IndexedSeq.empty[GraphEdge])
        (elr -> (edges ++ newEdges))
    }
                
    
    //Construct the graph
    val graphsByElr = allEdges map {
      case (elr, edges) =>
        val graph = edges.foldLeft(GraphBuilder(Map.empty[EName, GraphBuilderNode])) {
          case (graphBuilder, edge) =>
            graphBuilder.update(edge.from, Some(edge.to)).update(edge.to, None)
        }

     (elr -> graph)
    }
       
   //A non stack safe attempt to construct the dimensional graph (start at the root concept and work outwards).
   (graphsByElr map { case (elr, graphBuilder) =>
     val rootNode = graphBuilder.nodes(conceptEName).toDimensionsGraphNode(graphBuilder)
     DimensionsGraph(elr, rootNode)
   }).toList
  }
  
  /**
   * A utility for constructing the DimensionsGraph.  Keeps track of all the nodes in the graph.
   */
  private case class GraphBuilder(nodes: Map[EName, GraphBuilderNode]) {
    /**
     * Updates a node in the graph with an optional new to node, or adds the node to the graph if it does not exist.
     */
    def update(nodeEName: EName, toOption: Option[EName]): GraphBuilder = {
      nodes.get(nodeEName) match {
        case None =>
          val newNode = GraphBuilderNode(nodeEName, (toOption map { Set(_) }).getOrElse(Set()))
          GraphBuilder(nodes + (newNode.ename -> newNode))
        case Some(node) =>
          val updatedNode = GraphBuilderNode(node.ename, (toOption map { node.toNodes + _ }).getOrElse(node.toNodes))
          GraphBuilder(nodes.updated(node.ename, updatedNode))
      }
    }
  }
  
  /**
   * A node in the graph.  The node is identified by its ename, and keeps track of the nodes that it points to.  In this
   * way this is a node in a directed graph.
   */
  private case class GraphBuilderNode(ename: EName, toNodes: Set[EName]) {
    //This method is not stack safe, but this shouldn't be too much of a problem because dimensional graphs that start at a 
    //particular concept are quite small.
    def toDimensionsGraphNode(graphBuilder: GraphBuilder): DimensionsGraphNode = {
      val children = toNodes map { ename => 
        val node = graphBuilder.nodes(ename)
        node.toDimensionsGraphNode(graphBuilder)
      }
      DimensionsGraphNode(ename, children.toVector)
    }
  }
  
  /**
   * An edge of the graph that connects two nodes, identified by their ENames.
   */
  private case class GraphEdge(from: EName, to: EName)
  
  /**
   * Turns a relationship into a graph edge. The to and from can be reversed if desired (useful for domain member
   * relationships on the left side of the dimensional tree
   */
  private def relationshipToGraphEdge(rel: DimensionalLinkRelationship, reverse: Boolean): GraphEdge = {
    if (reverse) GraphEdge(rel.targetConceptEName, rel.sourceConceptEName) 
    else GraphEdge(rel.sourceConceptEName, rel.targetConceptEName) 
  }

}