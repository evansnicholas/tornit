package model

import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import java.net.URI
import eu.cdevreeze.yaidom.EName
import nl.ebpi.tqa.dimensionaware.DimensionalPathQueryApi
import nl.ebpi.tqa.model.dimensions.Primary

case class DimensionalGraphNode(concept: String, children: IndexedSeq[DimensionalGraphNode])

case class DimensionsGraph(elr: String, graph: DimensionalGraphNode)

object DimensionsGraph {
  def computeGraph(rat: RelationshipAwareTaxonomy, entrypointUri: URI, conceptEName: EName): Iterable[DimensionsGraph] = {
    val dpqa = DimensionalPathQueryApi(rat)
    val paths = dpqa.findDimensionalPaths filter { _.select[Primary].conceptDeclaration.targetEName == conceptEName  }
    val dimTree = DimensionalPathQueryApi.dimensionalTreeByElrByPrimary(paths)
    val graphs = dimTree(conceptEName) map { case(elr, tree) => 
        val hypercubeNodes = tree map { case((connection, hypercube), tree) =>
          val dimensionNodes = tree map { case(dimension, members) => 
            val memberNodes = members map { case mem => DimensionalGraphNode(mem.conceptDeclaration.targetEName.toString, IndexedSeq.empty[DimensionalGraphNode]) }
            DimensionalGraphNode(dimension.toString, memberNodes)
          }
          DimensionalGraphNode(hypercube.conceptDeclaration.targetEName.toString, dimensionNodes.toVector)
        }
        val conceptNode = DimensionalGraphNode(conceptEName.toString, hypercubeNodes.toVector)
        DimensionsGraph(elr, conceptNode)   
    }
    graphs
  }
}