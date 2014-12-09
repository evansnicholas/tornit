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
import nl.ebpi.tqa.model.relationship.ConceptLabelRelationship

@RunWith(classOf[JUnitRunner])
class ConceptLabelSpec extends Specification {

  "The concept label api should" should {
    
    "return the concept labels for a concept that has at least one label" in new WithApplication{
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

    "return an empty array of concept labels for a non-existing concept" in new WithApplication{
      val entrypointUri = TqaTaxonomyApi.dtsCollection.allEntrypointUris.head
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/labels?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptLabelsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptLabelsResult) must equalTo(OK)
      contentType(conceptLabelsResult) must beSome.which(_ == "application/json")
      contentAsString(conceptLabelsResult).trim must contain ("[]")
    }

    "return 400 on concept labels request for a non-existing entrypoint" in new WithApplication{
      val entrypointUri = new java.net.URI("")
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/labels?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptLabelsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptLabelsResult) must not(equalTo(OK))
      contentType(conceptLabelsResult) must beSome.which(_ == "application/json")
    }
  }
  
  
}