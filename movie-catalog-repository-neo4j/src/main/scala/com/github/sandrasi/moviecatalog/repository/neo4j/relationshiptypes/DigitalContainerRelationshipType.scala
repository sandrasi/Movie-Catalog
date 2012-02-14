package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

object DigitalContainerRelationshipType extends AbstractRelationshipType {

  type DigitalContainerRelationshipType = RelationshipTypeValue

  final val WithContent = relationshipTypeValue("withContent")
  final val WithSoundtrack = relationshipTypeValue("withSoundtrack")
  final val WithSubtitle = relationshipTypeValue("withSubtitle")
}
