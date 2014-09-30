package model

import java.net.URI
import java.net.URLDecoder
import play.api.cache.Cache
import play.api.Logger
import nl.ebpi.tqa.relationshipaware.RelationshipAwareTaxonomy
import play.api.Play.current
import eu.cdevreeze.yaidom.EName

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
  
  def computeDimensionalGraphs(entrypointPath: String, conceptEName: EName): List[DimensionsGraph] = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val rat = Cache.getOrElse[RelationshipAwareTaxonomy](entrypointUri.toString){
      dtsCollection.findEntrypointDtsAsRelationshipAwareTaxonomy(entrypointUri)
    }
    DimensionsGraph.computeGraph(rat, entrypointUri, conceptEName).toList
  }
  
  
  
}