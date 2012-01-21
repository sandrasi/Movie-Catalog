package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import java.util.Locale
import org.neo4j.graphdb.Node
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

private[utility] object LocalizedTextManager extends MovieCatalogGraphPropertyNames {
  
  def hasLocalizedText(node: Node, key: String): Boolean = node.hasProperty(key) && node.getProperty(key).isInstanceOf[Array[String]] &&
    node.hasProperty(key + LocaleLanguage) && node.getProperty(key + LocaleLanguage).isInstanceOf[Array[String]] &&
    node.hasProperty(key + LocaleCountry) && node.getProperty(key + LocaleCountry).isInstanceOf[Array[String]] &&
    node.hasProperty(key + LocaleVariant) && node.getProperty(key + LocaleVariant).isInstanceOf[Array[String]]
  
  def hasLocalizedText(node: Node, key: String, locale: Locale): Boolean = hasLocalizedText(node, key) && getLocalizedTextSet(node, key).find(_.locale == locale) != None

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

  def setLocalizedText(node: Node, key: String, localizedText: LocalizedText) {
    setLocalizedText(node, key, Set(localizedText))
  }

  def setLocalizedText(node: Node, key: String, localizedTextSet: Set[LocalizedText]) {
    val lta = localizedTextSet.toArray
    node.setProperty(key, for (lt <- lta) yield lt.text)
    node.setProperty(key + LocaleLanguage, for (lt <- lta) yield lt.locale.getLanguage)
    node.setProperty(key + LocaleCountry, for (lt <- lta) yield lt.locale.getCountry)
    node.setProperty(key + LocaleVariant, for (lt <- lta) yield lt.locale.getVariant)
  }

  def addLocalizedText(node: Node,  key: String, localizedText: LocalizedText) {
    val lts = if (hasLocalizedText(node, key)) getLocalizedTextSet(node, key) else Set[LocalizedText]()
    setLocalizedText(node, key, (lts + localizedText))
  }
}
