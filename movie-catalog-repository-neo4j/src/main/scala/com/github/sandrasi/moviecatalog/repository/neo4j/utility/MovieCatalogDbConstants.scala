package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}

private[neo4j] trait MovieCatalogDbConstants {

  protected final val ClassAbstractCast = classOf[AbstractCast]
  protected final val ClassActor = classOf[Actor]
  protected final val ClassActress = classOf[Actress]
  protected final val ClassCharacter = classOf[Character]
  protected final val ClassDigitalContainer = classOf[DigitalContainer]
  protected final val ClassMovie = classOf[Movie]
  protected final val ClassPerson = classOf[Person]
  protected final val ClassSoundtrack = classOf[Soundtrack]
  protected final val ClassSubtitle = classOf[Subtitle]

  protected final val CharacterDiscriminator = "discriminator"
  protected final val CharacterName = "name"

  protected final val LocaleCountry = "country"
  protected final val LocaleLanguage = "language"
  protected final val LocaleVariant = "variant"

  protected final val MovieLength = "length"
  protected final val MovieOriginalTitle = "originalTitle"
  protected final val MovieLocalizedTitles = "localizedTitles"
  protected final val MovieReleaseDate = "movieReleaseDate"

  protected final val PersonDateOfBirth = "dateOfBirth"
  protected final val PersonGender = "gender"
  protected final val PersonName = "name"
  protected final val PersonPlaceOfBirth = "placeOfBirth"

  protected final val SoundtrackFormatCode = "formatCode"
  protected final val SoundtrackFormatNames = "formatName"
  protected final val SoundtrackLanguageCode = "languageCode"
  protected final val SoundtrackLanguageNames = "languageName"

  protected final val SubtitleLanguageCode = "languageCode"
  protected final val SubtitleLanguageNames = "languageName"

  protected final val SubreferenceNodeClassName = "className"

  protected final val Version = "version"
}
