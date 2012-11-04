package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait CharacterRelationshipType extends CharacterRelationshipType.RelationshipType

case object CharacterRelationshipType extends AbstractRelationshipType[CharacterRelationshipType] {

  case object Played extends CharacterRelationshipType
  case object AppearedIn extends CharacterRelationshipType

  Played; AppearedIn
}
