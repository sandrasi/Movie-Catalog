package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait SubreferenceRelationshipType extends SubreferenceRelationshipType.ClassBasedRelationshipType

case object SubreferenceRelationshipType extends AbstractClassBasedRelationshipType[SubreferenceRelationshipType] {

  case object Actor extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actor] }
  case object Actress extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actress] }
  case object Cast extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Cast] }
  case object Character extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Character] }
  case object DigitalContainer extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.container.DigitalContainer] }
  case object MotionPicture extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.core.MotionPicture] }
  case object Movie extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Movie] }
  case object Person extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Person] }
  case object Soundtrack extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.container.Soundtrack] }
  case object Subtitle extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.entities.container.Subtitle] }

  Actor; Actress; Cast; Character; DigitalContainer; MotionPicture; Movie; Person; Soundtrack; Subtitle
}
