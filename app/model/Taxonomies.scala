package model

import java.net.URI
import java.net.URLDecoder

case class Entrypoint(uri: String)

object Taxonomies {

  def listEntrypoints(query: String): List[Entrypoint] = {
    val filteredEntrypoints = dtsCollection.allEntrypointUris filter { _.toString().contains(query) }
    (filteredEntrypoints map { u => Entrypoint(u.toString()) }).toList
  }
  
  def computeDtsGraph(entrypointPath: String): DtsGraph = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val taxo = dtsCollection.findEntrypointDtsAsTaxonomy(entrypointUri)
    DtsGraph.computeGraph(taxo, entrypointUri)
  }
  
}