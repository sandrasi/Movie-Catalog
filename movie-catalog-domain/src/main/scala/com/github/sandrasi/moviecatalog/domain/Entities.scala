package com.github.sandrasi.moviecatalog.domain

import org.joda.time.{Duration, ReadableDuration, LocalDate}
import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.utility.Gender
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

sealed trait BaseEntity[A] extends Equals {

  Validate.notNull(id)

  def id: Option[A]

  override def equals(o: Any): Boolean = o match {
    case other: BaseEntity[_] => other.canEqual(this) && id == other.id
    case _ => false
  }

  override def canEqual(o: Any): Boolean = o.isInstanceOf[BaseEntity[_]]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + id.hashCode
    result
  }
}

sealed trait VersionSupport { self: BaseEntity[_] =>

  Validate.isTrue(version >= 0)

  def version: Long
}

sealed trait VersionedLongIdEntity extends BaseEntity[Long] with VersionSupport {

  Validate.isTrue(id.getOrElse(0l) >= 0l)
}

case class Character(name: String, creator: String, creationDate: LocalDate, version: Long, id: Option[Long]) extends VersionedLongIdEntity {

  Validate.notNull(name)
  Validate.notNull(creator)
  Validate.notNull(creationDate)

  override def equals(o: Any): Boolean = o match {
    case other: Character => other.canEqual(this) && (name == other.name) && (creator == other.creator) && (creationDate == other.creationDate)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Character]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + name.hashCode
    result = 5 * result + creator.hashCode
    result = 5 * result + creationDate.hashCode
    result
  }
}

object Character {

  def apply(name: String,
            creator: String = "",
            creationDate: LocalDate = new LocalDate(0),
            version: Long = 0,
            id: Long = 0) = new Character(name, creator, creationDate, version, if (id == 0) None else Some(id))
}

sealed trait MotionPicture extends VersionedLongIdEntity {

  Validate.notNull(originalTitle)
  Validate.noNullElements(localizedTitles)
  Validate.notNull(runtime)
  Validate.notNull(releaseDate)

  def originalTitle: LocalizedText
  def localizedTitles: Set[LocalizedText]
  def runtime: ReadableDuration
  def releaseDate: LocalDate

  override def equals(o: Any): Boolean = o match {
    case other: MotionPicture => other.canEqual(this) && (originalTitle == other.originalTitle) && (releaseDate == other.releaseDate)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[MotionPicture]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + originalTitle.hashCode
    result = 5 * result + releaseDate.hashCode
    result
  }
}

case class Movie(originalTitle: LocalizedText, localizedTitles: Set[LocalizedText], runtime: ReadableDuration, releaseDate: LocalDate, version: Long, id: Option[Long]) extends MotionPicture {

  override def equals(o: Any): Boolean = o match {
    case other: Movie => other.canEqual(this) && super.equals(o)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Movie]
}

object Movie {

  def apply(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText] = Set(),
            runtime: ReadableDuration = Duration.ZERO,
            releaseDate: LocalDate = new LocalDate(0),
            version: Long = 0,
            id: Long = 0) = new Movie(originalTitle, localizedTitles, runtime, releaseDate, version, if (id == 0) None else Some(id))
}

case class Person(name: String, gender: Gender, dateOfBirth: LocalDate, placeOfBirth: String, version: Long, id: Option[Long]) extends VersionedLongIdEntity {

  Validate.notNull(name)
  Validate.notNull(gender)
  Validate.notNull(dateOfBirth)
  Validate.notNull(placeOfBirth)

  override def equals(o: Any): Boolean = o match {
    case other: Person => other.canEqual(this) && (name == other.name) && (gender == other.gender) && (dateOfBirth == other.dateOfBirth) && (placeOfBirth == other.placeOfBirth)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Person]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + name.hashCode
    result = 5 * result + dateOfBirth.hashCode
    result = 5 * result + gender.hashCode
    result
  }
}

object Person {

  def apply(name: String,
            gender: Gender,
            dateOfBirth: LocalDate,
            placeOfBirth: String,
            version: Long = 0,
            id: Long = 0) = new Person(name, gender, dateOfBirth, placeOfBirth, version, if (id == 0) None else Some(id))
}

sealed trait Crew extends VersionedLongIdEntity {

  Validate.notNull(person)
  Validate.notNull(motionPicture)

  def person: Person
  def motionPicture: MotionPicture

  override def equals(o: Any): Boolean = o match {
    case other: Crew => other.canEqual(this) && (person == other.person) && (motionPicture == other.motionPicture)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + person.hashCode
    result = 5 * result + motionPicture.hashCode
    result
  }

  override def canEqual(o: Any) = o.isInstanceOf[Crew]
}

sealed trait Cast extends Crew {

  Validate.notNull(character)

  def character: Character

  override def equals(o: Any): Boolean = o match {
    case other: Cast => other.canEqual(this) && super.equals(o) && character == other.character
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Cast]

  override def hashCode: Int = {
    var result = super.hashCode
    result = 5 * result + character.hashCode
    result
  }
}

case class Actor(person: Person, character: Character, motionPicture: MotionPicture, version: Long, id: Option[Long]) extends Cast {

  Validate.isTrue(person.gender == Male)

  override def equals(o: Any): Boolean = o match {
    case other: Actor => other.canEqual(this) && super.equals(o)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Actor]
}

object Actor {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: Long = 0) = new Actor(person, character, motionPicture, version, if (id == 0) None else Some(id))
}

case class Actress(person: Person, character: Character, motionPicture: MotionPicture, version: Long, id: Option[Long]) extends Cast {

  Validate.isTrue(person.gender == Female)

  override def equals(o: Any): Boolean = o match {
    case other: Actress => other.canEqual(this) && super.equals(o)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Actress]
}

object Actress {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: Long = 0) = new Actress(person, character, motionPicture, version, if (id == 0) None else Some(id))
}

case class Soundtrack(languageCode: String, formatCode: String, languageName: Option[LocalizedText], formatName: Option[LocalizedText], version: Long, id: Option[Long]) extends VersionedLongIdEntity {

  Validate.notBlank(languageCode)
  Validate.notBlank(formatCode)
  Validate.notNull(languageName)
  Validate.notNull(formatName)
  if (languageName.isDefined && formatName.isDefined) Validate.isTrue(languageName.get.locale == formatName.get.locale)

  override def equals(o: Any): Boolean = o match {
    case other: Soundtrack => other.canEqual(this) && (languageCode == other.languageCode) && (formatCode == other.formatCode)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Soundtrack]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + languageCode.hashCode
    result = 5 * result + formatCode.hashCode
    result
  }
}

object Soundtrack {

  def apply(languageCode: String,
            formatCode: String,
            languageName: LocalizedText = null,
            formatName: LocalizedText = null,
            version: Long = 0,
            id: Long = 0) = new Soundtrack(languageCode, formatCode, Option(languageName), Option(formatName), version, if (id == 0) None else Some(id))
}

case class Subtitle(languageCode: String, languageName: Option[LocalizedText], version: Long, id: Option[Long]) extends VersionedLongIdEntity {

  Validate.notBlank(languageCode)
  Validate.notNull(languageName)

  override def equals(o: Any): Boolean = o match {
    case other: Subtitle => other.canEqual(this) && languageCode == other.languageCode
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Subtitle]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + languageCode.hashCode
    result
  }
}

object Subtitle {

  def apply(languageCode: String,
            languageName: LocalizedText = null,
            version: Long = 0,
            id: Long = 0) = new Subtitle(languageCode, Option(languageName), version, if (id == 0) None else Some(id))
}

case class DigitalContainer(motionPicture: MotionPicture, soundtracks: Set[Soundtrack], subtitles: Set[Subtitle], version: Long, id: Option[Long]) extends VersionedLongIdEntity {

  Validate.notNull(motionPicture)
  Validate.noNullElements(soundtracks)
  Validate.noNullElements(subtitles)

  override def equals(o: Any): Boolean = o match {
    case other: DigitalContainer => other.canEqual(this) && (motionPicture == other.motionPicture) && (soundtracks == other.soundtracks) && (subtitles == other.subtitles)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[DigitalContainer]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + motionPicture.hashCode
    result = 5 * result + soundtracks.hashCode
    result = 5 * result + subtitles.hashCode
    result
  }
}

object DigitalContainer {

  def apply(motionPicture: MotionPicture,
            soundtracks: Set[Soundtrack] = Set(),
            subtitles: Set[Subtitle] = Set(),
            version: Long = 0,
            id: Long = 0) = new DigitalContainer(motionPicture, soundtracks, subtitles, version, if (id == 0) None else Some(id))
}