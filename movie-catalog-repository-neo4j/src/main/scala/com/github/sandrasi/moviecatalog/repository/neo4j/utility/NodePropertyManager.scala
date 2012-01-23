package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import java.util.Locale
import org.neo4j.graphdb.Node
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import org.joda.time.{Duration, LocalDate, ReadableDuration}

private[utility] object NodePropertyManager extends MovieCatalogGraphPropertyNames {
  
  def getString(node: Node, key: String): String = node.getProperty(key).asInstanceOf[String]
  
  def setString(node: Node, key: String, str: String) = { node.setProperty(key, str) }

  def getDuration(node: Node, key: String): ReadableDuration = Duration.millis(node.getProperty(key).asInstanceOf[Long])

  def setDuration(node: Node, key: String, duration: ReadableDuration) { node.setProperty(key, duration.getMillis) }
  
  def getLocalDate(node: Node, key: String): LocalDate = new LocalDate(node.getProperty(key).asInstanceOf[Long])
  
  def setLocalDate(node: Node, key: String, date: LocalDate) = { node.setProperty(key, date.toDateTimeAtStartOfDay.getMillis) }
  
  def hasLocalizedText(node: Node, key: String): Boolean = hasProperty(node, key, classOf[Array[String]]) && hasProperty(node, key + LocaleLanguage, classOf[Array[String]]) && hasProperty(node, key + LocaleCountry, classOf[Array[String]]) && hasProperty(node, key + LocaleVariant, classOf[Array[String]])
  
  def hasLocalizedText(node: Node, key: String, locale: Locale): Boolean = hasLocalizedText(node, key) && getLocalizedTextSet(node, key).find(_.locale == locale) != None
  
  private def hasProperty(node: Node, key: String, propertyType: Class[_]) = node.hasProperty(key) && node.getProperty(key).getClass == propertyType

  def getLocalizedText(node: Node, key: String): LocalizedText = getLocalizedTextSet(node, key).head

  def getLocalizedText(node: Node, key: String, locale: Locale): LocalizedText = getLocalizedTextSet(node, key).view.find(_.locale == locale).get

  def getLocalizedTextSet(node: Node, key: String): Set[LocalizedText] = {
    val text = node.getProperty(key).asInstanceOf[Array[String]]
    val languages = node.getProperty(key + LocaleLanguage).asInstanceOf[Array[String]]
    val countries = node.getProperty(key + LocaleCountry).asInstanceOf[Array[String]]
    val variants = node.getProperty(key + LocaleVariant).asInstanceOf[Array[String]]
    val localizedTextArray = for (i <- 0 until text.length) yield LocalizedText(text(i), new Locale(languages(i), countries(i), variants(i)))
    localizedTextArray.toSet
  }

  def setLocalizedText(node: Node, key: String, localizedText: LocalizedText) { setLocalizedText(node, key, Set(localizedText)) }

  def setLocalizedText(node: Node, key: String, localizedTextSet: Set[LocalizedText]) {
    val lta = localizedTextSet.toArray
    node.setProperty(key, for (lt <- lta) yield lt.text)
    node.setProperty(key + LocaleLanguage, for (lt <- lta) yield lt.locale.getLanguage)
    node.setProperty(key + LocaleCountry, for (lt <- lta) yield lt.locale.getCountry)
    node.setProperty(key + LocaleVariant, for (lt <- lta) yield lt.locale.getVariant)
  }

  def addOrReplaceLocalizedText(node: Node,  key: String, localizedText: LocalizedText) {
    val lts = if (hasLocalizedText(node, key)) getLocalizedTextSet(node, key).view.filterNot(_.locale == localizedText.locale).toSet else Set[LocalizedText]()
    setLocalizedText(node, key, (lts + localizedText))
  }
  
  def deleteLocalizedText(node: Node, key: String) {
    node.removeProperty(key)
    node.removeProperty(key + LocaleLanguage)
    node.removeProperty(key + LocaleCountry)
    node.removeProperty(key + LocaleVariant)
  }
  
  def deleteLocalizedText(node: Node, key: String, locale: Locale) {
    if (hasLocalizedText(node, key)) {
      val newLts = getLocalizedTextSet(node, key).view.filterNot(_.locale == locale).toSet
      if (newLts.size == 0) deleteLocalizedText(node, key) else setLocalizedText(node, key, newLts)
    }
  }
}
