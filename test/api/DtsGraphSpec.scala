package api

import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.{ JsonMatchers, MatchResult }
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.annotation.tailrec
import model.DtsGraph

@RunWith(classOf[JUnitRunner])
class DtsGraphSpec extends Specification with JsonMatchers {

  import DtsGraphSpec._
  
  "The dtsGraph api" should {
    
    "return the dts graph for an entrypoint" in new WithApplication {
      val requestUri =
        s"/dtsGraph?entrypointUri=http://www.nltaxonomie.nl/7.0/report/bd/entrypoints/rpt-bd-omzetbelasting-2013.xsd"

      val entrypointsResult = route(FakeRequest(GET, requestUri)).get
      
      val json = contentAsJson(entrypointsResult)
      
      (json \ "uri").as[JsString].value must beEqualTo("http://www.nltaxonomie.nl/7.0/report/bd/entrypoints/rpt-bd-omzetbelasting-2013.xsd")
     
      val dtsGraph = json.validate[DtsGraph].get
      val allUris = DtsGraphSpec.flatten(dtsGraph)
      println(allUris.contains("http://www.nltaxonomie.nl/7.0/basis/bd/linkroles/bd-linkroles-domains.xsd"))
      
      allUris must have size(60)
      allUris must contain("http://www.nltaxonomie.nl/7.0/basis/bd/linkroles/bd-linkroles-domains.xsd")
      allUris must contain("http://www.nltaxonomie.nl/7.0/basis/sbr/items/nl-common-data-documentation-lab-en.xml")
      allUris must contain("http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd")
      allUris must contain("http://www.nltaxonomie.nl/7.0/domein/bd/tuples/bd-alg-tuples.xsd")
    }
    
  }
  
}

object DtsGraphSpec {
  
  implicit lazy val dtsGraphReads: Reads[DtsGraph] = (
    (JsPath \ "uri").read[String] and
    (JsPath \ "children").lazyRead(Reads.seq[DtsGraph](dtsGraphReads)))(DtsGraph.apply _)

  def flatten(dtsGraph: DtsGraph): List[String] = {
    @tailrec
    def go(acc: List[String], dtsGraphs: List[DtsGraph]): List[String] = {
      if (dtsGraphs.isEmpty) acc
      else {
        val currentGraph = dtsGraphs.head
        go(acc :+ currentGraph.uri, dtsGraphs.tail ++ currentGraph.children)
      }
    }
    go(List.empty[String], List(dtsGraph))
  }
}