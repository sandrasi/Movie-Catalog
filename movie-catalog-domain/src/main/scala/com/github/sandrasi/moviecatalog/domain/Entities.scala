package com.github.sandrasi.moviecatalog.domain

import com.github.sandrasi.moviecatalog.common.{LocalizedText, Validate}
import com.github.sandrasi.moviecatalog.domain.utility.Gender
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import java.util.UUID
import org.joda.time.{LocalDate, ReadableDuration}

sealed trait IdSupport[A] extends Equals {

  Validate.notNull(id)

  def id: Option[A]

  override def equals(o: Any): Boolean = o match {
    case other: IdSupport[_] => other.canEqual(this) && id == other.id
    case _ => false
  }

  override def canEqual(o: Any): Boolean = o.isInstanceOf[IdSupport[_]]

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + id.hashCode
    result
  }
}

sealed trait VersionSupport {

  Validate.valid(version >= 0)

  def version: Long
}

sealed trait Entity extends IdSupport[UUID] with VersionSupport

case class Character(name: String, creator: Option[String], dateOfCreation: Option[LocalDate], version: Long, id: Option[UUID]) extends Entity {

  Validate.notBlank(name)
  Validate.notNull(creator)
  if (creator.isDefined) Validate.notBlank(creator.get)
  Validate.notNull(dateOfCreation)

  override def equals(o: Any): Boolean = o match {
    case other: Character => other.canEqual(this) && (name == other.name) && (creator == other.creator) && (dateOfCreation == other.dateOfCreation)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + name.hashCode
    result = 3 * result + creator.hashCode
    result = 3 * result + dateOfCreation.hashCode
    result
  }
}

object Character {

  def apply(name: String,
            creator: String = null,
            dateOfCreation: LocalDate = null,
            version: Long = 0,
            id: UUID = null): Character = Character(name, Option(creator), Option(dateOfCreation), version, Option(id))
}

case class Genre(code: String, name: Option[LocalizedText], version: Long, id: Option[UUID]) extends Entity {

  Validate.notBlank(code)
  Validate.notNull(name)
  if (name.isDefined) Validate.notBlank(name.get.text)

  override def equals(o: Any): Boolean = o match {
    case other: Genre => other.canEqual(this) && code == other.code
    case _ => false
  }

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + code.hashCode
    result
  }
}

object Genre {

  def apply(code: String,
            name: LocalizedText = null,
            version: Long = 0,
            id: UUID = null): Genre = Genre(code, Option(name), version, Option(id))
}

sealed trait MotionPicture extends Entity {

  Validate.notNull(originalTitle)
  Validate.notBlank(originalTitle.text)
  Validate.notNull(localizedTitle)
  if (localizedTitle.isDefined) Validate.notBlank(localizedTitle.get.text)
  Validate.noNullElements(genres)
  Validate.notNull(runtime)
  Validate.notNull(dateOfRelease)

  def originalTitle: LocalizedText
  def localizedTitle: Option[LocalizedText]
  def genres: Set[Genre]
  def runtime: Option[ReadableDuration]
  def dateOfRelease: Option[LocalDate]

  override def equals(o: Any): Boolean = o match {
    case other: MotionPicture => other.canEqual(this) && (originalTitle == other.originalTitle) && (dateOfRelease == other.dateOfRelease)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[MotionPicture]

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + originalTitle.hashCode
    result = 3 * result + dateOfRelease.hashCode
    result
  }
}

case class Movie(originalTitle: LocalizedText, localizedTitle: Option[LocalizedText], genres: Set[Genre], runtime: Option[ReadableDuration], dateOfRelease: Option[LocalDate], version: Long, id: Option[UUID]) extends MotionPicture {

  override def equals(o: Any): Boolean = o match {
    case other: Movie => other.canEqual(this) && super.equals(o)
    case _ => false
  }
}

object Movie {

  def apply(originalTitle: LocalizedText,
            localizedTitle: LocalizedText = null,
            genres: Set[Genre] = Set.empty,
            runtime: ReadableDuration = null,
            dateOfRelease: LocalDate = null,
            version: Long = 0,
            id: UUID = null): Movie = Movie(originalTitle, Option(localizedTitle), genres, Option(runtime), Option(dateOfRelease), version, Option(id))
}

case class Person(name: String, gender: Gender, dateOfBirth: LocalDate, placeOfBirth: String, version: Long, id: Option[UUID]) extends Entity {

  Validate.notNull(name)
  Validate.notNull(gender)
  Validate.notNull(dateOfBirth)
  Validate.notNull(placeOfBirth)

  override def equals(o: Any): Boolean = o match {
    case other: Person => other.canEqual(this) && (name == other.name) && (gender == other.gender) && (dateOfBirth == other.dateOfBirth) && (placeOfBirth == other.placeOfBirth)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + name.hashCode
    result = 3 * result + dateOfBirth.hashCode
    result = 3 * result + gender.hashCode
    result
  }
}

object Person {

  def apply(name: String,
            gender: Gender,
            dateOfBirth: LocalDate,
            placeOfBirth: String,
            version: Long = 0,
            id: UUID = null): Person = Person(name, gender, dateOfBirth, placeOfBirth, version, Option(id))
}

sealed trait Crew extends Entity {

  Validate.notNull(person)
  Validate.notNull(motionPicture)

  def person: Person
  def motionPicture: MotionPicture

  override def equals(o: Any): Boolean = o match {
    case other: Crew => other.canEqual(this) && (person == other.person) && (motionPicture == other.motionPicture)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Crew]

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + person.hashCode
    result = 3 * result + motionPicture.hashCode
    result
  }
}

sealed trait Cast extends Crew {

  Validate.notNull(character)

  def character: Character

  override def equals(o: Any): Boolean = o match {
    case other: Cast => other.canEqual(this) && super.equals(o) && character == other.character
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Cast]

  override def hashCode: Int = 2 * super.hashCode + character.hashCode
}

case class Actor(person: Person, character: Character, motionPicture: MotionPicture, version: Long, id: Option[UUID]) extends Cast {

  Validate.valid(person.gender == Male)
}

object Actor {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: UUID = null): Actor = Actor(person, character, motionPicture, version, Option(id))
}

case class Actress(person: Person, character: Character, motionPicture: MotionPicture, version: Long, id: Option[UUID]) extends Cast {

  Validate.valid(person.gender == Female)
}

object Actress {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: UUID = null): Actress = Actress(person, character, motionPicture, version, Option(id))
}

case class Soundtrack(languageCode: String, formatCode: String, languageName: Option[LocalizedText], formatName: Option[LocalizedText], version: Long, id: Option[UUID]) extends Entity {

  Validate.notBlank(languageCode)
  Validate.notBlank(formatCode)
  Validate.notNull(languageName)
  Validate.notNull(formatName)
  if (languageName.isDefined) Validate.notBlank(languageName.get.text)
  if (formatName.isDefined) Validate.notBlank(formatName.get.text)
  if (languageName.isDefined && formatName.isDefined) Validate.valid(languageName.get.locale == formatName.get.locale)

  override def equals(o: Any): Boolean = o match {
    case other: Soundtrack => other.canEqual(this) && (languageCode == other.languageCode) && (formatCode == other.formatCode)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + languageCode.hashCode
    result = 3 * result + formatCode.hashCode
    result
  }
}

object Soundtrack {

  def apply(languageCode: String,
            formatCode: String,
            languageName: LocalizedText = null,
            formatName: LocalizedText = null,
            version: Long = 0,
            id: UUID = null): Soundtrack = Soundtrack(languageCode, formatCode, Option(languageName), Option(formatName), version, Option(id))
}

case class Subtitle(languageCode: String, languageName: Option[LocalizedText], version: Long, id: Option[UUID]) extends Entity {

  Validate.notBlank(languageCode)
  Validate.notNull(languageName)
  if (languageName.isDefined) Validate.notBlank(languageName.get.text)

  override def equals(o: Any): Boolean = o match {
    case other: Subtitle => other.canEqual(this) && languageCode == other.languageCode
    case _ => false
  }

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + languageCode.hashCode
    result
  }
}

object Subtitle {

  def apply(languageCode: String,
            languageName: LocalizedText = null,
            version: Long = 0,
            id: UUID = null): Subtitle = Subtitle(languageCode, Option(languageName), version, Option(id))
}

case class DigitalContainer(motionPicture: MotionPicture, soundtracks: Set[Soundtrack], subtitles: Set[Subtitle], version: Long, id: Option[UUID]) extends Entity {

  Validate.notNull(motionPicture)
  Validate.noNullElements(soundtracks)
  Validate.noNullElements(subtitles)

  override def equals(o: Any): Boolean = o match {
    case other: DigitalContainer => other.canEqual(this) && (motionPicture == other.motionPicture) && (soundtracks == other.soundtracks) && (subtitles == other.subtitles)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + motionPicture.hashCode
    result = 3 * result + soundtracks.hashCode
    result = 3 * result + subtitles.hashCode
    result
  }
}

object DigitalContainer {

  def apply(motionPicture: MotionPicture,
            soundtracks: Set[Soundtrack] = Set.empty,
            subtitles: Set[Subtitle] = Set.empty,
            version: Long = 0,
            id: UUID = null):DigitalContainer = DigitalContainer(motionPicture, soundtracks, subtitles, version, Option(id))
}
