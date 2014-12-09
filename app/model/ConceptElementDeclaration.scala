package model

import eu.cdevreeze.yaidom.core.EName

case class ConceptElementDeclaration(ename: EName, elementDeclaration: String, substitutionGroupHierarchy: Seq[EName], typeHierarchy: Seq[EName])
