package model

import java.net.URI
import java.net.URLDecoder

case class Entrypoint(uri: String)

object Taxonomies {

  def listEntrypoints: List[Entrypoint] = {
    (dtsCollection.allEntrypointUris map { u => Entrypoint(u.toString()) }).toList
  }
  
  def computeDtsGraph(entrypointPath: String): DtsGraph = {
    val entrypointUri = new URI(URLDecoder.decode(entrypointPath, "UTF-8"))
    val taxo = dtsCollection.findEntrypointDtsAsTaxonomy(entrypointUri)
    DtsGraph.computeGraph(taxo, entrypointUri)
  }
  
}