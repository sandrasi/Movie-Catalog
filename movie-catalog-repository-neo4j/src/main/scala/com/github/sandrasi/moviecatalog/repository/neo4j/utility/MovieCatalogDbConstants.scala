package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import com.github.sandrasi.moviecatalog.domain._

private[neo4j] object MovieCatalogDbConstants {

  final val ClassCast = classOf[Cast]
  final val ClassActor = classOf[Actor]
  final val ClassActress = classOf[Actress]
  final val ClassCharacter = classOf[Character]
  final val ClassDigitalContainer = classOf[DigitalContainer]
  final val ClassGenre = classOf[Genre]
  final val ClassMovie = classOf[Movie]
  final val ClassPerson = classOf[Person]
  final val ClassSoundtrack = classOf[Soundtrack]
  final val ClassSubtitle = classOf[Subtitle]

  final val CastPerson = "person"
  final val CastCharacter = "character"
  final val CastMotionPicture = "motionPicture"

  final val CharacterName = "name"
  final val CharacterCreator = "creator"
  final val CharacterDateOfCreation = "dateOfCreation"

  final val DigitalContainerMotionPicture = "motionPicture"
  final val DigitalContainerSoundtrack = "soundtrack"
  final val DigitalContainerSubtitle = "subtitle"

  final val LocaleCountry = "country"
  final val LocaleLanguage = "language"
  final val LocaleVariant = "variant"

  final val GenreCode = "code"
  final val GenreName = "name"

  final val MovieGenres = "genres"
  final val MovieLocalizedTitle = "localizedTitle"
  final val MovieOriginalTitle = "originalTitle"
  final val MovieReleaseDate = "releaseDate"
  final val MovieRuntime = "runtime"

  final val PersonDateOfBirth = "dateOfBirth"
  final val PersonGender = "gender"
  final val PersonName = "name"
  final val PersonPlaceOfBirth = "placeOfBirth"

  final val SoundtrackFormatCode = "formatCode"
  final val SoundtrackFormatName = "formatName"
  final val SoundtrackLanguageCode = "languageCode"
  final val SoundtrackLanguageName = "languageName"

  final val SubtitleLanguageCode = "languageCode"
  final val SubtitleLanguageName = "languageName"

  final val SubreferenceNodeClassName = "className"

  final val Uuid = "uuid"

  final val Version = "version"
}
