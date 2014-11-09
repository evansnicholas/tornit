package model

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
import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import nl.ebpi.tqa.model.relationship.ParentChildRelationship

import eu.cdevreeze.yaidom.EName

import utils.Utils

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
  
  def queryEntrypointUris(query: String) = {
    Utils.filterWithQuery(dtsCollection.allEntrypointUris.toList)(_.toString())(query) map { u => Entrypoint(u.toString()) }
  }
  
  def listConcepts(entrypointPath: String, query:String): List[Concept] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    val items = 
      rat.taxonomy.findAllItemDeclarationsByEName.keySet map { e => 
        Concept(e.namespaceUriOption.getOrElse(""), e.localPart, "item") 
      }
    val tuples = 
      rat.taxonomy.findAllTupleDeclarationsByEName.keySet map { e => 
        Concept(e.namespaceUriOption.getOrElse(""), e.localPart, "tuple") 
      }
    
    val conceptsList = (items ++ tuples).toList.sortBy(_.localPart.size)
    
    Utils.filterWithQuery(conceptsList)(c => c.localPart)(query)
  }
  
  def listPresentationElrs(entrypointPath: String): List[String] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    rat.findRelationships[ParentChildRelationship].map(_.extendedLinkRole).distinct.toList
  }
  
  def computeDtsGraph(entrypointPath: String): DtsGraph = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    DtsGraph.computeGraph(rat.taxonomy, entrypointUri)
  }
  
  def computeDimensionalGraphs(entrypointPath: String, namespace: String, localPart: String): List[DimensionsGraph] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    val decodedNs = URLDecoder.decode(namespace, "UTF-8")
    val conceptEName = EName(decodedNs, localPart)
    DimensionsGraph.computeGraph(rat, entrypointUri, conceptEName).toList
  }
  
  def computePresentationTree(entrypointPath: String, elr: String): PresentationELR = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    PresentationTree.createPresentationTree(rat, elr)
  }
  
  def showTaxonomyDocument(entrypointPath: String, docUriString: String): String = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val docUri = new URI(URLDecoder.decode(docUriString, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    val doc = rat.taxonomy.taxonomyDocsByUri(docUri).doc.document
    Utils.docPrinter.print(doc)
  }

  def findConceptLabels(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Label] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString) {
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }

    val conceptEName = EName(URLDecoder.decode(conceptNamespace, "UTF-8"), conceptLocalName)
    val conceptLabels = rat.findConceptLabelsByConcept(conceptEName)

    conceptLabels.toList map { conceptLabel =>
      Label(role = conceptLabel.resourceRole, language = conceptLabel.language, text = conceptLabel.labelText)
    }
  }

  def findConceptReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Reference] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString) {
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }

    val conceptEName = EName(URLDecoder.decode(conceptNamespace, "UTF-8"), conceptLocalName)
    val conceptReferences = rat.findConceptReferencesByConcept(conceptEName)

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
}