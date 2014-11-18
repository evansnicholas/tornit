package model.tqa

import eu.cdevreeze.yaidom.EName
import nl.ebpi.tqa.model.xsd.{ XsdSchema, GlobalElementDeclaration }

object XsdSchemaUtils {

  def findSubstitutionGroupHierarchy(xsdSchema: XsdSchema)(elemDec: GlobalElementDeclaration): List[EName] = {
    (elemDec.substitutionGroupOption map { sg => 
      xsdSchema.findSubstitutionGroupAncestorsOrSelf(sg)
    }).getOrElse(List.empty[EName])
  }
  
  
}