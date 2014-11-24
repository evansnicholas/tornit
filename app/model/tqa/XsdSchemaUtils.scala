package model.tqa

import eu.cdevreeze.yaidom.core.EName
import nl.ebpi.tqa.model.xsd.{ XsdSchema, GlobalElementDeclaration }
import nl.ebpi.tqa.XsAnyTypeEName 

object XsdSchemaUtils {

  def findSubstitutionGroupHierarchy(xsdSchema: XsdSchema)(elemDec: GlobalElementDeclaration): List[EName] = {
    (elemDec.substitutionGroupOption map { sg => 
      xsdSchema.findSubstitutionGroupAncestorsOrSelf(sg)
    }).getOrElse(List.empty[EName])
  }
  
  def findTypeAncestry(xsdSchema: XsdSchema)(elemDec: GlobalElementDeclaration): List[EName] = {
    xsdSchema.findAncestorTypes(findElemType(elemDec))
  }
  
  def findElemType(elemDec: GlobalElementDeclaration): EName = {
    elemDec.typeAttributeOption orElse {
      elemDec.anonymousTypeDefinitionOption flatMap { typeDef =>
        typeDef.baseTypeOption
      }
    } getOrElse XsAnyTypeEName
  }
  
  
  
}