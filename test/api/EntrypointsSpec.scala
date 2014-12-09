package api

import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.{ JsonMatchers, MatchResult }
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

@RunWith(classOf[JUnitRunner])
class EntrypointsSpec extends Specification with JsonMatchers {

  "The entrypoint api" should {
    
    "return a list of all entrypoints that match the 8.0/reports/kvk query string" in new WithApplication {
      val queryString = "8.0/report/kvk" 
      val requestUri =
        s"/entrypoints?uri=$queryString"

      val entrypointsResult = route(FakeRequest(GET, requestUri)).get
      
      val jsonArray = contentAsJson(entrypointsResult).as[JsArray]
      
      jsonArray.value must have size(47)
      val entrypoints = jsonArray.value map { v => (v \"uri").as[JsString].value } 
      
      entrypoints must containPattern("http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/.*.xsd")
    }
    
    "return the empty list for a query string that doesn't match any entrypoints" in new WithApplication {
      val queryString = "/us-gaap/" 
      val requestUri =
        s"/entrypoints?uri=$queryString"

      val entrypointsResult = route(FakeRequest(GET, requestUri)).get
      
      val jsonArray = contentAsJson(entrypointsResult).as[JsArray]
      
      jsonArray.value must have size(0)
    }
    
    "return 200 response when asked to load an existing entrypoint" in new WithApplication {
      val queryString = "/us-gaap/" 
      val requestUri =
        s"/entrypoint?entrypointUri=http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-grote-rechtspersoon-model-a-f-indirect-2013.xsd"
        
      val entrypointResult = route(FakeRequest(GET, requestUri)).get
      status(entrypointResult) must equalTo(OK)

    }
  }
  
}