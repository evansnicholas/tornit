package model
package tqa

import play.api._
import play.api.Play.current
import play.api.cache.Cache
import java.net.URI
import java.net.URLDecoder
import java.net.URI
import nl.ebpi.tqa.uris.DefaultUriConverter
import nl.ebpi.tqa.taxonomies.DtsCollections
import nl.ebpi.tqa.names.CachingENameProvider
import nl.ebpi.tqa.names.CachingQNameProvider
import nl.ebpi.tqa.model.relationship.ParentChildRelationship
import nl.ebpi.tqa.dimensionaware.DimensionalPathQueryApi
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import utils.Utils
import model.Concept
import model.DimensionsGraph
import model.DtsGraph
import model.Label
import model.PresentationELR
import model.PresentationTree
import model.Reference
import model.TaxonomyApi
import model.TaxonomyPlugin
import eu.cdevreeze.yaidom.utils.NamespaceUtils

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
    Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
  }
  
  def queryEntrypointUris(query: String) = {
    Utils.filterWithQuery(dtsCollection.allEntrypointUris.toList)(_.toString())(query) map { u => Entrypoint(u.toString()) }
  }
  
  def listConcepts(entrypointPath: String, query:String): List[Concept] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    val items = 
      fullTaxo.relationshipAwareTaxonomy.taxonomy.findAllItemDeclarationsByEName.keySet map { e => 
        Concept(e.namespaceUriOption.getOrElse(""), e.localPart, "item") 
      }
    val tuples = 
      fullTaxo.relationshipAwareTaxonomy.taxonomy.findAllTupleDeclarationsByEName.keySet map { e => 
        Concept(e.namespaceUriOption.getOrElse(""), e.localPart, "tuple") 
      }
    
    val conceptsList = (items ++ tuples).toList.sortBy(_.localPart.size)
    
    Utils.filterWithQuery(conceptsList)(c => c.localPart)(query)
  }
  
  def listPresentationElrs(entrypointPath: String): List[String] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    fullTaxo.relationshipAwareTaxonomy.findRelationships[ParentChildRelationship].map(_.extendedLinkRole).distinct.toList
  }
  
  def computeDtsGraph(entrypointPath: String): DtsGraph = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    DtsGraph.computeGraph(fullTaxo.relationshipAwareTaxonomy.taxonomy, entrypointUri)
  }
  
  def computeDimensionalGraphs(entrypointPath: String, namespace: String, localPart: String): List[DimensionsGraph] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    val decodedNs = URLDecoder.decode(namespace, "UTF-8")
    val conceptEName = EName(decodedNs, localPart)
    DimensionsGraph.computeGraph(fullTaxo, entrypointUri, conceptEName).toList
  }
  
  def computePresentationTree(entrypointPath: String, elr: String): PresentationELR = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    PresentationTree.createPresentationTree(fullTaxo.relationshipAwareTaxonomy, elr)
  }
  
  def showTaxonomyDocument(entrypointPath: String, docUriString: String): String = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val docUri = new URI(URLDecoder.decode(docUriString, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }
    val doc = fullTaxo.relationshipAwareTaxonomy.taxonomy.taxonomyDocsByUri(docUri).doc.document
    Utils.docPrinter.print(doc)
  }

  def findConceptLabels(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Label] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }

    val conceptEName = EName(URLDecoder.decode(conceptNamespace, "UTF-8"), conceptLocalName)
    val conceptLabels = fullTaxo.relationshipAwareTaxonomy.findConceptLabelsByConcept(conceptEName)

    conceptLabels.toList map { conceptLabel =>
      Label(role = conceptLabel.resourceRole, language = conceptLabel.language, text = conceptLabel.labelText)
    }
  }

  def findConceptReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Reference] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString){
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }

    val conceptEName = EName(URLDecoder.decode(conceptNamespace, "UTF-8"), conceptLocalName)
    val conceptReferences = fullTaxo.relationshipAwareTaxonomy.findConceptReferencesByConcept(conceptEName)

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
    val fullTaxo = Cache.getOrElse[DimensionalPathQueryApi](entrypointUri.toString) {
      DimensionalPathQueryApi(dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri))
    }

    val decodedConceptNamespace = URLDecoder.decode(conceptNamespace, "UTF-8")
    val ename = EName(decodedConceptNamespace, conceptLocalName)
    val xsdSchema = fullTaxo.relationshipAwareTaxonomy.taxonomy
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