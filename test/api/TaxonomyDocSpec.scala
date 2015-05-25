package api

import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.{ JsonMatchers, MatchResult }
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.functional.syntax._


@RunWith(classOf[JUnitRunner])
class TaxonomyDocSpec extends Specification {

  "The taxonomy doc api" should {
    "return the taxonomy document" in new WithApplication {
      val entrypointUri = "http://www.nltaxonomie.nl/8.0/report/kvk/entrypoints/algemeen/kvk-rpt-grote-rechtspersoon-model-a-f-indirect-2013.xsd"
      val docUri = "http://www.nltaxonomie.nl/8.0/basis/kvk/linkroles/kvk-share-capital-members-def.xml"
      val requestUri =
        s"/taxoDoc?entrypointUri=$entrypointUri&docUri=$docUri"

      val entrypointsResult = route(FakeRequest(GET, requestUri)).get
      
      val docString = contentAsString(entrypointsResult)

      val expected = """ <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                       |<link:linkbase xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink">
                       |  <link:roleRef xlink:type="simple" xlink:href="kvk-linkroles-domains.xsd#kvk-dlrd_ShareCapitalDomain" roleURI="urn:kvk:linkrole:share-capital-domain"/>
                       |  <link:arcroleRef xlink:type="simple" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#domain-member" arcroleURI="http://xbrl.org/int/dim/arcrole/domain-member"/>
                       |  <link:definitionLink xlink:type="extended" xlink:role="urn:kvk:linkrole:share-capital-domain">
                       |    <link:loc xlink:type="locator" xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_ShareCapitalComponentDomain" xlink:label="bw2-dm-eqty_ShareCapitalComponentDomain_loc"/>
                       |    <link:loc xlink:type="locator" xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_OrdinarySharesMember" xlink:label="bw2-dm-eqty_OrdinarySharesMember_loc"/>
                       |    <link:loc xlink:type="locator" xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_PreferredSharesMember" xlink:label="bw2-dm-eqty_PreferredSharesMember_loc"/>
                       |    <link:loc xlink:type="locator" xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_PrioritySharesMember" xlink:label="bw2-dm-eqty_PrioritySharesMember_loc"/>
                       |    <link:loc xlink:type="locator" xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_TreasurySharesMember" xlink:label="bw2-dm-eqty_TreasurySharesMember_loc"/>
                       |    <link:definitionArc xlink:type="arc" xlink:to="bw2-dm-eqty_OrdinarySharesMember_loc" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" order="1"/>
                       |    <link:definitionArc xlink:type="arc" xlink:to="bw2-dm-eqty_PreferredSharesMember_loc" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" order="2"/>
                       |    <link:definitionArc xlink:type="arc" xlink:to="bw2-dm-eqty_PrioritySharesMember_loc" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" order="3"/>
                       |    <link:definitionArc xlink:type="arc" xlink:to="bw2-dm-eqty_TreasurySharesMember_loc" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" order="4"/>
                       |  </link:definitionLink>
                       |</link:linkbase>""".stripMargin.trim
        
     docString.stripMargin.trim must beEqualTo(expected)
     
    }
  }
  
}