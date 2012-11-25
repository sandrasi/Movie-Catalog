package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import java.util.{UUID, Locale}
import org.joda.time.{Duration, LocalDate, ReadableDuration}
import org.neo4j.graphdb.PropertyContainer
import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._

private[utility] object PropertyManager {

  def getUuid(propCntnr: PropertyContainer): UUID = UUID.fromString(propCntnr.getProperty(Uuid).asInstanceOf[String])

  def setUuid(propCntnr: PropertyContainer, uuid: UUID) { propCntnr.setProperty(Uuid, uuid.toString) }

  private def hasString(propCntnr: PropertyContainer, key: String): Boolean = hasProperty(propCntnr, key, classOf[String])

  def getString(propCntnr: PropertyContainer, key: String): Option[String] = if (hasString(propCntnr, key)) Some(propCntnr.getProperty(key).asInstanceOf[String]) else None

  def setString(propCntnr: PropertyContainer, key: String, str: String) { propCntnr.setProperty(key, str) }

  def hasLong(propCntnr: PropertyContainer, key: String): Boolean = hasProperty(propCntnr, key, classOf[java.lang.Long])

  def getLong(propCntnr: PropertyContainer, key: String): Long = propCntnr.getProperty(key).asInstanceOf[Long]

  def setLong(propCntnr: PropertyContainer, key: String, lng: Long) { propCntnr.setProperty(key, lng) }

  def getDuration(propCntnr: PropertyContainer, key: String): Option[ReadableDuration] = if (hasLong(propCntnr, key)) Some(Duration.millis(propCntnr.getProperty(key).asInstanceOf[Long])) else None

  def setDuration(propCntnr: PropertyContainer, key: String, duration: ReadableDuration) { propCntnr.setProperty(key, duration.getMillis) }

  def getLocalDate(propCntnr: PropertyContainer, key: String): Option[LocalDate] = if (hasLong(propCntnr, key)) Some(new LocalDate(propCntnr.getProperty(key).asInstanceOf[Long])) else None

  def setLocalDate(propCntnr: PropertyContainer, key: String, date: LocalDate) { propCntnr.setProperty(key, date.toDateTimeAtStartOfDay.getMillis) }

  private def hasLocalizedText(propCntnr: PropertyContainer, key: String, propertyType: Class[_]): Boolean =
    hasProperty(propCntnr, key, propertyType) &&
    hasProperty(propCntnr, key + LocaleLanguage, propertyType) &&
    hasProperty(propCntnr, key + LocaleCountry, propertyType) &&
    hasProperty(propCntnr, key + LocaleVariant, propertyType)

  private def hasLocalizedText(propCntnr: PropertyContainer, key: String): Boolean = hasLocalizedText(propCntnr, key, classOf[String])

  def getLocalizedText(propCntnr: PropertyContainer, key: String): Option[LocalizedText] = if (hasLocalizedText(propCntnr, key)) {
    val text = propCntnr.getProperty(key).asInstanceOf[String]
    val language = propCntnr.getProperty(key + LocaleLanguage).asInstanceOf[String]
    val country = propCntnr.getProperty(key + LocaleCountry).asInstanceOf[String]
    val variant = propCntnr.getProperty(key + LocaleVariant).asInstanceOf[String]
    Some(LocalizedText(text)(new Locale(language, country, variant)))
  } else None

  def setLocalizedText(propCntnr: PropertyContainer, key: String, localizedText: LocalizedText) {
    propCntnr.setProperty(key, localizedText.text)
    propCntnr.setProperty(key + LocaleLanguage, localizedText.locale.getLanguage)
    propCntnr.setProperty(key + LocaleCountry, localizedText.locale.getCountry)
    propCntnr.setProperty(key + LocaleVariant, localizedText.locale.getVariant)
  }

  private def hasLocalizedTextSet(propCntnr: PropertyContainer, key: String): Boolean = hasLocalizedText(propCntnr, key, classOf[Array[String]])

  def getLocalizedText(propCntnr: PropertyContainer, key: String, locale: Locale): Option[LocalizedText] = if (hasLocalizedTextSet(propCntnr, key)) getAllLocalizedText(propCntnr, key).view.find(_.locale == locale) else None

  private def getAllLocalizedText(propCntnr: PropertyContainer, key: String): Set[LocalizedText] = {
    val text = propCntnr.getProperty(key).asInstanceOf[Array[String]]
    val languages = propCntnr.getProperty(key + LocaleLanguage).asInstanceOf[Array[String]]
    val countries = propCntnr.getProperty(key + LocaleCountry).asInstanceOf[Array[String]]
    val variants = propCntnr.getProperty(key + LocaleVariant).asInstanceOf[Array[String]]
    (for (i <- 0 until text.length) yield LocalizedText(text(i))(new Locale(languages(i), countries(i), variants(i)))).toSet
  }

  def addOrReplaceLocalizedText(propCntnr: PropertyContainer, key: String, localizedText: LocalizedText) {
    val lts = if (hasLocalizedTextSet(propCntnr, key)) getAllLocalizedText(propCntnr, key).view.filterNot(_.locale == localizedText.locale).toSet else Set.empty[LocalizedText]
    setLocalizedTextSet(propCntnr, key, (lts + localizedText))
  }

  def deleteLocalizedText(propCntnr: PropertyContainer, key: String) {
    propCntnr.removeProperty(key)
    propCntnr.removeProperty(key + LocaleLanguage)
    propCntnr.removeProperty(key + LocaleCountry)
    propCntnr.removeProperty(key + LocaleVariant)
  }

  def deleteLocalizedText(propCntnr: PropertyContainer, key: String, locale: Locale) {
    if (hasLocalizedTextSet(propCntnr, key)) {
      val newLts = getAllLocalizedText(propCntnr, key).view.filterNot(_.locale == locale).toSet
      if (newLts.isEmpty) deleteLocalizedText(propCntnr, key) else setLocalizedTextSet(propCntnr, key, newLts)
    }
  }

  private def setLocalizedTextSet(propCntnr: PropertyContainer, key: String, localizedTextSet: Set[LocalizedText]) {
    val lta = localizedTextSet.toArray
    propCntnr.setProperty(key, for (lt <- lta) yield lt.text)
    propCntnr.setProperty(key + LocaleLanguage, for (lt <- lta) yield lt.locale.getLanguage)
    propCntnr.setProperty(key + LocaleCountry, for (lt <- lta) yield lt.locale.getCountry)
    propCntnr.setProperty(key + LocaleVariant, for (lt <- lta) yield lt.locale.getVariant)
  }

  def deleteProperty(propCntnr: PropertyContainer, key: String) { propCntnr.removeProperty(key) }

  private def hasProperty(propCntnr: PropertyContainer, key: String, propertyType: Class[_]) = propCntnr.hasProperty(key) && propCntnr.getProperty(key).getClass == propertyType
}
