package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.AbstractCast
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}

private[neo4j] object SubreferenceRelationshipType extends AbstractClassBasedRelationshipType[VersionedLongIdEntity] {

  type SubreferenceRelationshipType = RelationshipTypeValue

  final val AbstractCast = relationshipTypeValue(classOf[AbstractCast])
  final val Character = relationshipTypeValue(classOf[Character])
  final val DigitalContainer = relationshipTypeValue(classOf[DigitalContainer])
  final val Movie = relationshipTypeValue(classOf[Movie])
  final val Person = relationshipTypeValue(classOf[Person])
  final val Soundtrack = relationshipTypeValue(classOf[Soundtrack])
  final val Subtitle = relationshipTypeValue(classOf[Subtitle])
}
