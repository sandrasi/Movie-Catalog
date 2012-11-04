package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait DigitalContainerRelationshipType extends DigitalContainerRelationshipType.RelationshipType

case object DigitalContainerRelationshipType extends AbstractRelationshipType[DigitalContainerRelationshipType] {

  case object WithContent extends DigitalContainerRelationshipType
  case object WithSoundtrack extends DigitalContainerRelationshipType
  case object WithSubtitle extends DigitalContainerRelationshipType

  WithContent; WithSoundtrack; WithSubtitle
}
