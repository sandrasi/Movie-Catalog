package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import java.util.Locale
import org.neo4j.graphdb.Node
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

private[utility] object LocalizedTextManager extends MovieCatalogGraphPropertyNames {
  
  def hasLocalizedText(node: Node, key: String) = node.hasProperty(key)
  
  def hasLocalizedText(node: Node, key: String, locale: Locale) = hasLocalizedText(node, key) && getLocalizedTextSetFrom(node, key).find(_.locale == locale) != None

  def getLocalizedTextFrom(node: Node, key: String) = getLocalizedTextSetFrom(node, key).head

  def getLocalizedTextFrom(node: Node, key: String, locale: Locale) = getLocalizedTextSetFrom(node, key).view.find(_.locale == locale).get

  def getLocalizedTextSetFrom(node: Node, key: String) = {
    val text = node.getProperty(key).asInstanceOf[Array[String]]
    val languages = node.getProperty(key + LocaleLanguage).asInstanceOf[Array[String]]
    val countries = node.getProperty(key + LocaleCountry).asInstanceOf[Array[String]]
    val variants = node.getProperty(key + LocaleVariant).asInstanceOf[Array[String]]
    val localizedTextArray = for (i <- 0 until text.length) yield LocalizedText(text(i), new Locale(languages(i), countries(i), variants(i)))
    localizedTextArray.toSet
  }
  
  def setLocalizedTextOf(node: Node, key: String, localizedText: LocalizedText) {
    setLocalizedTextOf(node, key, Set(localizedText))
  }

  def setLocalizedTextOf[A <: LocalizedText](node: Node, key: String, localizedTextSet: Set[A]) {
    val lta = localizedTextSet.toArray
    node.setProperty(key, for (lt <- lta) yield lt.text)
    node.setProperty(key + LocaleLanguage, for (lt <- lta) yield lt.locale.getLanguage)
    node.setProperty(key + LocaleCountry, for (lt <- lta) yield lt.locale.getCountry)
    node.setProperty(key + LocaleVariant, for (lt <- lta) yield lt.locale.getVariant)
  }

  def addLocalizedTextTo(node: Node,  key: String, localizedText: LocalizedText) {
    val lts = if (hasLocalizedText(node, key)) getLocalizedTextSetFrom(node, key) else Set()
    setLocalizedTextOf(node, key, (lts + localizedText))
  }
}
