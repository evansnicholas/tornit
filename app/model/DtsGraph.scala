package model

import nl.ebpi.tqa.model.taxonomy.{Taxonomy, TaxonomyDoc}
import java.net.URI

case class DtsGraph(uri: String, children: Seq[DtsGraph])