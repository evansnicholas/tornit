package controllers

import play.api._
import play.api.mvc._
import java.net.URI
import play.api.libs.json._
import model.{ Entrypoint, Taxonomies }
import play.api.libs.functional.syntax._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def listEntrypoints = Action {
    val json = Json.toJson(Taxonomies.listEntrypoints)
    Ok(json)
  }
  
  implicit val entrypointWrites: Writes[Entrypoint] = 
    ((JsPath \ "uri").write[String].contramap((f: Entrypoint) => f.uri)) 

}