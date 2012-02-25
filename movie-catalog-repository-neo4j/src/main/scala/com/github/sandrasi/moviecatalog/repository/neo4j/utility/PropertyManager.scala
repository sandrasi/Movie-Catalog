package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import java.util.Locale
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import org.joda.time.{Duration, LocalDate, ReadableDuration}
import org.neo4j.graphdb.PropertyContainer

private[utility] object PropertyManager extends MovieCatalogDbConstants {
  
  def getString(propCntnr: PropertyContainer, key: String): String = propCntnr.getProperty(key).asInstanceOf[String]
  
  def setString(propCntnr: PropertyContainer, key: String, str: String) { propCntnr.setProperty(key, str) }

  def hasLong(propCntnr: PropertyContainer, key: String): Boolean = hasProperty(propCntnr, key, classOf[java.lang.Long])

  def getLong(propCntnr: PropertyContainer, key: String): Long = propCntnr.getProperty(key).asInstanceOf[Long]

  def setLong(propCntnr: PropertyContainer, key: String, lng: Long) { propCntnr.setProperty(key, lng) }

  def getDuration(propCntnr: PropertyContainer, key: String): ReadableDuration = Duration.millis(propCntnr.getProperty(key).asInstanceOf[Long])

  def setDuration(propCntnr: PropertyContainer, key: String, duration: ReadableDuration) { propCntnr.setProperty(key, duration.getMillis) }
  
  def getLocalDate(propCntnr: PropertyContainer, key: String): LocalDate = new LocalDate(propCntnr.getProperty(key).asInstanceOf[Long])
  
  def setLocalDate(propCntnr: PropertyContainer, key: String, date: LocalDate) { propCntnr.setProperty(key, date.toDateTimeAtStartOfDay.getMillis) }
  
  def hasLocalizedText(propCntnr: PropertyContainer, key: String): Boolean = hasProperty(propCntnr, key, classOf[Array[String]]) && hasProperty(propCntnr, key + LocaleLanguage, classOf[Array[String]]) && hasProperty(propCntnr, key + LocaleCountry, classOf[Array[String]]) && hasProperty(propCntnr, key + LocaleVariant, classOf[Array[String]])
  
  def hasLocalizedText(propCntnr: PropertyContainer, key: String, locale: Locale): Boolean = hasLocalizedText(propCntnr, key) && getLocalizedTextSet(propCntnr, key).find(_.locale == locale) != None
  
  private def hasProperty(propCntnr: PropertyContainer, key: String, propertyType: Class[_]) = propCntnr.hasProperty(key) && propCntnr.getProperty(key).getClass == propertyType

  def getLocalizedText(propCntnr: PropertyContainer, key: String): LocalizedText = getLocalizedTextSet(propCntnr, key).head

  def getLocalizedText(propCntnr: PropertyContainer, key: String, locale: Locale): LocalizedText = getLocalizedTextSet(propCntnr, key).view.find(_.locale == locale).get

  def getLocalizedTextSet(propCntnr: PropertyContainer, key: String): Set[LocalizedText] = {
    val text = propCntnr.getProperty(key).asInstanceOf[Array[String]]
    val languages = propCntnr.getProperty(key + LocaleLanguage).asInstanceOf[Array[String]]
    val countries = propCntnr.getProperty(key + LocaleCountry).asInstanceOf[Array[String]]
    val variants = propCntnr.getProperty(key + LocaleVariant).asInstanceOf[Array[String]]
    val localizedTextArray = for (i <- 0 until text.length) yield LocalizedText(text(i))(new Locale(languages(i), countries(i), variants(i)))
    localizedTextArray.toSet
  }

  def setLocalizedText(propCntnr: PropertyContainer, key: String, localizedText: LocalizedText) { setLocalizedText(propCntnr, key, Set(localizedText)) }

  def setLocalizedText(propCntnr: PropertyContainer, key: String, localizedTextSet: Set[LocalizedText]) {
    val lta = localizedTextSet.toArray
    propCntnr.setProperty(key, for (lt <- lta) yield lt.text)
    propCntnr.setProperty(key + LocaleLanguage, for (lt <- lta) yield lt.locale.getLanguage)
    propCntnr.setProperty(key + LocaleCountry, for (lt <- lta) yield lt.locale.getCountry)
    propCntnr.setProperty(key + LocaleVariant, for (lt <- lta) yield lt.locale.getVariant)
  }

  def addOrReplaceLocalizedText(propCntnr: PropertyContainer,  key: String, localizedText: LocalizedText) {
    val lts = if (hasLocalizedText(propCntnr, key)) getLocalizedTextSet(propCntnr, key).view.filterNot(_.locale == localizedText.locale).toSet else Set[LocalizedText]()
    setLocalizedText(propCntnr, key, (lts + localizedText))
  }
  
  def deleteLocalizedText(propCntnr: PropertyContainer, key: String) {
    propCntnr.removeProperty(key)
    propCntnr.removeProperty(key + LocaleLanguage)
    propCntnr.removeProperty(key + LocaleCountry)
    propCntnr.removeProperty(key + LocaleVariant)
  }
  
  def deleteLocalizedText(propCntnr: PropertyContainer, key: String, locale: Locale) {
    if (hasLocalizedText(propCntnr, key)) {
      val newLts = getLocalizedTextSet(propCntnr, key).view.filterNot(_.locale == locale).toSet
      if (newLts.size == 0) deleteLocalizedText(propCntnr, key) else setLocalizedText(propCntnr, key, newLts)
    }
  }
}
