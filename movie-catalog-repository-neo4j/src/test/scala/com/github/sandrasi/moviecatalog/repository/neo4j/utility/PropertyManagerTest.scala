package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.joda.time.{Duration, LocalDate}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

class PropertyManagerTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll with ShouldMatchers with MovieCatalogNeo4jSupport {

  test("should get the string property") {
    val node = createNode()
    transaction(db) { node.setProperty("key", "test") }
    PropertyManager.getString(node, "key") should be("test")
  }
  
  test("should set the string property") {
    val node = createNode()
    transaction(db) { PropertyManager.setString(node, "key", "test") }
    node.getProperty("key") should be("test")
  }

  test("should get the long property") {
    val node = createNode()
    transaction(db) { node.setProperty("key", 1L) }
    PropertyManager.getLong(node, "key") should be(1L)
  }

  test("should set the long property") {
    val node = createNode()
    transaction(db) { PropertyManager.setLong(node, "key", 1L) }
    node.getProperty("key") should be(1L)
  }

  test("should get the duration property") {
    val node = createNode()
    transaction(db) { node.setProperty("key", Duration.millis(1).getMillis) }
    PropertyManager.getDuration(node, "key") should be(Duration.millis(1))
  }

  test("should set the duration property") {
    val node = createNode()
    transaction(db) { PropertyManager.setDuration(node, "key", Duration.millis(1)) }
    node.getProperty("key") should be(1)
  }

  test("should get the local date property") {
    val node = createNode()
    val today = new LocalDate
    transaction(db) { node.setProperty("key", today.toDateTimeAtStartOfDay.getMillis) }
    PropertyManager.getLocalDate(node, "key") should be(today)
  }

  test("should set the local date property") {
    val node = createNode()
    val today = new LocalDate
    transaction(db) { PropertyManager.setLocalDate(node, "key", today) }
    node.getProperty("key") should be(today.toDateTimeAtStartOfDay.getMillis)
  }
  
  test("should return true if the property container has the given localized text property") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(true)
  }

  test("should return false if the text is missing from the localized text property") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return false if the text of the localized text property is not a string array") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", "test")
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return false if the locale's language is missing from the localized text property") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return false if the language of the locale of the localized text property is not a string array") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, "en")
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return false if the locale's country is missing from the localized text property") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return false if the country of the locale of the localized text property is not a string array") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, "US")
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return false if the locale's variant is missing from the localized text property") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return false if the variant of the locale of the localized text property is not a string array") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, "")
    }
    PropertyManager.hasLocalizedText(node, "key") should be(false)
  }

  test("should return true if the property container has the given localized text property with the given locale") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key", AmericanLocale) should be(true)
  }

  test("should return false if the property container does not have the given localized text property with the given locale") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.hasLocalizedText(node, "key", HungarianLocale) should be(false)
  }

  test("should get the first localized text property") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    PropertyManager.getLocalizedText(node, "key") should be(LocalizedText("test")(AmericanLocale))
  }

  test("should get the localized text property with the given locale") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test", "teszt"))
      node.setProperty("key" + LocaleLanguage, Array("en", "hu"))
      node.setProperty("key" + LocaleCountry, Array("US", "HU"))
      node.setProperty("key" + LocaleVariant, Array("", ""))
    }
    PropertyManager.getLocalizedText(node, "key", HungarianLocale) should be(LocalizedText("teszt")(HungarianLocale))
  }

  test("should not get the localized text property if it does not match the locale") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
    }
    intercept[NoSuchElementException] {
      PropertyManager.getLocalizedText(node, "key", HungarianLocale)
    }
  }

  test("should get the localized text properties") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test", "teszt"))
      node.setProperty("key" + LocaleLanguage, Array("en", "hu"))
      node.setProperty("key" + LocaleCountry, Array("US", "HU"))
      node.setProperty("key" + LocaleVariant, Array("", ""))
    }
    PropertyManager.getLocalizedTextSet(node, "key") should be(Set(LocalizedText("test")(AmericanLocale), LocalizedText("teszt")(HungarianLocale)))
  }

  test("should set the localized text property") {
    val node = createNode()
    transaction(db) {
      PropertyManager.setLocalizedText(node, "key", LocalizedText("test")(AmericanLocale))
    }
    node.getProperty("key").asInstanceOf[Array[String]] should be(Array("test"))
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should be(Array("en"))
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should be(Array("US"))
    node.getProperty("key" + LocaleVariant).asInstanceOf[Array[String]] should be(Array(""))
  }

  test("should set the localized text property from multiple localized text instances") {
    val node = createNode()
    transaction(db) {
      PropertyManager.setLocalizedText(node, "key", Set(LocalizedText("test")(AmericanLocale), LocalizedText("teszt")(HungarianLocale)))
    }
    node.getProperty("key").asInstanceOf[Array[String]] should have size(2)
    node.getProperty("key").asInstanceOf[Array[String]] should contain("test")
    node.getProperty("key").asInstanceOf[Array[String]] should contain("teszt")
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should have size(2)
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should contain("en")
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should contain("hu")
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should have size(2)
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should contain("US")
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should contain("HU")
    node.getProperty("key" + LocaleVariant).asInstanceOf[Array[String]] should be(Array("", ""))
  }

  test("should add the localized text to the existing localized text properties") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
      PropertyManager.addOrReplaceLocalizedText(node, "key", LocalizedText("teszt")(HungarianLocale))
    }
    node.getProperty("key").asInstanceOf[Array[String]] should have size(2)
    node.getProperty("key").asInstanceOf[Array[String]] should contain("test")
    node.getProperty("key").asInstanceOf[Array[String]] should contain("teszt")
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should have size(2)
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should contain("en")
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should contain("hu")
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should have size(2)
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should contain("US")
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should contain("HU")
    node.getProperty("key" + LocaleVariant).asInstanceOf[Array[String]] should be(Array("", ""))
  }

  test("should replace the localized text with the same locale in the existing localized text properties") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
      PropertyManager.addOrReplaceLocalizedText(node, "key", LocalizedText("other test")(AmericanLocale))
    }
    node.getProperty("key").asInstanceOf[Array[String]] should be(Array("other test"))
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should be(Array("en"))
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should be(Array("US"))
    node.getProperty("key" + LocaleVariant).asInstanceOf[Array[String]] should be(Array(""))
  }
  
  test("should delete the localized text property") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
      PropertyManager.deleteLocalizedText(node, "key")
    }
    node.hasProperty("key") should be(false)
    node.hasProperty("key" + LocaleLanguage) should be(false)
    node.hasProperty("key" + LocaleCountry) should be(false)
    node.hasProperty("key" + LocaleVariant) should be(false)
  }

  test("should delete the localized text property with the given locale") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test", "teszt"))
      node.setProperty("key" + LocaleLanguage, Array("en", "hu"))
      node.setProperty("key" + LocaleCountry, Array("US", "HU"))
      node.setProperty("key" + LocaleVariant, Array("", ""))
      PropertyManager.deleteLocalizedText(node, "key", AmericanLocale)
    }
    node.getProperty("key").asInstanceOf[Array[String]] should be(Array("teszt"))
    node.getProperty("key" + LocaleLanguage).asInstanceOf[Array[String]] should be(Array("hu"))
    node.getProperty("key" + LocaleCountry).asInstanceOf[Array[String]] should be(Array("HU"))
    node.getProperty("key" + LocaleVariant).asInstanceOf[Array[String]] should be(Array(""))
  }
  
  test("should delete the localized text property if the given locale is the only one") {
    val node = createNode()
    transaction(db) {
      node.setProperty("key", Array("test"))
      node.setProperty("key" + LocaleLanguage, Array("en"))
      node.setProperty("key" + LocaleCountry, Array("US"))
      node.setProperty("key" + LocaleVariant, Array(""))
      PropertyManager.deleteLocalizedText(node, "key", AmericanLocale)
    }
    node.hasProperty("key") should be(false)
    node.hasProperty("key" + LocaleLanguage) should be(false)
    node.hasProperty("key" + LocaleCountry) should be(false)
    node.hasProperty("key" + LocaleVariant) should be(false)
  }
}
