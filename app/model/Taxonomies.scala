package model

import java.net.URI
import java.net.URLDecoder
import play.api.cache.Cache
import play.api.Logger
import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import play.api.Play.current
import eu.cdevreeze.yaidom.EName

import utils.Utils

case class Entrypoint(uri: String)

case class Concept(namespace: String, localPart: String, conceptType: String)

object Taxonomies {

  def listEntrypoints(query: String): List[Entrypoint] = {
    val filteredEntrypoints = dtsCollection.allEntrypointUris filter { _.toString().contains(query) }
    (filteredEntrypoints map { u => Entrypoint(u.toString()) }).toList
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
    
    (items ++ tuples).toList filter { case Concept(_, lp, _) => lp.startsWith(query) }
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
  
  def computePresentationTree(entrypointPath: String): List[PresentationELR] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    PresentationTree.createPresentationTree(rat)
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
  
  
}