package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

private[neo4j] object CharacterRelationshipType extends AbstractRelationshipType {

  type CharacterRelationshipType = RelationshipTypeValue

  final val PlayedBy = relationshipTypeValue("playedBy")
  final val AppearedIn = relationshipTypeValue("appearedIn")
}
