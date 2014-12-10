package api

import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.{ JsonMatchers, MatchResult }
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import eu.cdevreeze.yaidom.core.EName
import play.api.libs.functional.syntax._
import model._

@RunWith(classOf[JUnitRunner])
class PresentationSpec extends Specification {

  import PresentationSpec._
  
  "The presentation spec" should {
    
    "return a list of presentation elrs for an entrypoint" in new WithApplication {
      val entrypoint = "http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-grote-rechtspersoon-model-b-e-direct-2013.xsd"
      val requestUri =
        s"/presentationElrs?uri=$entrypoint"

      val elrsList = route(FakeRequest(GET, requestUri)).get
      
      val elrs = contentAsJson(elrsList).as[JsArray].value.map(_.as[JsString].value)
      println(elrs)
      elrs must have size(9)
      elrs must contain("urn:venj:linkrole:income-statement")
      elrs must contain("urn:kvk:linkrole:information-required-for-filing")
      elrs must contain("urn:rj:linkrole:comprehensive-income-statement")
      
    }
    
    "return the presentation tree for an elr" in new WithApplication {
      val entrypoint = "http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-grote-rechtspersoon-model-b-e-direct-2013.xsd"
      val requestUri =
        s"/presentationTree?uri=$entrypoint&elr=urn:kvk:linkrole:information-required-for-filing"

      val elrTreeFuture = route(FakeRequest(GET, requestUri)).get
      
      val elrTree = contentAsJson(elrTreeFuture)
      (elrTree \ "elr").as[JsString].value must beEqualTo("urn:kvk:linkrole:information-required-for-filing")
      
      val rootNodes = (elrTree \ "roots").as[JsArray].value map { _.as[PresentationNode] }
      rootNodes must have size(1)
      
      val rootNode = rootNodes.head
      rootNode.concept.ename must beEqualTo("InformationRequiredForFilingTitle")
           
      val rootNodeChildren = rootNode.children
      rootNode.children must have size(3)
      rootNodeChildren(0).concept.ename must beEqualTo("LegalSizeCriteriaClassification")
      rootNodeChildren(1).concept.ename must beEqualTo("SbiBusinessCode")
      rootNodeChildren(2).concept.ename must beEqualTo("ContactForDocumentPresentation")
      
      rootNodeChildren(0).children must beEmpty
      rootNodeChildren(1).children must beEmpty
      rootNodeChildren(2).children must have size(6)
      rootNodeChildren(2).children(1).concept.ename must beEqualTo("FirstName")
      
    }
  }
  
}

object PresentationSpec {
  implicit val labelReads: Reads[Label] = (
    (JsPath \ "role").read[String] and 
    (JsPath \ "language").read[String] and
    (JsPath \ "text").read[String])(Label.apply _)
  
  implicit val presentationConceptReads: Reads[PresentationConcept] = (
    (JsPath \ "ename").read[String] and
    (JsPath \ "labels").read(Reads.seq[Label](labelReads))
  )(PresentationConcept.apply _)
  
  implicit lazy val presentationNodeReads: Reads[PresentationNode] = (
    (JsPath \ "concept").read[PresentationConcept] and
    (JsPath \ "children").lazyRead(Reads.seq[PresentationNode](presentationNodeReads)))(PresentationNode.apply _)
      
}