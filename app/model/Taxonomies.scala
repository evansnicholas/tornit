package model

import play.api.Play
import play.api.Play.current

import java.net.URI

object Taxonomies extends TaxonomyApi {

  private def taxoApi: TaxonomyApi = {
   Play.application.plugin[TaxonomyPlugin]
   .getOrElse(throw new IllegalStateException("Taxonomy plugin not loaded"))
   .api
  }
  
  def loadEntrypoint(entrypointPath: String) = {
    taxoApi.loadEntrypoint(entrypointPath)
  }
  
  def entrypointUris: Set[URI] = {
    taxoApi.entrypointUris
  }
  
  def queryEntrypointUris(query: String): List[Entrypoint] = {
    taxoApi.queryEntrypointUris(query)
  }
  
  def listConcepts(entrypointPath: String, query:String): List[Concept] = 
    taxoApi.listConcepts(entrypointPath, query)
  
  def listPresentationElrs(entrypointPath: String): List[String] =
    taxoApi.listPresentationElrs(entrypointPath)
  
  def computeDtsGraph(entrypointPath: String): DtsGraph = 
    taxoApi.computeDtsGraph(entrypointPath)
  
  def findDimensionalGraphs(entrypointPath: String, namespace: String, localPart: String): List[DimensionsGraph] = 
    taxoApi.findDimensionalGraphs(entrypointPath, namespace, localPart)
  
  def computePresentationTree(entrypointPath: String, elr: String): PresentationELR = 
    taxoApi.computePresentationTree(entrypointPath, elr)
  
  def showTaxonomyDocument(entrypointPath: String, docUriString: String): String = 
    taxoApi.showTaxonomyDocument(entrypointPath, docUriString)
  
  def findConceptLabels(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Label] = 
    taxoApi.findConceptLabels(entrypointPath, conceptNamespace, conceptLocalName)
  
  def findConceptReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Reference] = 
    taxoApi.findConceptReferences(entrypointPath, conceptNamespace, conceptLocalName)
    
  def findConceptElementDeclaration(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): ConceptElementDeclaration = {
    taxoApi.findConceptElementDeclaration(entrypointPath, conceptNamespace, conceptLocalName)
  }
  
  def findConceptInfo(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): ConceptInfo = {
    taxoApi.findConceptInfo(entrypointPath, conceptNamespace, conceptLocalName)
  }
}