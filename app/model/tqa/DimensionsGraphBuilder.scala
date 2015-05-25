package model.tqa

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import model.{ DimensionsGraph, DimensionsGraphNode } 
import nl.ebpi.tqa.queryapi.QueryableTaxonomy
import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import nl.ebpi.tqa.model.dimrelationship.DimensionalLinkRelationship
import shapeless._
import shapeless.HList.hlistOps
import scala.collection.mutable

object DimensionsGraphBuilder {
  
  def findDimensionsGraphs(qt: QueryableTaxonomy, conceptEName: EName): List[DimensionsGraph] = {
    
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
          case (graphBuilder, edge) => graphBuilder.addEdge(edge)
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
     * The elr is the elr of the edge that points to the new node.
     */
    def addEdge(edge: GraphEdge): GraphBuilder = {
      val fromEName = edge.from 
      val toEName = edge.to
      val edgeElr = edge.elr

      val updatedFrom =
        nodes.get(fromEName) match {
          case None =>
            //Pass None as the ELR because we cannot know it.  This will get updated at some point if there is an
            //edge pointing to the node.
            GraphBuilderNode(fromEName, None, Set(toEName))
          case Some(node) => node.addToNode(toEName)
        }
      
      val updatedTo = 
        nodes.get(toEName).map(_.updateElr(edgeElr)).getOrElse {
          GraphBuilderNode(toEName, Some(edgeElr), Set.empty[EName])
        }
      
      GraphBuilder(nodes.updated(updatedFrom.ename, updatedFrom).updated(updatedTo.ename, updatedTo))
    }
  }
  
  /**
   * A node in the graph.  The node is identified by its ename, and keeps track of the nodes that it points to.  In this
   * way this is a node in a directed graph.  Also contains the elr of the edge that connected to this node.
   */
  private case class GraphBuilderNode(ename: EName, elrOption: Option[String], toNodes: Set[EName]) {
    //This method is not stack safe, but this shouldn't be too much of a problem because dimensional graphs that start at a 
    //particular concept are quite small.
    def toDimensionsGraphNode(graphBuilder: GraphBuilder): DimensionsGraphNode = {
      val children = toNodes map { ename => 
        val node = graphBuilder.nodes(ename)
        node.toDimensionsGraphNode(graphBuilder)
      }
      DimensionsGraphNode(ename, elrOption, children.toVector)
    }
    
    def addToNode(newTo: EName): GraphBuilderNode = {
      GraphBuilderNode(ename, elrOption, toNodes + newTo)
    }
    
    def updateElr(newElr: String): GraphBuilderNode = {
      GraphBuilderNode(ename, Some(newElr), toNodes)
    }
  }
  
  /**
   * An edge of the graph that connects two nodes, identified by their ENames.  Also contains the elr containing
   * the relationship that gave rise to this graph edge.
   */
  private case class GraphEdge(from: EName, to: EName, elr: String)
  
  /**
   * Turns a relationship into a graph edge. The to and from can be reversed if desired (useful for domain member
   * relationships on the left side of the dimensional tree
   */
  private def relationshipToGraphEdge(rel: DimensionalLinkRelationship, reverse: Boolean): GraphEdge = {
    if (reverse) GraphEdge(rel.targetConceptEName, rel.sourceConceptEName, rel.extendedLinkRole) 
    else GraphEdge(rel.sourceConceptEName, rel.targetConceptEName, rel.extendedLinkRole) 
  }

}