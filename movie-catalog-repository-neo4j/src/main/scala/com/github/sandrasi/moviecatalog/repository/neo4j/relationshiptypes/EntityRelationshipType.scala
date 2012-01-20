package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

private[neo4j] object EntityRelationshipType extends AbstractRelationshipType {

  type EntityRelationshipType = RelationshipTypeValue

  final val IsA = relationshipTypeValue("isA")
}
