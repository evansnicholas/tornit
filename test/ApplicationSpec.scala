import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.JsonMatchers
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import model._
import nl.ebpi.tqa.model.relationship.ConceptLabelRelationship
import eu.cdevreeze.yaidom.core.EName
import nl.ebpi.tqa.model.relationship.ConceptReferenceRelationship
import model.tqa.TqaTaxonomyApi
import java.net.URI

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with JsonMatchers {

  "Application" should {

    /*
    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }
    */

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Taxoscope")
    }

    "render the concept labels for a concept that has at least one label" in new WithApplication{
      val entrypointUri = TqaTaxonomyApi.dtsCollection.allEntrypointUris.head
      
      val dts = TqaTaxonomyApi.dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
      val dtsConceptLabels = dts.findRelationships[ConceptLabelRelationship]
      
      val conceptEName = dtsConceptLabels.head.sourceConceptEName
      
      val requestUri =
        s"/concept/labels?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptLabelsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptLabelsResult) must equalTo(OK)
      contentType(conceptLabelsResult) must beSome.which(_ == "application/json")
      contentAsString(conceptLabelsResult) must contain (""""role"""")
      contentAsString(conceptLabelsResult) must contain (""""language"""")
      contentAsString(conceptLabelsResult) must contain (""""text"""")
    }

    "render an empty array of concept labels for a non-existing concept" in new WithApplication{
      val entrypointUri = TqaTaxonomyApi.dtsCollection.allEntrypointUris.head
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/labels?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptLabelsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptLabelsResult) must equalTo(OK)
      contentType(conceptLabelsResult) must beSome.which(_ == "application/json")
      contentAsString(conceptLabelsResult).trim must contain ("[]")
    }

    "send 400 on concept labels request for a non-existing entrypoint" in new WithApplication{
      val entrypointUri = new java.net.URI("")
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/labels?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptLabelsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptLabelsResult) must not(equalTo(OK))
      contentType(conceptLabelsResult) must beSome.which(_ == "application/json")
    }

    "render the concept references for a concept that has at least one reference" in new WithApplication{
      val entrypointUri = TqaTaxonomyApi.dtsCollection.allEntrypointUris.head
      
      val dts = TqaTaxonomyApi.dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
      val dtsConceptReferences = dts.findRelationships[ConceptReferenceRelationship]
      
      val conceptEName = dtsConceptReferences.head.sourceConceptEName
      
      val requestUri =
        s"/concept/references?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptRefsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptRefsResult) must equalTo(OK)
      contentType(conceptRefsResult) must beSome.which(_ == "application/json")
      contentAsString(conceptRefsResult) must contain (""""role"""")
      contentAsString(conceptRefsResult) must contain (""""parts"""")
      contentAsString(conceptRefsResult) must contain (""""namespace"""")
      contentAsString(conceptRefsResult) must contain (""""value"""")
    }

    "render an empty array of concept references for a non-existing concept" in new WithApplication{
      val entrypointUri = TqaTaxonomyApi.dtsCollection.allEntrypointUris.head
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/references?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptRefsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptRefsResult) must equalTo(OK)
      contentType(conceptRefsResult) must beSome.which(_ == "application/json")
      contentAsString(conceptRefsResult).trim must contain ("[]")
    }

    "send 400 on concept references request for a non-existing entrypoint" in new WithApplication{
      val entrypointUri = new java.net.URI("")
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/references?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptRefsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptRefsResult) must not(equalTo(OK))
      contentType(conceptRefsResult) must beSome.which(_ == "application/json")
    }
    
    "render a dimensional graph for an item connected only to a hypercube" in new WithApplication{
      val entrypointUri = new URI("http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-kleine-rechtspersoon-gecomprimeerd-2013.xsd")
      
      val conceptEName = EName("http://www.nltaxonomie.nl/8.0/basis/sbr/items/nl-common-data", "FirstName")
      
      val requestUri =
        s"/dimensionsGraph?uri=${entrypointUri}&namespace=${conceptEName.namespaceUriOption.getOrElse("")}&localPart=${conceptEName.localPart}"

      val conceptDimensionTree = route(FakeRequest(GET, requestUri)).get

      status(conceptDimensionTree) must equalTo(OK)
      contentType(conceptDimensionTree) must beSome.which(_ == "application/json")
      val json = contentAsJson(conceptDimensionTree).as[JsArray]
      
      json.value must have size(1)
      
      
      //\("children").as[JsArray](0) \("children").value must have size(0)
      
      val jsonString = contentAsString(conceptDimensionTree).trim 
      
      jsonString must /#(0) /("elr" -> "urn:kvk:linkrole:adimensional-table")
      jsonString must /#(0) /("graph") /("ename") /("localName" -> "FirstName")
      jsonString must /#(0) /("graph") /("children") /#(0) /("children") /#(0) /("ename") /("localName" -> "ValidationTable")
      

      
    }
  }
}
