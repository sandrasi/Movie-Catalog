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
  def genres: Option[Set[Genre]]
  def runtime: Option[Long]
  def releaseDate: Option[String]
}

case class Actor(id: Option[String], person: Person, character: Character, motionPicture: MotionPicture) extends Cast

case class Actress(id: Option[String], person: Person, character: Character, motionPicture: MotionPicture) extends Cast

case class Character(id: Option[String], name: String, creator: Option[String] = None, creationDate: Option[String] = None) extends Entity

case class DigitalContainer(id: Option[String], motionPicture: MotionPicture, soundtracks: Set[Soundtrack], subtitles: Set[Subtitle]) extends Entity

case class Genre(id: Option[String], code: String, name: Option[String] = None) extends Entity

case class Movie(id: Option[String], originalTitle: String, localizedTitle: Option[String] = None, genres: Option[Set[Genre]] = None, runtime: Option[Long] = None, releaseDate: Option[String] = None) extends MotionPicture

case class Person(id: Option[String], name: String, gender: Option[String] = None, dateOfBirth: Option[String] = None, placeOfBirth: Option[String] = None) extends Entity

case class Soundtrack(id: Option[String], languageCode: String, formatCode: String, languageName: Option[String] = None, formatName: Option[String] = None) extends Entity

case class Subtitle(id: Option[String], languageCode: String, languageName: Option[String] = None) extends Entity

object DtoSupport {

  private implicit def optionalUuidToOptionalString(id: Option[UUID]) = id.map(_.toString)

  implicit def toActorDto(a: domain.Actor)(implicit l: Locale = US): Actor = Actor(a.id, toShortPersonDto(a.person), toShortCharacterDto(a.character), toShortMotionPictureDto(a.motionPicture))

  implicit def toActressDto(a: domain.Actress)(implicit l: Locale = US): Actress = Actress(a.id, toShortPersonDto(a.person), toShortCharacterDto(a.character), toShortMotionPictureDto(a.motionPicture))

  implicit def toCharacterDto(c: domain.Character): Character = Character(c.id, c.name, c.creator, c.creationDate.map(_.toString))

  private def toShortCharacterDto(c: domain.Character): Character = Character(c.id, c.name)

  implicit def toDigitalContainerDto(dc: domain.DigitalContainer): DigitalContainer = DigitalContainer(dc.id, toShortMotionPictureDto(dc.motionPicture), dc.soundtracks.map(toShortSoundtrackDto(_)), dc.subtitles.map(toShortSubtitleDto(_)))

  implicit def toGenreDto(g: domain.Genre): Genre = Genre(g.id, g.code, g.name.map(_.text))

  private def toShortGenreDto(g: domain.Genre): Genre = Genre(g.id, g.code)

  implicit def toMotionPictureDto(m: domain.MotionPicture)(implicit l: Locale = US): MotionPicture = m match {
    case movie: domain.Movie => Movie(m.id, m.originalTitle.text, m.localizedTitle.find(_.locale == l).map(_.text), Option(m.genres.map(toShortGenreDto(_))), m.runtime.map(_.getMillis), m.releaseDate.map(_.toString))
    case _ => throw new IllegalArgumentException("Unsupported motion picture type: %s".format(m.getClass.getName))
  }

  private def toShortMotionPictureDto(m: domain.MotionPicture)(implicit l: Locale = US): MotionPicture = m match {
    case movie: domain.Movie => Movie(m.id, m.originalTitle.text, m.localizedTitle.find(_.locale == l).map(_.text))
    case _ => throw new IllegalArgumentException("Unsupported motion picture type: %s".format(m.getClass.getName))
  }

  implicit def toPersonDto(p: domain.Person): Person = Person(p.id, p.name, Option(p.gender.toString), Option(p.dateOfBirth.toString), Option(p.placeOfBirth))

  private def toShortPersonDto(p: domain.Person): Person = Person(p.id, p.name)

  implicit def toSoundtrackDto(s: domain.Soundtrack): Soundtrack = Soundtrack(s.id, s.languageCode, s.formatCode, s.languageName.map(_.text), s.formatName.map(_.text))

  private def toShortSoundtrackDto(s: domain.Soundtrack): Soundtrack = Soundtrack(s.id, s.languageCode, s.formatCode)

  implicit def toSubtitleDto(s: domain.Subtitle): Subtitle = Subtitle(s.id, s.languageCode, s.languageName.map(_.text))

  private def toShortSubtitleDto(s: domain.Subtitle): Subtitle = Subtitle(s.id, s.languageCode)
}
