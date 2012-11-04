package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait EntityRelationshipType extends EntityRelationshipType.RelationshipType

case object EntityRelationshipType extends AbstractRelationshipType[EntityRelationshipType] {

  case object IsA extends EntityRelationshipType

  IsA
}
