package com.github.sandrasi.moviecatalog.service.dtos

import java.util.{Locale, UUID}
import java.util.Locale.US
import com.github.sandrasi.moviecatalog.domain

sealed trait Entity {

  def id: Option[String]
}

sealed trait Cast extends Crew {

  def character: Character
}

sealed trait Crew extends Entity {

  def person: Person
  def motionPicture: MotionPicture
}

sealed trait MotionPicture extends Entity {

  def originalTitle: String
  def localizedTitle: Option[String]
  def genres: Set[Genre]
  def runtime: Long
  def releaseDate: String
}

case class Actor(id: Option[String], person: Person, character: Character, motionPicture: MotionPicture) extends Cast

case class Actress(id: Option[String], person: Person, character: Character, motionPicture: MotionPicture) extends Cast

case class Character(id: Option[String], name: String, creator: String, creationDate: String) extends Entity

case class DigitalContainer(id: Option[String], motionPicture: MotionPicture, soundtracks: Set[Soundtrack], subtitles: Set[Subtitle]) extends Entity

case class Genre(id: Option[String], code: String, name: Option[String]) extends Entity

case class Movie(id: Option[String], originalTitle: String, localizedTitle: Option[String], genres: Set[Genre], runtime: Long, releaseDate: String) extends MotionPicture

case class Person(id: Option[String], name: String, gender: String, dateOfBirth: String, placeOfBirth: String) extends Entity

case class Soundtrack(id: Option[String], languageCode: String, formatCode: String, languageName: Option[String], formatName: Option[String]) extends Entity

case class Subtitle(id: Option[String], languageCode: String, languageName: Option[String]) extends Entity

object DtoSupport {

  private implicit def optionalUuidToOptionalString(id: Option[UUID]) = if (id.isDefined) Some(id.get.toString) else None

  implicit def toActorDto(a: domain.Actor)(implicit l: Locale = US): Actor = Actor(a.id, a.person, a.character, a.motionPicture)

  implicit def toActressDto(a: domain.Actress)(implicit l: Locale = US): Actress = Actress(a.id, a.person, a.character, a.motionPicture)

  implicit def toCharacterDto(c: domain.Character): Character = Character(c.id, c.name, c.creator, c.creationDate.toString)

  implicit def toDigitalContainerDto(dc: domain.DigitalContainer): DigitalContainer = DigitalContainer(dc.id, dc.motionPicture, dc.soundtracks.map(toSoundtrackDto(_)), dc.subtitles.map(toSubtitleDto(_)))

  implicit def toGenreDto(g: domain.Genre): Genre = Genre(g.id, g.code, if (g.name.isDefined) Some(g.name.get.text) else None)

  implicit def toMotionPictureDto(m: domain.MotionPicture)(implicit l: Locale = US): MotionPicture = m match {
    case movie: domain.Movie => Movie(m.id, m.originalTitle.text, if (m.localizedTitles.exists(_.locale == l)) Some(m.localizedTitles.filter(_.locale == l).head.text) else None, m.genres.map(toGenreDto(_)), m.runtime.getMillis, m.releaseDate.toString)
    case _ => throw new IllegalArgumentException("Unsupported motion picture type: %s".format(m.getClass.getName))
  }

  implicit def toPersonDto(p: domain.Person): Person = Person(p.id, p.name, p.gender.toString, p.dateOfBirth.toString, p.placeOfBirth)

  implicit def toSoundtrackDto(s: domain.Soundtrack): Soundtrack = Soundtrack(s.id, s.languageCode, s.formatCode, if (s.languageName.isDefined) Some(s.languageName.get.text) else None, if (s.formatName.isDefined) Some(s.formatName.get.text) else None)

  implicit def toSubtitleDto(s: domain.Subtitle): Subtitle = Subtitle(s.id, s.languageCode, if (s.languageName.isDefined) Some(s.languageName.get.text) else None)
}
