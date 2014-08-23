package model

import java.net.URI

case class Entrypoint(uri: String)

object Taxonomies {

  def listEntrypoints: List[Entrypoint] = {
    (dtsCollection.allEntrypointUris map { u => Entrypoint(u.toString()) }).toList
  }
  
  def computeDtsGraph(entrypointPath: String): DtsGraph = {
    val entrypointUri = new URI(s"http://$entrypointPath")
    val taxo = dtsCollection.findEntrypointDtsAsTaxonomy(entrypointUri)
    DtsGraph.computeGraph(taxo, entrypointUri)
  }
  
}