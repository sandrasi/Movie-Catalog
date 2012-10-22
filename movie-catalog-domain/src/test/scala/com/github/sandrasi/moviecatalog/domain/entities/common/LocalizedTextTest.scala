package com.github.sandrasi.moviecatalog.domain.entities.common

import java.util.Locale
import java.util.Locale.ENGLISH
import java.util.Locale.US
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class LocalizedTextTest extends FunSuite with ShouldMatchers {

  test("should create localized text with specified text and default locale") {
    val subject = LocalizedText("localized text")
    subject.text should be("localized text")
    subject.locale should be(US)
  }

  test("should create localized text with specified locale") {
    val subject = new LocalizedText("honosított szöveg")(new Locale("hu", "HU"))
    subject.locale should be(new Locale("hu", "HU"))
  }

  test("should create localized text with implicit locale") {
    implicit val hungarianLocale = new Locale("hu", "HU")
    val subject = LocalizedText("honosított szöveg")
    subject.locale should be(hungarianLocale)
  }

  test("should create localized text from string with default locale using implicit conversion") {
    val subject: LocalizedText = "localized text"
    subject.text should be("localized text")
    subject.locale should be(US)
  }

  test("should create localized text from string with implicit locale using implicit conversion") {
    implicit val hungarianLocale = new Locale("hu", "HU")
    val subject: LocalizedText = "honosított szöveg"
    subject.text should be("honosított szöveg")
    subject.locale should be(hungarianLocale)
  }

  test("should not create localized text with null text") {
    intercept[IllegalArgumentException] {
      LocalizedText(null)
    }
  }

  test("should not create localized text with null locale") {
    intercept[IllegalArgumentException]{
      LocalizedText("localized text")(null)
    }
  }

  test("should compare two objects for equality") {
    val localizedText = LocalizedText("localized text")
    val otherLocalizedText = LocalizedText("localized text")
    val otherLocalizedTextWithDifferentText = LocalizedText("different text")
    val otherLocalizedTextWithDifferentLocale = LocalizedText("localized text")(ENGLISH)

    localizedText should not equal(null)
    localizedText should not equal(new AnyRef)
    localizedText should not equal(otherLocalizedTextWithDifferentText)
    localizedText should not equal(otherLocalizedTextWithDifferentLocale)
    localizedText should equal(localizedText)
    localizedText should equal(otherLocalizedText)
  }

  test("should calculate hash code") {
    val localizedText = LocalizedText("localized text")
    val otherLocalizedText = LocalizedText("localized text")

    localizedText.hashCode should equal(otherLocalizedText.hashCode)
  }

  test("should convert to string") {
    LocalizedText("localized text")(US).toString should be(""""localized text" [en_US]""")
  }
}
