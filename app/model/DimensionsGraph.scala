package model

import java.net.URI
import eu.cdevreeze.yaidom.core.EName


case class DimensionsGraphNode(ename: EName, children: IndexedSeq[DimensionsGraphNode])
case class DimensionsGraph(elr: String, graph: DimensionsGraphNode)
