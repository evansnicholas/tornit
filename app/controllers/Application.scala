package controllers

import play.api._
import play.api.mvc._
import java.net.URI
import play.api.libs.json._
import model.{ Entrypoint, Taxonomies, DtsGraph, Concept }
import play.api.libs.functional.syntax._

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
    
  def computeDtsGraph(entrypointPath: String) = Action {
    val json = Json.toJson(Taxonomies.computeDtsGraph(entrypointPath))
    Ok(json)
  }
  
  implicit lazy val dtsGraphWrites: Writes[DtsGraph] = (
    (JsPath \ "uri").write[String] and
    (JsPath \ "children").lazyWrite(Writes.seq[DtsGraph](dtsGraphWrites))
  )(unlift(DtsGraph.unapply))

}