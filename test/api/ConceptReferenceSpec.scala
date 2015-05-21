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

import nl.ebpi.tqa.model.relationship.ConceptReferenceRelationship

@RunWith(classOf[JUnitRunner])
class ConceptReferenceSpec extends Specification {

  "The concept reference api" should {
    
    "return the concept references for a concept that has at least one reference" in new WithApplication{
      val entrypointUri = TqaTaxonomyApi.dtsCollection.allEntrypointUris.head
      
      val dts = TqaTaxonomyApi.dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
      val dtsConceptReferences = dts.findStandardRelationships(scala.reflect.classTag[ConceptReferenceRelationship])
      
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

    "return an empty array of concept references for a non-existing concept" in new WithApplication{
      val entrypointUri = TqaTaxonomyApi.dtsCollection.allEntrypointUris.head
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/references?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptRefsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptRefsResult) must equalTo(OK)
      contentType(conceptRefsResult) must beSome.which(_ == "application/json")
      contentAsString(conceptRefsResult).trim must contain ("[]")
    }

    "return 400 on concept references request for a non-existing entrypoint" in new WithApplication{
      val entrypointUri = new java.net.URI("")
      
      val conceptEName = EName("http://blah", "blah")
      
      val requestUri =
        s"/concept/references?entrypointUri=${entrypointUri}&conceptNamespace=${conceptEName.namespaceUriOption.getOrElse("")}&conceptLocalName=${conceptEName.localPart}"

      val conceptRefsResult = route(FakeRequest(GET, requestUri)).get

      status(conceptRefsResult) must not(equalTo(OK))
      contentType(conceptRefsResult) must beSome.which(_ == "application/json")
    }
    
  }
  
}