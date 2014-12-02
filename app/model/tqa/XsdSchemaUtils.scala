package model.tqa

import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.core.QName
import eu.cdevreeze.yaidom.core.Scope
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.utils.DocumentENameExtractor
import eu.cdevreeze.yaidom.utils.TextENameExtractor
import eu.cdevreeze.yaidom.utils.SimpleTextENameExtractor
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

  /**
   * ENameExtractor for XML schema content. It knows only about ENames in XML Schema, and nothing about ENames in
   * appinfo sections.
   */
  object ENameExtractor extends DocumentENameExtractor {

    private val attrENames = Set("ref", "type", "base", "substitutionGroup", "refer", "itemType").map(s => EName(s))
    private val memberTypesEName = EName("memberTypes")

    object MemberTypesENameExtractor extends TextENameExtractor {

      def extractENames(scope: Scope, text: String): Set[EName] = {
        val memberTypes = text.split("\\s+").toSeq
        memberTypes.flatMap(s => scope.resolveQNameOption(QName(s))).toSet
      }
    }

    def findAttributeValueENameExtractor(elem: indexed.Elem, attributeName: EName): Option[TextENameExtractor] = {
      if (attrENames.contains(attributeName)) {
        Some(SimpleTextENameExtractor)
      } else if (attributeName == memberTypesEName) Some(MemberTypesENameExtractor) else None
    }

    def findElemTextENameExtractor(elem: indexed.Elem): Option[TextENameExtractor] = None
  }
}
