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
        s"/taxoDoc?uri=$entrypointUri&docUri=$docUri"

      val entrypointsResult = route(FakeRequest(GET, requestUri)).get
      
      val docString = contentAsString(entrypointsResult)

      val expected = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
  This file is part of the Nederlandse taxonomy (NT, Dutch Taxonomy) 
  Intellectual Property State of the Netherlands 
  Version 8.0
  Released by the Dutch SBR Programme 
  Release date Mon Dec 2 09:02:00 2013
--><link:linkbase xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:xlink="http://www.w3.org/1999/xlink">
  <link:roleRef roleURI="urn:kvk:linkrole:share-capital-domain" xlink:href="kvk-linkroles-domains.xsd#kvk-dlrd_ShareCapitalDomain" xlink:type="simple"/>
  <link:arcroleRef arcroleURI="http://xbrl.org/int/dim/arcrole/domain-member" xlink:href="http://www.xbrl.org/2005/xbrldt-2005.xsd#domain-member" xlink:type="simple"/>
  <link:definitionLink xlink:role="urn:kvk:linkrole:share-capital-domain" xlink:type="extended">
    <link:loc xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_ShareCapitalComponentDomain" xlink:label="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:type="locator"/>
    <link:loc xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_OrdinarySharesMember" xlink:label="bw2-dm-eqty_OrdinarySharesMember_loc" xlink:type="locator"/>
    <link:loc xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_PreferredSharesMember" xlink:label="bw2-dm-eqty_PreferredSharesMember_loc" xlink:type="locator"/>
    <link:loc xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_PrioritySharesMember" xlink:label="bw2-dm-eqty_PrioritySharesMember_loc" xlink:type="locator"/>
    <link:loc xlink:href="../../../basis/venj/domains/bw2-domains-equity.xsd#bw2-dm-eqty_TreasurySharesMember" xlink:label="bw2-dm-eqty_TreasurySharesMember_loc" xlink:type="locator"/>
    <link:definitionArc xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:to="bw2-dm-eqty_OrdinarySharesMember_loc" order="1" xlink:type="arc"/>
    <link:definitionArc xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:to="bw2-dm-eqty_PreferredSharesMember_loc" order="2" xlink:type="arc"/>
    <link:definitionArc xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:to="bw2-dm-eqty_PrioritySharesMember_loc" order="3" xlink:type="arc"/>
    <link:definitionArc xlink:arcrole="http://xbrl.org/int/dim/arcrole/domain-member" xlink:from="bw2-dm-eqty_ShareCapitalComponentDomain_loc" xlink:to="bw2-dm-eqty_TreasurySharesMember_loc" order="4" xlink:type="arc"/>
  </link:definitionLink>
</link:linkbase>""".trim()
        
     docString.trim() must beEqualTo(expected)
     
    }
  }
  
}