package controllers

import play.api._
import play.api.mvc._
import java.net.URI
import play.api.libs.json._
import model._
import play.api.libs.functional.syntax._
import eu.cdevreeze.yaidom.EName

object Application extends Controller {

  def main = Action {
    Ok(views.html.main("Taxoscope"))
  }
  
  def listEntrypoints(query: String) = Action {
    val json = Json.toJson(Taxonomies.listEntrypoints(query))
    Ok(json)
  }
  
  implicit val entrypointWrites: Writes[Entrypoint] = 
    ((JsPath \ "uri").write[String].contramap((f: Entrypoint) => f.uri)) 
    
  def listConcepts(entrypointPath: String, query: String) = Action {
    val json = Json.toJson(Taxonomies.listConcepts(entrypointPath, query))
    Ok(json)
  } 
  
  implicit val conceptWrites: Writes[Concept] = (
    (JsPath \ "namespace").write[String] and
    (JsPath \ "localPart").write[String] and 
    (JsPath \ "conceptType").write[String]
  )(unlift(Concept.unapply))
    
  def listPresentationElrs(entrypointPath: String) = Action {
    val json = Json.toJson(Taxonomies.listPresentationElrs(entrypointPath))
    Ok(json)
  }
  
  def computeDtsGraph(entrypointPath: String) = Action {
    val json = Json.toJson(Taxonomies.computeDtsGraph(entrypointPath))
    Ok(json)
  }
  
  implicit lazy val dtsGraphWrites: Writes[DtsGraph] = (
    (JsPath \ "uri").write[String] and
    (JsPath \ "children").lazyWrite(Writes.seq[DtsGraph](dtsGraphWrites))
  )(unlift(DtsGraph.unapply))
  
  def computeDimensionalGraphs(entrypointPath: String, namespace: String, localPart: String) = Action {
    val json = Json.toJson(Taxonomies.computeDimensionalGraphs(entrypointPath, namespace, localPart))
    Ok(json)
  }
  
  implicit val dimGraphWrites: Writes[DimensionsGraph] = (
    (JsPath \ "elr").write[String] and
    (JsPath \ "graph").write[DimensionalGraphNode]
  )(unlift(DimensionsGraph.unapply))
  
  implicit lazy val dimGraphNodeWrites: Writes[DimensionalGraphNode] = (
    (JsPath \ "namespace").write[String] and
    (JsPath \ "localPart").write[String] and
    (JsPath \ "children").lazyWrite(Writes.seq[DimensionalGraphNode](dimGraphNodeWrites))
  )(unlift(DimensionalGraphNode.unapply))
  
  def computePresentationTree(entrypointPath: String, elr: String) = Action {
    val json = Json.toJson(Taxonomies.computePresentationTree(entrypointPath, elr))
    Ok(json)
  }
  
  implicit lazy val presentationNodeWrites: Writes[PresentationNode] = (
    (JsPath \ "concept").write[PresentationConcept] and
    (JsPath \ "children").lazyWrite(Writes.seq[PresentationNode](presentationNodeWrites))
  )(unlift(PresentationNode.unapply))
  
  implicit val presentationElrWrites: Writes[PresentationELR] = (
    (JsPath \ "elr").write[String] and
    (JsPath \ "roots").lazyWrite(Writes.seq[PresentationNode](presentationNodeWrites))
  )(unlift(PresentationELR.unapply))
  
  implicit lazy val labelWrites: Writes[Label] = ( 
    (JsPath \ "role").write[String] and 
    (JsPath \ "language").write[String] and 
    (JsPath \ "text").write[String] 
  )(unlift(Label.unapply))
  
  implicit val presentationConceptWrites: Writes[PresentationConcept] = (
    (JsPath \ "ename").write[String] and 
    (JsPath \ "labels").lazyWrite(Writes.seq[Label](labelWrites))
  )(unlift(PresentationConcept.unapply))

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

  implicit lazy val abbreviatedReferencePartWrites = new Writes[ReferenceAbbreviatedPart] {
    def writes(part: ReferenceAbbreviatedPart): JsObject = {
      Json.obj(
        "localName" -> part.partLocalName,
        "value" -> part.partValue)
    }
  }

  implicit lazy val abbreviatedReferenceWrites = new Writes[AbbreviatedReference] {
    def writes(reference: AbbreviatedReference): JsObject = {
      Json.obj(
        "role" -> reference.role,
        "parts" -> JsArray(reference.parts.map(p => Json.toJson(p))))
    }
  }

  def showTaxonomyDocument(uri: String, docUri: String) = Action {
    val json = Taxonomies.showTaxonomyDocument(uri, docUri)
    Ok(json)
  }

  def findConceptLabels(entrypointPath: String, conceptNamespace: String, conceptLocalName: String) = Action {
    val json = Json.toJson(Taxonomies.findConceptLabels(entrypointPath, conceptNamespace, conceptLocalName))
    Ok(json)
  }

  def findConceptReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String) = Action {
    val json = Json.toJson(Taxonomies.findConceptReferences(entrypointPath, conceptNamespace, conceptLocalName))
    Ok(json)
  }

  def findConceptAbbreviatedReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String) = Action {
    val json = Json.toJson(Taxonomies.findConceptAbbreviatedReferences(entrypointPath, conceptNamespace, conceptLocalName))
    Ok(json)
  }
}
