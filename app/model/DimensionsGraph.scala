package model

import java.net.URI
import eu.cdevreeze.yaidom.core.EName

/**
 * @param ename The ename of the node
 * @param elrOption The elr of the arc with this node as its to concept.  This is an option because the root node
 * of the graph will not be pointed to by any arcs.
 * @param children The nodes that are pointed to by arcs with this node as its from concept.
 */
case class DimensionsGraphNode(ename: EName, elrOption: Option[String], children: IndexedSeq[DimensionsGraphNode])
case class DimensionsGraph(elr: String, graph: DimensionsGraphNode)
