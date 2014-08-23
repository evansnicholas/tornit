package controllers

import play.api._
import play.api.mvc._
import java.net.URI
import play.api.libs.json._
import model.{ Entrypoint, Taxonomies, DtsGraph }
import play.api.libs.functional.syntax._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def dtsGraph = Action {
    Ok(views.html.dtsgraph())
  }
  
  def presentationViewer = Action {
    Ok(views.html.presentationviewer())
  }
  
  def dimensionViewer = Action {
    Ok(views.html.dimensionviewer())
  }
  
  def listEntrypoints = Action {
    val json = Json.toJson(Taxonomies.listEntrypoints)
    Ok(json)
  }
  
  implicit val entrypointWrites: Writes[Entrypoint] = 
    ((JsPath \ "uri").write[String].contramap((f: Entrypoint) => f.uri)) 
    
  def computeDtsGraph(entrypointPath: String) = Action {
    val json = Json.toJson(Taxonomies.computeDtsGraph(entrypointPath))
    Ok(json)
  }
  
  implicit lazy val dtsGraphWrites: Writes[DtsGraph] = (
    (JsPath \ "uri").write[String] and
    (JsPath \ "children").lazyWrite(Writes.seq[DtsGraph](dtsGraphWrites))
  )(unlift(DtsGraph.unapply))

}