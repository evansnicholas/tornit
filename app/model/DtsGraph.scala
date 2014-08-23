package model

import nl.ebpi.tqa.model.taxonomy.{Taxonomy, TaxonomyDoc}
import java.net.URI

case class DtsGraph(uri: String, children: IndexedSeq[DtsGraph])

object DtsGraph {

  def computeGraph(taxo: Taxonomy, entrypointUri: URI): DtsGraph = {
    val docsByUri = taxo.taxonomyDocsByUri
    
    def buildGraph(seenDocs: Set[URI], docUri: URI): (Set[URI], DtsGraph) = {
      val referencedDocs = docsByUri(docUri).usedDocUris
      val newDocs = referencedDocs diff seenDocs
      val updatedSeenDocs = seenDocs ++ newDocs
      val (allSeen, children) = 
        newDocs.foldLeft((updatedSeenDocs, IndexedSeq.empty[DtsGraph])){ case ((seen, graphs), doc) =>
          val (allSeen, graph) = buildGraph(seen, doc)
          (seen ++ allSeen, graphs :+ graph)
        }
      (allSeen, DtsGraph(docUri.toString, children ))
    }  
    
    val (_, graph) = buildGraph(Set.empty[URI], entrypointUri)
    graph
  }
  
}