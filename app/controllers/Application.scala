package controllers

import play.api._
import play.api.mvc._
import java.net.URI
import scala.util._
import play.api.libs.json._
import model._
import play.api.libs.functional.syntax._
import eu.cdevreeze.yaidom.core.EName

object Application extends Controller {

  def main = Action {
    Ok(views.html.main("Taxoscope"))
  }
  
  def loadEntrypoint(entrypointPath: String) = Action {
    Taxonomies.loadEntrypoint(entrypointPath)
    Ok("")
  }
  
  def listEntrypoints(query: String) = Action {
    val json = Json.toJson(Taxonomies.queryEntrypointUris(query))
    Ok(json)
  }
  
  implicit val entrypointWrites: Writes[Entrypoint] = 
    ((JsPath \ "uri").write[String].contramap((f: Entrypoint) => f.uri)) 
    
  def listConcepts(entrypointPath: String, query: String) = Action {
    val json = Json.toJson(Taxonomies.listConcepts(entrypointPath, query))
    Ok(json)
  } 
  
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
    val json = Json.toJson(Taxonomies.findDimensionalGraphs(entrypointPath, namespace, localPart))
    Ok(json)
  }
  
  implicit val dimGraphWrites: Writes[DimensionsGraph] = (
    (JsPath \ "elr").write[String] and
    (JsPath \ "graph").write[DimensionsGraphNode]
  )(unlift(DimensionsGraph.unapply))
  
  implicit lazy val dimGraphNodeWrites: Writes[DimensionsGraphNode] = (
    (JsPath \ "ename").write[EName] and
    (JsPath \ "children").lazyWrite(Writes.seq[DimensionsGraphNode](dimGraphNodeWrites))
  )(unlift(DimensionsGraphNode.unapply))
  
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
    (JsPath \ "ename").write[EName] and 
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
  
  implicit val conceptElementDeclarationWrites: Writes[ConceptElementDeclaration] = (
    (JsPath \ "ename").write[EName] and 
    (JsPath \ "elementDeclaration").write[String] and
    (JsPath \ "substitutionGroupHierarchy").write(Writes.seq[EName](enameWrites)) and
    (JsPath \ "typeHierarchy").write(Writes.seq[EName](enameWrites))
  )(unlift(ConceptElementDeclaration.unapply))

  implicit val conceptInfoWrites: Writes[ConceptInfo] = (
    (JsPath \ "ename").write[EName] and 
    (JsPath \ "substitutionGroup").write[EName] and 
    (JsPath \ "xsdType").write[EName]
  )(unlift(ConceptInfo.unapply))
  
  def showTaxonomyDocument(uri: String, docUri: String) = Action {
    val json = Taxonomies.showTaxonomyDocument(uri, docUri)
    Ok(json)
  }

  def findConceptLabels(entrypointPath: String, conceptNamespace: String, conceptLocalName: String) = Action {
    Try {
      Json.toJson(Taxonomies.findConceptLabels(entrypointPath, conceptNamespace, conceptLocalName))
    } match {
      case Success(json) => Ok(json)
      case Failure(t) => BadRequest(Json.obj("error" -> t.toString))
    }
  }

  def findConceptReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String) = Action {
    Try {
      Json.toJson(Taxonomies.findConceptReferences(entrypointPath, conceptNamespace, conceptLocalName))
    } match {
      case Success(json) => Ok(json)
      case Failure(t) => BadRequest(Json.obj("error" -> t.toString))
    }
  }
  
  def findConceptElementDeclaration(entrypointPath: String, conceptNamespace: String, conceptLocalName: String) = Action {
    Try {
      Json.toJson(Taxonomies.findConceptElementDeclaration(entrypointPath, conceptNamespace, conceptLocalName))
    } match {
      case Success(json) => Ok(json)
      case Failure(t) => BadRequest(Json.obj("error" -> t.toString))
    }
  }
  
  def findConceptInfo(entrypointPath: String, conceptNamespace: String, conceptLocalName: String) = Action {
    val json = Json.toJson(Taxonomies.findConceptInfo(entrypointPath, conceptNamespace, conceptLocalName))
    Ok(json)
  }
}
