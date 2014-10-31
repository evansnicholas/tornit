import play.api._
import play.api.Play.current
import nl.ebpi.tqa.taxonomies.DtsCollections
import play.api.Play.current
import java.net.URI
import nl.ebpi.tqa.uris.DefaultUriConverter
import nl.ebpi.tqa.names.CachingENameProvider
import nl.ebpi.tqa.names.CachingQNameProvider

package object model {

  val enameProviderCacheSize = System.getProperty("enameProvider.cache.size", "5000").toInt
  CachingENameProvider.setAsGlobalENameProvider(enameProviderCacheSize)

  val qnameProviderCacheSize = System.getProperty("qnameProvider.cache.size", "5000").toInt
  CachingQNameProvider.setAsGlobalQNameProvider(qnameProviderCacheSize)

  val taxoRootDir = Play.getFile("lib/taxonomy")
  val uriConverter = new DefaultUriConverter(taxoRootDir)
  val taxoCache = DtsCollections.createTaxonomyDocCache(uriConverter, 500)
  val entrypointFilter: URI => Boolean = uri => uri.getPath().contains("/entrypoints/")
  
  val dtsCollection = DtsCollections.getLocallyMirroredDtsCollection(uriConverter, taxoCache, entrypointFilter)
  
}
