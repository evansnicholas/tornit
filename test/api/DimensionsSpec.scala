package api

import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.{ JsonMatchers, MatchResult }
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import model.tqa.TqaTaxonomyApi
import java.net.URI
import eu.cdevreeze.yaidom.core.EName

@RunWith(classOf[JUnitRunner])
class DimensionsSpec extends Specification with JsonMatchers {

  "The dimensional api" should {
    
    "return a dimensional graph for an item connected only to a hypercube" in new WithApplication{
      val entrypointUri = new URI("http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-kleine-rechtspersoon-gecomprimeerd-2013.xsd")
      
      val conceptEName = EName("http://www.nltaxonomie.nl/8.0/basis/sbr/items/nl-common-data", "FirstName")
      
      val requestUri =
        s"/dimensionsGraph?uri=${entrypointUri}&namespace=${conceptEName.namespaceUriOption.getOrElse("")}&localPart=${conceptEName.localPart}"

      val conceptDimensionTree = route(FakeRequest(GET, requestUri)).get

      status(conceptDimensionTree) must equalTo(OK)
      contentType(conceptDimensionTree) must beSome.which(_ == "application/json")
      val jsonArray = contentAsJson(conceptDimensionTree).as[JsArray]
      
      jsonArray.value must have size(1)
      
      val jsonString = contentAsString(conceptDimensionTree).trim 
      
      jsonString must /#(0) /("elr" -> "urn:kvk:linkrole:adimensional-table")
      jsonString must /#(0) /("graph") /("ename") /("localName" -> "FirstName")
      jsonString must /#(0) /("graph") /("children") /#(0) /("children") /#(0) /("ename") /("localName" -> "ValidationTable")
          
    }
    
    "return a dimensional graph for an item with one dimension in one ELR" in new WithApplication {
      val entrypointUri = new URI("http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-middelgrote-rechtspersoon-inrichtingsstukken-2013.xsd")  
      val conceptEName = EName("http://www.nltaxonomie.nl/8.0/basis/rj/items/rj-data", "PurchaseIntangibleAssets")
      
      val requestUri =
        s"/dimensionsGraph?uri=${entrypointUri}&namespace=${conceptEName.namespaceUriOption.getOrElse("")}&localPart=${conceptEName.localPart}"

      val conceptDimensionTree = route(FakeRequest(GET, requestUri)).get

      status(conceptDimensionTree) must equalTo(OK)
      contentType(conceptDimensionTree) must beSome.which(_ == "application/json")
      
      val jsonArray = contentAsJson(conceptDimensionTree).as[JsArray]
      
      jsonArray.value must have size(1)
      
      (jsonArray(0) \ "elr").as[JsString].value must beEqualTo("urn:kvk:linkrole:financial-statements-type-table")
      
      val dimensionsArray = (((jsonArray(0) \ "graph" \ "children")(0) \ "children")(0) \ "children").as[JsArray]
      
      dimensionsArray.value must have size(1)
      
      val expectedMembersForDimension = Map(
        "FinancialStatementsTypeAxis" -> Set("ConsolidatedMember", "SeparateMember") 
      )
       
      DimensionsSpec.validateConceptDimensions(dimensionsArray, expectedMembersForDimension)
 
    }
    
    "return a dimensional graph for an item with two dimensions in one ELR" in new WithApplication {
      val entrypointUri = new URI("http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-kleine-rechtspersoon-gecomprimeerd-2013.xsd")  
      val conceptEName = EName("http://www.nltaxonomie.nl/8.0/basis/venj/items/bw2-data", "BalanceSheetBeforeAfterAppropriationResults")
      
      val requestUri =
        s"/dimensionsGraph?uri=${entrypointUri}&namespace=${conceptEName.namespaceUriOption.getOrElse("")}&localPart=${conceptEName.localPart}"

      val conceptDimensionTree = route(FakeRequest(GET, requestUri)).get

      status(conceptDimensionTree) must equalTo(OK)
      contentType(conceptDimensionTree) must beSome.which(_ == "application/json")
      
      val jsonArray = contentAsJson(conceptDimensionTree).as[JsArray]
      
      jsonArray.value must have size(1)
      
      (jsonArray(0) \ "elr").as[JsString].value must beEqualTo("urn:kvk:linkrole:financial-statements-type-table")
      
      val dimensionsArray = (((jsonArray(0) \ "graph" \ "children")(0) \ "children")(0) \ "children").as[JsArray]
      
      dimensionsArray.value must have size(2)
      
      val expectedMembersForDimension = Map(
        "FinancialStatementsTypeAxis" -> Set("ConsolidatedMember", "SeparateMember"),
        "BasisOfPreparationAxis" -> Set("CommercialMember", "FiscalMember")
      )
       
      DimensionsSpec.validateConceptDimensions(dimensionsArray, expectedMembersForDimension)
 
    }
    
    "return a dimensional graph for an item with multiple dimensions in two ELRs" in new WithApplication {
      val entrypointUri = new URI("http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-kleine-rechtspersoon-gecomprimeerd-2013.xsd")  
      val conceptEName = EName("http://www.nltaxonomie.nl/8.0/basis/venj/items/bw2-data", "Equity")
      
      val requestUri =
        s"/dimensionsGraph?uri=${entrypointUri}&namespace=${conceptEName.namespaceUriOption.getOrElse("")}&localPart=${conceptEName.localPart}"

      val conceptDimensionTree = route(FakeRequest(GET, requestUri)).get

      status(conceptDimensionTree) must equalTo(OK)
      contentType(conceptDimensionTree) must beSome.which(_ == "application/json")
      
      val jsonArray = contentAsJson(conceptDimensionTree).as[JsArray]
      
      jsonArray.value must have size(2)
      
      (jsonArray(0) \ "elr").as[JsString].value must beEqualTo("urn:kvk:linkrole:equity-movement-schedule-table")
      
      val dimensionsArray = (((jsonArray(0) \ "graph" \ "children")(0) \ "children")(0) \ "children").as[JsArray]
      
      dimensionsArray.value must have size(3)
      
      val expectedMembersForDimension = Map(
        "FinancialStatementsTypeAxis" -> Set("ConsolidatedMember", "SeparateMember"),
        "EquityComponentsAxis" -> Set("EquityGroupMember", "EquityMember", "FiscalReservesOtherMember", "LegalReservesMember", "LegalStatutoryReservesMember", "NoncontrollingInterestMember", "ReinvestmentReserveMember", "ReservesOtherMember", "ResultForTheYearMember", "RetainedEarningsMember", "RevaluationReserveMember", "ShareCapitalMember", "SharePremiumMember", "StatutoryReservesMember"),
        "BasisOfPreparationAxis" -> Set("CommercialMember", "FiscalMember")
      )
       
      DimensionsSpec.validateConceptDimensions(dimensionsArray, expectedMembersForDimension)
      
      (jsonArray(1) \ "elr").as[JsString].value must beEqualTo("urn:kvk:linkrole:financial-statements-type-table")
      
      val dimensionsArray2 = (((jsonArray(1) \ "graph" \ "children")(0) \ "children")(0) \ "children").as[JsArray]
      
      dimensionsArray2.value must have size(2)
      
      val expectedMembersForDimension2 = Map(
        "FinancialStatementsTypeAxis" -> Set("ConsolidatedMember", "SeparateMember"),
        "BasisOfPreparationAxis" -> Set("CommercialMember", "FiscalMember")
      )
       
      DimensionsSpec.validateConceptDimensions(dimensionsArray2, expectedMembersForDimension2)
 
    }
    
  }  
}

object DimensionsSpec extends Specification with JsonMatchers {
  //Note this method has NTA knowledge and knows how deep the different parts of the dimensional tree are nested
  def validateConceptDimensions(dimensionsTree: JsArray, membersForDimension: Map[String, Set[String]]): Seq[MatchResult[Set[String]]] = { 
    dimensionsTree.value map { dimension =>
      val dimLocalName = dimension \ "ename" \ "localName"
      val actualMembers = ((dimension \ "children")(0) \ "children").as[JsArray].value map { mem => (mem \ "ename" \ "localName").as[JsString].value }
      val expectedMembers = membersForDimension(dimLocalName.as[JsString].value)
      
      actualMembers.toSet must beEqualTo(expectedMembers)
    } 
  }
}