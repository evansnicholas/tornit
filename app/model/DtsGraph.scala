package model

import java.net.URI

case class DtsGraph(uri: String, children: Seq[DtsGraph])