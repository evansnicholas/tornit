package model
package tqa

import play.api._
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.json.JsString
import java.net.URI
import java.net.URLDecoder
import java.net.URI
import nl.ebpi.tqa.uris.DefaultUriConverter
import nl.ebpi.tqa.taxonomies.DtsCollections
import nl.ebpi.tqa.names.CachingENameProvider
import nl.ebpi.tqa.names.CachingQNameProvider
import nl.ebpi.tqa.model.relationship.ParentChildRelationship
import nl.ebpi.tqa.queryapi.QueryableTaxonomy
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import utils.Utils
import model.Concept
import model.DtsGraph
import model.Label
import model.PresentationELR
import model.PresentationTree
import model.Reference
import model.TaxonomyApi
import model.TaxonomyPlugin
import eu.cdevreeze.yaidom.utils.NamespaceUtils
import org.apache.commons.lang3.StringEscapeUtils
import nl.ebpi.tqa.model.xsd.XsdSchema
import nl.ebpi.tqa.model.xsd.XsdDoc

class TqaTaxonomyPlugin(app: Application) extends TaxonomyPlugin(app){

  def api: TaxonomyApi = TqaTaxonomyApi
  
  override def enabled: Boolean = true

}

object TqaTaxonomyApi extends TaxonomyApi {
  
  val enameProviderCacheSize = System.getProperty("enameProvider.cache.size", "5000").toInt
  CachingENameProvider.setAsGlobalENameProvider(enameProviderCacheSize)

  val qnameProviderCacheSize = System.getProperty("qnameProvider.cache.size", "5000").toInt
  CachingQNameProvider.setAsGlobalQNameProvider(qnameProviderCacheSize)

  val taxoRootDir = Play.getFile("lib/taxonomy")
  val uriConverter = new DefaultUriConverter(taxoRootDir)
  val taxoCache = DtsCollections.createTaxonomyDocCache(uriConverter, 500)
  val entrypointFilter: URI => Boolean = uri => uri.getPath().contains("/entrypoints/")
  
  val dtsCollection = DtsCollections.getLocallyMirroredDtsCollection(uriConverter, taxoCache, entrypointFilter)
  
  def entrypointUris = {
    dtsCollection.allEntrypointUris
  }
  
  def loadEntrypoint(entrypointPath: String) = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
  }
  
  def queryEntrypointUris(query: String) = {
    Utils.filterWithQuery(dtsCollection.allEntrypointUris.toList)(_.toString())(query) map { u => Entrypoint(u.toString()) }
  }
  
  def listConcepts(entrypointPath: String, query:String): List[Concept] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    val items = 
      fullTaxo.taxo.taxonomy.findAllItemDeclarationsByEName.keySet map { e => 
        Concept(e.namespaceUriOption.getOrElse(""), e.localPart, "item") 
      }
    val tuples = 
      fullTaxo.taxo.taxonomy.findAllTupleDeclarationsByEName.keySet map { e => 
        Concept(e.namespaceUriOption.getOrElse(""), e.localPart, "tuple") 
      }
    
    val conceptsList = (items ++ tuples).toList.sortBy(_.localPart.size)
    
    Utils.filterWithQuery(conceptsList)(c => c.localPart)(query)
  }
  
  def listPresentationElrs(entrypointPath: String): List[String] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    fullTaxo.taxo.findRelationships[ParentChildRelationship].map(_.extendedLinkRole).distinct.toList
  }
  
  def computeDtsGraph(entrypointPath: String): DtsGraph = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    DtsGraph.computeGraph(fullTaxo.taxo.taxonomy, entrypointUri)
  }
  
  def findDimensionalGraphs(entrypointPath: String, namespace: String, localPart: String): List[DimensionsGraph] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    val decodedNs = URLDecoder.decode(namespace, "UTF-8")
    val conceptEName = EName(decodedNs, localPart)
    DimensionsGraphBuilder.findDimensionsGraphs(fullTaxo, conceptEName)
  }
  
  def computePresentationTree(entrypointPath: String, elr: String): PresentationELR = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    PresentationTree.createPresentationTree(fullTaxo.taxo, elr)
  }
  
  def showTaxonomyDocument(entrypointPath: String, docUriString: String): String = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val docUri = new URI(URLDecoder.decode(docUriString, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    val doc = fullTaxo.taxo.taxonomy.taxonomyDocsByUri(docUri).taxoDoc.docawareDoc.document
    Utils.docPrinter.print(doc)
  }

  def findConceptLabels(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Label] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }

    val conceptEName = EName(URLDecoder.decode(conceptNamespace, "UTF-8"), conceptLocalName)
    val conceptLabels = fullTaxo.taxo.findConceptLabelsByConcept(conceptEName)

    conceptLabels.toList map { conceptLabel =>
      Label(role = conceptLabel.resourceRole, language = conceptLabel.language, text = conceptLabel.labelText)
    }
  }

  def findConceptReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Reference] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString){
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }

    val conceptEName = EName(URLDecoder.decode(conceptNamespace, "UTF-8"), conceptLocalName)
    val conceptReferences = fullTaxo.taxo.findConceptReferencesByConcept(conceptEName)

    conceptReferences.toList map { conceptRef =>
      val parts = conceptRef.referenceElems map { partElem =>
        ReferencePart(
          partNamespace = partElem.resolvedName.namespaceUriOption.getOrElse(""),
          partLocalName = partElem.resolvedName.localPart,
          partValue = partElem.text.trim)
      }
      Reference(conceptRef.resourceRole, parts)
    }
  }

  def findConceptElementDeclaration(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): ConceptElementDeclaration = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[QueryableTaxonomy](entrypointUri.toString) {
      new QueryableTaxonomy(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }

    import nl.ebpi.tqa.XsSchemaEName 
    
    val decodedConceptNamespace = URLDecoder.decode(conceptNamespace, "UTF-8")
    val ename = EName(decodedConceptNamespace, conceptLocalName)
    val xsdSchema =
      XsdSchema.build(fullTaxo.taxo.taxonomy.taxonomyDocs.collect { case doc if doc.documentElement.resolvedName == XsSchemaEName => XsdDoc(doc.taxoDoc) })
    val elementDeclaration = xsdSchema.getGlobalElementDeclarationByEName(ename)
    val substitutionGroupHierarchy = XsdSchemaUtils.findSubstitutionGroupHierarchy(xsdSchema)(elementDeclaration)
    val typeHierarchy = XsdSchemaUtils.findTypeAncestry(xsdSchema)(elementDeclaration)

    val elementDeclIndexedElem =
      indexed.Elem(elementDeclaration.docawareElem.rootElem, elementDeclaration.docawareElem.path)
    val strippedElementDecl =
      NamespaceUtils.stripUnusedNamespaces(elementDeclIndexedElem, XsdSchemaUtils.ENameExtractor)

    val elemDecString = Utils.docPrinter.print(strippedElementDecl)
       
    ConceptElementDeclaration(ename, elemDecString, substitutionGroupHierarchy, typeHierarchy)
  }
}