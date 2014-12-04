package model.tqa

import nl.ebpi.tqa.dimensionaware.DimensionalPathQueryApi
import nl.ebpi.tqa.model.dimensions.{ Primary, CompositeConnection }
import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import eu.cdevreeze.yaidom.core.EName
import java.net.URI
import model.{ DimensionsGraph, DimensionsGraphNode }

object DimensionsGraphBuilder {

  def findDimensionsGraphs(dimApi: DimensionalPathQueryApi, entrypointUri: URI, conceptEName: EName): List[DimensionsGraph] = {
    val dimTree = dimApi.dimensionalTreeByElrByPrimary
    val graphsOption = dimTree.get(conceptEName) map { elrs => 
      (elrs map {  case(elr, tree) => 
        val hypercubeNodes = tree map { case((connection, hypercube), tree) =>
          val dimensionNodes = tree map { case(dimension, members) => 
            val memberNodes = members map { case mem => 
              val mEName = mem.conceptDeclaration.targetEName
              DimensionsGraphNode(mEName, IndexedSeq.empty[DimensionsGraphNode]) 
            }
            DimensionsGraphNode(dimension, memberNodes)
          }
          val hEName = hypercube.conceptDeclaration.targetEName    
          createNodeFromCompositeConnection(connection, DimensionsGraphNode(hEName, dimensionNodes.toVector))
        }
        val conceptNode = DimensionsGraphNode(conceptEName, hypercubeNodes.toVector)
        DimensionsGraph(elr, conceptNode)   
      }).toList
    }
    graphsOption.getOrElse(List.empty[DimensionsGraph])
  }
  
  /**
   * Returns all the nodes in a composite connection except for the outermost node.
   */
  private def createNodeFromCompositeConnection(compositeConnection: CompositeConnection, 
      finalNode: DimensionsGraphNode): DimensionsGraphNode = {
    //The extending relations start with the concept the closest to the hypercube.  In this way the final node
    //is actually the first element in the vector.
    val extendingRelations = compositeConnection.extendingRelations
    extendingRelations.foldLeft(finalNode) { case (child, connection) => 
      DimensionsGraphNode(connection.sourceConceptEName, Vector(child))
    }
  }
  
}