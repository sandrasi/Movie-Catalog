package com.github.sandrasi.moviecatalog.service.dtos

import java.util.Locale
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, MotionPicture, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Subtitle, Soundtrack}

sealed trait BaseEntityDto {

  val id: Long
}

sealed trait CastDto extends FilmCrewDto {

  val character: CharacterDto
}

sealed trait FilmCrewDto extends BaseEntityDto {

  val person: PersonDto
  val motionPicture: MotionPictureDto
}

sealed trait MotionPictureDto extends BaseEntityDto {

  val originalTitle: String
  val localizedTitle: Option[String]
  val length: Long
  val releaseDate: String
}

case class ActorDto(id: Long, person: PersonDto, character: CharacterDto, motionPicture: MotionPictureDto) extends CastDto

case class ActressDto(id: Long, person: PersonDto, character: CharacterDto, motionPicture: MotionPictureDto) extends CastDto

case class CharacterDto(id: Long, name: String, discriminator: String) extends BaseEntityDto

case class DigitalContainerDto(id: Long, motionPicture: MotionPictureDto, soundtracks: Set[SoundtrackDto], subtitles: Set[SubtitleDto]) extends BaseEntityDto

case class MovieDto(id: Long, originalTitle: String, localizedTitle: Option[String] = None, length: Long, releaseDate: String) extends MotionPictureDto

case class PersonDto(id: Long, name: String, gender: String, dateOfBirth: String, placeOfBirth: String) extends BaseEntityDto

case class SoundtrackDto(id: Long, languageCode: String, formatCode: String, languageName: Option[String], formatName: Option[String]) extends BaseEntityDto

case class SubtitleDto(id: Long, languageCode: String, languageName: Option[String]) extends BaseEntityDto

object DtoSupport {

  implicit def toCharacterDto(c: Character): CharacterDto = CharacterDto(c.id.get, c.name, c.discriminator)

  implicit def toMotionPictureDto(m: MotionPicture)(implicit l: Locale): MotionPictureDto = m match {
    case movie: Movie => MovieDto(m.id.get, m.originalTitle.text, if (m.localizedTitles.exists(_.locale == l)) Some(m.localizedTitles.filter(_.locale == l).head.text) else None, m.length.getMillis, m.releaseDate.toString)
    case _ => throw new IllegalArgumentException("Unsupported motion picture type: %s".format(m.getClass.getName))
  }

  implicit def toPersonDto(p: Person): PersonDto = PersonDto(p.id.get, p.name, p.gender.toString, p.dateOfBirth.toString, p.placeOfBirth)

  implicit def toActorDto(a: Actor)(implicit l: Locale): ActorDto = ActorDto(a.id.get, a.person, a.character, a.motionPicture)

  implicit def toActressDto(a: Actress)(implicit l: Locale): ActressDto = ActressDto(a.id.get, a.person, a.character, a.motionPicture)

  private implicit def toSoundtrackDtoSet(ss: Set[Soundtrack])(implicit l: Locale) = ss.map(toSoundtrackDto(_))

  private implicit def toSubtitleDtoSet(ss: Set[Subtitle])(implicit l: Locale) = ss.map(toSubtitleDto(_))

  implicit def toDigitalContainerDto(dc: DigitalContainer)(implicit l: Locale): DigitalContainerDto = DigitalContainerDto(dc.id.get, dc.motionPicture, dc.soundtracks, dc.subtitles)

  implicit def toSoundtrackDto(s: Soundtrack)(implicit l: Locale): SoundtrackDto = SoundtrackDto(s.id.get, s.languageCode, s.formatCode, if (s.languageName.isDefined) Some(s.languageName.get.text) else None, if (s.formatName.isDefined) Some(s.formatName.get.text) else None)

  implicit def toSubtitleDto(s: Subtitle)(implicit l: Locale): SubtitleDto = SubtitleDto(s.id.get, s.languageCode, if (s.languageName.isDefined) Some(s.languageName.get.text) else None)
}
