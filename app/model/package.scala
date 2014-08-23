import play.api._
import play.api.Play.current
import nl.ebpi.tqa.taxonomies.DtsCollections
import play.api.Play.current
import java.net.URI

package object model {

  val taxoRootDir = Play.getFile("lib/taxonomy")
  val taxoCache = DtsCollections.createTaxonomyDocCache(taxoRootDir, 500)
  val entrypointFilter: URI => Boolean = uri => uri.getPath().contains("/entrypoints/")
  val dtsCollection = DtsCollections.getLocallyMirroredDtsCollection(taxoRootDir, taxoCache, entrypointFilter)
  
}