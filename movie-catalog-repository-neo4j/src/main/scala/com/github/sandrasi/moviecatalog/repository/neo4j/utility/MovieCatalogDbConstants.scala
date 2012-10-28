package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}

private[neo4j] object MovieCatalogDbConstants {

  final val ClassAbstractCast = classOf[AbstractCast]
  final val ClassActor = classOf[Actor]
  final val ClassActress = classOf[Actress]
  final val ClassCharacter = classOf[Character]
  final val ClassDigitalContainer = classOf[DigitalContainer]
  final val ClassMovie = classOf[Movie]
  final val ClassPerson = classOf[Person]
  final val ClassSoundtrack = classOf[Soundtrack]
  final val ClassSubtitle = classOf[Subtitle]

  final val CastPerson = "person"
  final val CastCharacter = "character"
  final val CastMotionPicture = "motionPicture"

  final val CharacterName = "name"
  final val CharacterCreator = "creator"
  final val CharacterCreationDate = "creationDate"

  final val DigitalContainerMotionPicture = "motionPicture"
  final val DigitalContainerSoundtrack = "soundtrack"
  final val DigitalContainerSubtitle = "subtitle"

  final val LocaleCountry = "country"
  final val LocaleLanguage = "language"
  final val LocaleVariant = "variant"

  final val MovieRuntime = "runtime"
  final val MovieOriginalTitle = "originalTitle"
  final val MovieLocalizedTitles = "localizedTitles"
  final val MovieReleaseDate = "movieReleaseDate"

  final val PersonDateOfBirth = "dateOfBirth"
  final val PersonGender = "gender"
  final val PersonName = "name"
  final val PersonPlaceOfBirth = "placeOfBirth"

  final val SoundtrackFormatCode = "formatCode"
  final val SoundtrackFormatNames = "formatName"
  final val SoundtrackLanguageCode = "languageCode"
  final val SoundtrackLanguageNames = "languageName"

  final val SubtitleLanguageCode = "languageCode"
  final val SubtitleLanguageNames = "languageName"

  final val SubreferenceNodeClassName = "className"

  final val Version = "version"
}
