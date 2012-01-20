package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

object DigitalContainerRelationshipType extends AbstractRelationshipType {

  type DigitalContainerRelationshipType = RelationshipTypeValue

  final val StoredIn = relationshipTypeValue("storedIn")
  final val WithSoundtrack = relationshipTypeValue("withSoundtrack")
  final val WithSubtitle = relationshipTypeValue("withSubtitle")
}
