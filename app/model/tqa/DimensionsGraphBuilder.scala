package model.tqa

import java.net.URI

import scala.collection.immutable

import eu.cdevreeze.yaidom.core.EName
import model.{ DimensionsGraph, DimensionsGraphNode } 
import nl.ebpi.tqa.model.dimensions._
import nl.ebpi.tqa.model.dimrelationship.{ DimensionalLinkRelationship, DomainMemberRelationship }
import nl.ebpi.tqa.dimensionaware.{ DimensionalPathBuilder, DimensionalPathQueryApi }
import nl.ebpi.tqa.dimensionaware.DimensionalPathQueryApi._
import nl.ebpi.tqa.model.dimensions.{ CompositeConnection, Dimension, Hypercube, Primary }
import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import shapeless._
import shapeless.HList.hlistOps
import scala.collection.mutable

object DimensionsGraphBuilder {
  
  def findDimensionsGraphs(dimApi: DimensionalPathQueryApi, entrypointUri: URI, conceptEName: EName): List[DimensionsGraph] = {
    val fullPaths: immutable.IndexedSeq[FullPath] = dimApi.findDimensionalPaths.filterConcretePrimary
    val dimPaths = DimensionalPathBuilder[Primary :: Hypercube :: Dimension :: HNil](dimApi.relationshipAwareTaxonomy).filterConcretePrimary
    val hycPaths = DimensionalPathBuilder[Primary :: Hypercube :: HNil](dimApi.relationshipAwareTaxonomy).filterConcretePrimary

    val relevantFullPaths = fullPaths.filter(_.select[Primary].conceptDeclaration.targetEName == conceptEName)
    val relevantDimPaths = dimPaths.filter(_.select[Primary].conceptDeclaration.targetEName == conceptEName)
    val relevantHycPaths = hycPaths.filter(_.select[Primary].conceptDeclaration.targetEName == conceptEName)
    
    
    val fullPathEdgesByElr = relevantFullPaths.groupBy(_.select[HasHypercubeConnection].elr) map {
      case (elr, paths) => 
        val allEdges = paths flatMap { path =>
          val hasHypConn = path.select[HasHypercubeConnection]
          val leftDomMemEdges = hasHypConn.extendingRelations map { rel => relationshipToGraphEdge(rel, true) }
          val hasHypEdge = relationshipToGraphEdge(hasHypConn.baseRelation)
          val hypDimEdge = relationshipToGraphEdge(path.select[HypercubeDimensionConnection].baseRelation)
          val domMemRightEdges = path.select[DimensionMemberConnection] match {
            case emc: DimensionExplicitMemberConnection => emc.allRelations map { rel => relationshipToGraphEdge(rel) }
            case tmc: DimensionTypedMemberConnection => Vector.empty[GraphEdge]
         }
          leftDomMemEdges ++ Vector(hasHypEdge, hypDimEdge) ++ domMemRightEdges
        } 
        (elr -> (allEdges.toVector))
    }
    
    val dimPathEdgesByElr = relevantDimPaths.groupBy(_.select[HasHypercubeConnection].elr) map {
      case (elr, paths) => 
        val allEdges = paths flatMap { path =>
          val hasHypConn = path.select[HasHypercubeConnection]
          val leftDomMemEdges = hasHypConn.extendingRelations map { rel => relationshipToGraphEdge(rel, true) }
          val hasHypEdge = relationshipToGraphEdge(hasHypConn.baseRelation)
          val hypDimEdge = relationshipToGraphEdge(path.select[HypercubeDimensionConnection].baseRelation)
          leftDomMemEdges ++ Vector(hasHypEdge, hypDimEdge)
        }
        (elr -> (allEdges.toVector))
    }
    
    val hycPathByElr = relevantHycPaths.groupBy(_.select[HasHypercubeConnection].elr) map {
      case (elr, paths) => 
        val allEdges = paths flatMap { path =>
          val hasHypConn = path.select[HasHypercubeConnection]
          val leftDomMemEdges = hasHypConn.extendingRelations map { rel => relationshipToGraphEdge(rel, true) }
          val hasHypEdge = relationshipToGraphEdge(hasHypConn.baseRelation)
          leftDomMemEdges ++ Vector(hasHypEdge)
        }
        (elr -> (allEdges.toVector))
    }
    
    import scalaz.Scalaz._

    // Combine the contents of maps without overwritting existing values.  If a key for a vector already exists, append the values 
    // to the existing vector rather than replacing it.
    // See http://www.nimrodstech.com/scala-map-merge/ for an explanation on map merge using scalaz semigroups.
      
    val allDimensionalEdgesByElr = fullPathEdgesByElr |+| fullPathEdgesByElr |+| hycPathByElr

    //Construct the graph
    val graphsByElr = allDimensionalEdgesByElr map {
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
  private def relationshipToGraphEdge(rel: DimensionalLinkRelationship, reverse: Boolean = false): GraphEdge = {
    if (reverse) GraphEdge(rel.targetConceptEName, rel.sourceConceptEName)
    else GraphEdge(rel.sourceConceptEName, rel.targetConceptEName) 
  }

}