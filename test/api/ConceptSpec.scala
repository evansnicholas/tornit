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
import ReadUtils._

@RunWith(classOf[JUnitRunner])
class ConceptSpec extends Specification {

  import ConceptSpec._
  
  "The concept api" should {
    
    "return a list of all concepts in the taxonomy that match a query string" in new WithApplication {
      val conceptQuery = "equ"
      val requestUri =
        s"/concepts?uri=http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-middelgrote-rechtspersoon-publicatiestukken-model-b-j-direct-2013.xsd&concept=$conceptQuery"

      val conceptList = route(FakeRequest(GET, requestUri)).get
      
      val json = contentAsJson(conceptList).as[JsArray]
      
      val conceptENames = json.value map { value =>
        val namespace = (value \ "namespace").as[String]
        val localPart = (value \ "localPart").as[String]
        EName(namespace, localPart)
      }
      
      val conceptSelection = conceptENames.filter(_.localPart.contains("Equity"))
      conceptSelection must have size(60)
      
      
      val equityEName = EName(Bw2DataNamespace, "Equity")
      conceptENames must contain(equityEName)
      val equityTransfers = EName(Bw2DataNamespace, "EquityTransfers")
      conceptENames must contain(equityTransfers)
      val propertyPlantEquipment = EName(Bw2DataNamespace, "PropertyPlantEquipment")
      conceptENames must contain(propertyPlantEquipment)
    }
    
    "return the information related to a concept in the taxonomy" in new WithApplication {
      val conceptParams = s"conceptNamespace=$Bw2DataNamespace&conceptLocalName=Equity"
      val requestUri =
        s"/concept?entrypointUri=http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-middelgrote-rechtspersoon-publicatiestukken-model-b-j-direct-2013.xsd&$conceptParams"

      val conceptDec = route(FakeRequest(GET, requestUri)).get
      
      val json = contentAsJson(conceptDec)
            
      val ced = json.as[ConceptElementDeclaration]
      ced.ename must beEqualTo(EName(Bw2DataNamespace, "Equity"))
      ced.elementDeclaration must contain("""substitutionGroup="xbrli:item"""")
      ced.substitutionGroupHierarchy must have size(1)
      ced.typeHierarchy must have size(6)
      
    }
  } 
}

object ConceptSpec {
  
  implicit val conceptElementDeclarationReads: Reads[ConceptElementDeclaration] = (
    (JsPath \ "ename").read[EName] and
    (JsPath \ "elementDeclaration").read[String] and 
    (JsPath \ "substitutionGroupHierarchy").read(Reads.seq[EName](enameReads)) and
    (JsPath \ "typeHierarchy").read(Reads.seq[EName](enameReads))
   )(ConceptElementDeclaration.apply _)
}