package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

private[neo4j] object LocalizedTextRelationshipType extends AbstractRelationshipType {

  type LocalizedTextRelationshipType = RelationshipTypeValue

  final val Locale = relationshipTypeValue("locale")
}
