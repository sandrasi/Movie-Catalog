package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait LocalizedTextRelationshipType extends LocalizedTextRelationshipType.RelationshipType

case object LocalizedTextRelationshipType extends AbstractRelationshipType[LocalizedTextRelationshipType] {

  case object Locale extends LocalizedTextRelationshipType

  Locale
}
