package model

import eu.cdevreeze.yaidom.EName

case class ConceptElementDeclaration(ename: EName, elementDeclaration: String, substitutionGroupHierarchy: List[EName], typeHierarchy: List[EName])
