package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait MotionPictureRelationshipType extends MotionPictureRelationshipType.RelationshipType

case object MotionPictureRelationshipType extends AbstractRelationshipType[MotionPictureRelationshipType] {

  case object HasGenre extends MotionPictureRelationshipType

  HasGenre
}
