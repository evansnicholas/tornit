package controllers

import scala.util._
import play.api.libs.json._
import model._
import play.api.libs.functional.syntax._
import eu.cdevreeze.yaidom.core.EName

object Writers {

  implicit val entrypointWrites: Writes[Entrypoint] =
    ((JsPath \ "uri").write[String].contramap((f: Entrypoint) => f.uri))

  implicit val enameWrites = new Writes[EName] {
    def writes(ename: EName): JsObject = {
      val namespace = ename.namespaceUriOption.getOrElse("")
      Json.obj(
        "namespace" -> namespace,
        "localName" -> ename.localPart)
    }
  }

  implicit val conceptWrites: Writes[Concept] = (
    (JsPath \ "namespace").write[String] and
    (JsPath \ "localPart").write[String] and
    (JsPath \ "conceptType").write[String])(unlift(Concept.unapply))

  implicit lazy val dtsGraphWrites: Writes[DtsGraph] = (
    (JsPath \ "uri").write[String] and
    (JsPath \ "children").lazyWrite(Writes.seq[DtsGraph](dtsGraphWrites)))(unlift(DtsGraph.unapply))

  implicit val dimGraphWrites: Writes[DimensionsGraph] = (
    (JsPath \ "elr").write[String] and
    (JsPath \ "graph").write[DimensionsGraphNode])(unlift(DimensionsGraph.unapply))

  implicit lazy val dimGraphNodeWrites: Writes[DimensionsGraphNode] = (
    (JsPath \ "ename").write[EName] and
    (JsPath \ "elrOption").write[Option[String]] and
    (JsPath \ "children").lazyWrite(Writes.seq[DimensionsGraphNode](dimGraphNodeWrites)))(unlift(DimensionsGraphNode.unapply))

  implicit lazy val presentationNodeWrites: Writes[PresentationNode] = (
    (JsPath \ "concept").write[PresentationConcept] and
    (JsPath \ "children").lazyWrite(Writes.seq[PresentationNode](presentationNodeWrites)))(unlift(PresentationNode.unapply))

  implicit val presentationElrWrites: Writes[PresentationELR] = (
    (JsPath \ "elr").write[String] and
    (JsPath \ "roots").lazyWrite(Writes.seq[PresentationNode](presentationNodeWrites)))(unlift(PresentationELR.unapply))

  implicit lazy val labelWrites: Writes[Label] = (
    (JsPath \ "role").write[String] and
    (JsPath \ "language").write[String] and
    (JsPath \ "text").write[String])(unlift(Label.unapply))

  implicit val presentationConceptWrites: Writes[PresentationConcept] = (
    (JsPath \ "ename").write[EName] and
    (JsPath \ "labels").lazyWrite(Writes.seq[Label](labelWrites)))(unlift(PresentationConcept.unapply))

  implicit lazy val referencePartWrites = new Writes[ReferencePart] {
    def writes(part: ReferencePart): JsObject = {
      Json.obj(
        "namespace" -> part.partNamespace,
        "localName" -> part.partLocalName,
        "value" -> part.partValue)
    }
  }

  implicit lazy val referenceWrites = new Writes[Reference] {
    def writes(reference: Reference): JsObject = {
      Json.obj(
        "role" -> reference.role,
        "parts" -> JsArray(reference.parts.map(p => Json.toJson(p))))
    }
  }

  implicit val conceptElementDeclarationWrites: Writes[ConceptElementDeclaration] = (
    (JsPath \ "ename").write[EName] and
    (JsPath \ "elementDeclaration").write[String] and
    (JsPath \ "substitutionGroupHierarchy").write(Writes.seq[EName](enameWrites)) and
    (JsPath \ "typeHierarchy").write(Writes.seq[EName](enameWrites)))(unlift(ConceptElementDeclaration.unapply))

  implicit val conceptInfoWrites: Writes[ConceptInfo] = (
    (JsPath \ "ename").write[EName] and
    (JsPath \ "substitutionGroup").write[EName] and
    (JsPath \ "xsdType").write[EName])(unlift(ConceptInfo.unapply))

}