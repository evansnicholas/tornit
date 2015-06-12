package controllers

import play.api._
import play.api.mvc._
import java.net.URI
import scala.util._
import play.api.libs.json._
import model._
import play.api.libs.functional.syntax._
import eu.cdevreeze.yaidom.core.EName

import Writers._

object Application extends Controller {

  def main = Action {
    Ok(views.html.main("Tornit"))
  }
  
  def loadEntrypoint(entrypointPath: String) = Action {
    Taxonomies.loadEntrypoint(entrypointPath)
    Ok("")
  }
  
  def listEntrypoints(query: String) = Action {
    val json = Json.toJson(Taxonomies.queryEntrypointUris(query))
    Ok(json)
  } 
    
  def listConcepts(entrypointPath: String, query: String) = Action {
    val json = Json.toJson(Taxonomies.listConcepts(entrypointPath, query))
    Ok(json)
  }  
    
  def listPresentationElrs(entrypointPath: String) = Action {
    val json = Json.toJson(Taxonomies.listPresentationElrs(entrypointPath))
    Ok(json)
  }
  
  def computeDtsGraph(entrypointPath: String) = Action {
    val json = Json.toJson(Taxonomies.computeDtsGraph(entrypointPath))
    Ok(json)
  }
  
  
  
  def computeDimensionalGraphs(entrypointPath: String, namespace: String, localPart: String) = Action {
    val json = Json.toJson(Taxonomies.findDimensionalGraphs(entrypointPath, namespace, localPart))
    Ok(json)
  }
  
  def computePresentationTree(entrypointPath: String, elr: String) = Action {
    val json = Json.toJson(Taxonomies.computePresentationTree(entrypointPath, elr))
    Ok(json)
  }
  
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
