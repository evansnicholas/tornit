package model

import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import nl.ebpi.tqa.model.relationship.ParentChildRelationship
import eu.cdevreeze.yaidom.EName

case class PresentationELR(elr: String, roots: IndexedSeq[PresentationNode])
case class PresentationNode(concept: PresentationConcept, children: IndexedSeq[PresentationNode])
case class PresentationConcept(ename: String, labels: IndexedSeq[Label])
case class Label(role: String, language: String, text: String)

object PresentationTree {
  
  def createPresentationTree(rat: RelationshipAwareTaxonomy): List[PresentationELR] = {
    val parentChildRelsByElr = rat.findRelationships[ParentChildRelationship] groupBy { _.extendedLinkRole }
    (parentChildRelsByElr map { case (elr, rels) => processRelationshipsForElr(rat)(rels) }).toList
  }
  
  /*
   * This method assumes that all the relationships that are passed are from the same elr.
   */
  private def processRelationshipsForElr(rat: RelationshipAwareTaxonomy)(relationships: IndexedSeq[ParentChildRelationship]): PresentationELR = {
    val elr = relationships.head.extendedLinkRole
    val parentChildRelsBySource = relationships.groupBy(_.sourceConceptEName)
    val allChildren = relationships.map(_.targetConceptEName).toSet
    val allParents = parentChildRelsBySource.keySet
    val roots = allParents diff allChildren
    
    def build(source: EName): PresentationNode = {
      val childrenRels = parentChildRelsBySource.getOrElse(source, Vector()).sortBy(_.order)
      val childNodes = childrenRels map { r => build(r.targetConceptEName) }
      val labelRels = rat.findConceptLabelsByConcept(source)
      val labels = labelRels map { r => Label(r.resourceRole, r.language, r.labelText) }
      val concept = PresentationConcept(source.localPart, labels)
      PresentationNode(concept, childNodes)
    }
    
    PresentationELR(elr, roots.toVector map build)
  }
  
}