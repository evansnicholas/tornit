package api

import play.api.test.Helpers._
import play.api.libs.json._
import eu.cdevreeze.yaidom.core.EName
import play.api.libs.functional.syntax._

object ReadUtils {

  implicit val enameReads: Reads[EName] = (
      (JsPath \ "namespace").read[String] and 
      (JsPath \ "localName").read[String]
    )(EName.apply _: (String, String) => EName)
  
}

