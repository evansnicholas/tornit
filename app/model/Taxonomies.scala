package model

import java.net.URI

case class Entrypoint(uri: String)


object Taxonomies {

  def listEntrypoints: List[Entrypoint] = {
    (dtsCollection.allEntrypointUris map { u => Entrypoint(u.toString()) }).toList
  }
  
}