package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import com.github.sandrasi.moviecatalog.domain

sealed trait CrewRelationshipType extends CrewRelationshipType.ClassBasedRelationshipType

case object CrewRelationshipType extends AbstractClassBasedRelationshipType[CrewRelationshipType] {

  case object Actor extends CrewRelationshipType { override def forClass = classOf[domain.Actor] }
  case object Actress extends CrewRelationshipType { override def forClass = classOf[domain.Actress] }

  Actor; Actress
}
