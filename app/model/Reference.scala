package model

case class Reference(role: String, parts: IndexedSeq[ReferencePart])

case class ReferencePart(partNamespace: String, partLocalName: String, partValue: String)
