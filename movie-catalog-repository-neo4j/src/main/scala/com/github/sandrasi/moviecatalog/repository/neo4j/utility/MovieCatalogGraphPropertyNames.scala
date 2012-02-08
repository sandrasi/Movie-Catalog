package com.github.sandrasi.moviecatalog.repository.neo4j.utility

private[neo4j] trait MovieCatalogGraphPropertyNames {

  final val AppearedInRelationshipCastRelationshipId = "castRelationshipId"
  final val CharacterDiscriminator = "discriminator"
  final val CharacterName = "name"
  final val LocaleCountry = "country"
  final val LocaleLanguage = "language"
  final val LocaleVariant = "variant"
  final val MovieLength = "length"
  final val MovieOriginalTitle = "originalTitle"
  final val MovieLocalizedTitles = "localizedTitles"
  final val MovieReleaseDate = "movieReleaseDate"
  final val PersonDateOfBirth = "dateOfBirth"
  final val PersonGender = "gender"
  final val PersonName = "name"
  final val PersonPlaceOfBirth = "placeOfBirth"
  final val PlayedByRelationshipCastRelationshipId = "castRelationshipId"
  final val SoundtrackFormatCode = "formatCode"
  final val SoundtrackFormatNames = "formatName"
  final val SoundtrackLanguageCode = "languageCode"
  final val SoundtrackLanguageNames = "languageName"
  final val SubtitleLanguageCode = "languageCode"
  final val SubtitleLanguageNames = "languageName"
  final val SubreferenceNodeClassName = "className"
  final val Version = "version"
}
