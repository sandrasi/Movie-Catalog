package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait CrewRelationshipType extends CrewRelationshipType.ClassBasedRelationshipType

case object CrewRelationshipType extends AbstractClassBasedRelationshipType[CrewRelationshipType] {

  case object Actor extends CrewRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Actor] }
  case object Actress extends CrewRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Actress] }

  Actor; Actress
}
