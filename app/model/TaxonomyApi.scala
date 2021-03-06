package model

import java.net.URI
import eu.cdevreeze.yaidom.core.EName

case class Entrypoint(uri: String)
case class Concept(namespace: String, localPart: String, conceptType: String)
case class ConceptInfo(ename: EName, substitutionGroup: EName, xsdType: EName)

trait TaxonomyApi {
  
  def entrypointUris: Set[URI]
  
  def loadEntrypoint(entrypointPath: String)
  
  def queryEntrypointUris(query: String): List[Entrypoint]
  
  def listConcepts(entrypointPath: String, query:String): List[Concept]
  
  def listPresentationElrs(entrypointPath: String): List[String]
  
  def computeDtsGraph(entrypointPath: String): DtsGraph
  
  def findDimensionalGraphs(entrypointPath: String, namespace: String, localPart: String): List[DimensionsGraph]
  
  def computePresentationTree(entrypointPath: String, elr: String): PresentationELR
  
  def showTaxonomyDocument(entrypointPath: String, docUriString: String): String
  
  def findConceptLabels(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Label]
  
  def findConceptReferences(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): List[Reference]
  
  def findConceptElementDeclaration(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): ConceptElementDeclaration
  
  def findConceptInfo(entrypointPath: String, conceptNamespace: String, conceptLocalName: String): ConceptInfo
}