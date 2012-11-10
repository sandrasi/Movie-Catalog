package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

sealed trait SubreferenceRelationshipType extends SubreferenceRelationshipType.ClassBasedRelationshipType

case object SubreferenceRelationshipType extends AbstractClassBasedRelationshipType[SubreferenceRelationshipType] {

  case object Actor extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Actor] }
  case object Actress extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Actress] }
  case object Cast extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Cast] }
  case object Character extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Character] }
  case object DigitalContainer extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.DigitalContainer] }
  case object Genre extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Genre] }
  case object MotionPicture extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.MotionPicture] }
  case object Movie extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Movie] }
  case object Person extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Person] }
  case object Soundtrack extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Soundtrack] }
  case object Subtitle extends SubreferenceRelationshipType { override def forClass = classOf[com.github.sandrasi.moviecatalog.domain.Subtitle] }

  Actor; Actress; Cast; Character; DigitalContainer; Genre; MotionPicture; Movie; Person; Soundtrack; Subtitle
}
