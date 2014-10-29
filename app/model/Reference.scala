package model

case class Reference(role: String, parts: IndexedSeq[ReferencePart]) {
  
  def abbreviate: AbbreviatedReference = {
    AbbreviatedReference(role, parts.map(_.abbreviate))
  }
}

case class ReferencePart(partNamespace: String, partLocalName: String, partValue: String) {
  
  def abbreviate: ReferenceAbbreviatedPart = ReferenceAbbreviatedPart(partLocalName, partValue)
}

case class AbbreviatedReference(role: String, parts: IndexedSeq[ReferenceAbbreviatedPart])

case class ReferenceAbbreviatedPart(partLocalName: String, partValue: String)
