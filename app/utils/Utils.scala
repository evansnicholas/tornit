package utils

import nl.ebpi.tqa.printing.DocumentPrinterUtils
import java.util.regex.Pattern

object Utils {

  val docPrinter = DocumentPrinterUtils.newDocumentPrinter
  
  /**
   * Given a list of strings filters the list based on the query parameter.  The query paramater is treated both as an 
   * ordinary query string and as regex. 
   */
  def filterWithQuery[A](list: List[A])(f: A => String)(query: String): List[A] = {
    val lowerCaseQuery = query.toLowerCase()
    list filter { e =>
      val string  = f(e)
      string.toLowerCase().contains(lowerCaseQuery)
    }
  }
  
}