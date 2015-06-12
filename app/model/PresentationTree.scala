package model

import eu.cdevreeze.yaidom.core.EName

case class PresentationELR(elr: String, roots: Seq[PresentationNode])
case class PresentationNode(concept: PresentationConcept, children: Seq[PresentationNode])
case class PresentationConcept(ename: EName, labels: Seq[Label])
case class Label(role: String, language: String, text: String)
