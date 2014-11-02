package model

import play.api.{ Application, Plugin }

abstract class TaxonomyPlugin(app: Application) extends Plugin {

  def api: TaxonomyApi
  
  override def onStart() = {
    //no startup
  }

  override def onStop() = {
    //no stop
  }
  
}

