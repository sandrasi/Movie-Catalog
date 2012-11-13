package com.github.sandrasi.moviecatalog.service.dtos

import java.util.{UUID, Locale}
import java.util.Locale.US
import com.github.sandrasi.moviecatalog.domain._

sealed trait BaseEntityDto {

  def id: Option[String]
}

sealed trait CastDto extends FilmCrewDto {

  def character: CharacterDto
}

sealed trait FilmCrewDto extends BaseEntityDto {

  def person: PersonDto
  def motionPicture: MotionPictureDto
}

sealed trait MotionPictureDto extends BaseEntityDto {

  def originalTitle: String
  def localizedTitle: Option[String]
  def genres: Set[GenreDto]
  def runtime: Long
  def releaseDate: String
}

case class ActorDto(id: Option[String], person: PersonDto, character: CharacterDto, motionPicture: MotionPictureDto) extends CastDto

case class ActressDto(id: Option[String], person: PersonDto, character: CharacterDto, motionPicture: MotionPictureDto) extends CastDto

case class CharacterDto(id: Option[String], name: String, creator: String, creationDate: String) extends BaseEntityDto

case class DigitalContainerDto(id: Option[String], motionPicture: MotionPictureDto, soundtracks: Set[SoundtrackDto], subtitles: Set[SubtitleDto]) extends BaseEntityDto

case class GenreDto(id: Option[String], code: String, name: Option[String]) extends BaseEntityDto

case class MovieDto(id: Option[String], originalTitle: String, localizedTitle: Option[String], genres: Set[GenreDto], runtime: Long, releaseDate: String) extends MotionPictureDto

case class PersonDto(id: Option[String], name: String, gender: String, dateOfBirth: String, placeOfBirth: String) extends BaseEntityDto

case class SoundtrackDto(id: Option[String], languageCode: String, formatCode: String, languageName: Option[String], formatName: Option[String]) extends BaseEntityDto

case class SubtitleDto(id: Option[String], languageCode: String, languageName: Option[String]) extends BaseEntityDto

object DtoSupport {

  private implicit def optionalUuidToOptionalString(id: Option[UUID]) = if (id.isDefined) Some(id.get.toString) else None

  implicit def toActorDto(a: Actor)(implicit l: Locale = US): ActorDto = ActorDto(a.id, a.person, a.character, a.motionPicture)

  implicit def toActressDto(a: Actress)(implicit l: Locale = US): ActressDto = ActressDto(a.id, a.person, a.character, a.motionPicture)

  implicit def toCharacterDto(c: Character): CharacterDto = CharacterDto(c.id, c.name, c.creator, c.creationDate.toString)

  implicit def toDigitalContainerDto(dc: DigitalContainer): DigitalContainerDto = DigitalContainerDto(dc.id, dc.motionPicture, dc.soundtracks.map(toSoundtrackDto(_)), dc.subtitles.map(toSubtitleDto(_)))

  implicit def toGenreDto(g: Genre): GenreDto = GenreDto(g.id, g.code, if (g.name.isDefined) Some(g.name.get.text) else None)

  implicit def toMotionPictureDto(m: MotionPicture)(implicit l: Locale = US): MotionPictureDto = m match {
    case movie: Movie => MovieDto(m.id, m.originalTitle.text, if (m.localizedTitles.exists(_.locale == l)) Some(m.localizedTitles.filter(_.locale == l).head.text) else None, m.genres.map(toGenreDto(_)), m.runtime.getMillis, m.releaseDate.toString)
    case _ => throw new IllegalArgumentException("Unsupported motion picture type: %s".format(m.getClass.getName))
  }

  implicit def toPersonDto(p: Person): PersonDto = PersonDto(p.id, p.name, p.gender.toString, p.dateOfBirth.toString, p.placeOfBirth)

  implicit def toSoundtrackDto(s: Soundtrack): SoundtrackDto = SoundtrackDto(s.id, s.languageCode, s.formatCode, if (s.languageName.isDefined) Some(s.languageName.get.text) else None, if (s.formatName.isDefined) Some(s.formatName.get.text) else None)

  implicit def toSubtitleDto(s: Subtitle): SubtitleDto = SubtitleDto(s.id, s.languageCode, if (s.languageName.isDefined) Some(s.languageName.get.text) else None)
}
