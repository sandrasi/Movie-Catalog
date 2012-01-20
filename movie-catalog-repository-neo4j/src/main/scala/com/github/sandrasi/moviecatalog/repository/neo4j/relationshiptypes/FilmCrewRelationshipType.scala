package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{FilmCrew, Actor, Actress}

private[neo4j] object FilmCrewRelationshipType extends AbstractClassBasedRelationshipType[FilmCrew] {

  type FilmCrewRelationshipType = RelationshipTypeValue
  
  final val Actor = relationshipTypeValue(classOf[Actor])
  final val Actress = relationshipTypeValue(classOf[Actress])
}
