package com.github.sandrasi.moviecatalog.common

import java.util.Locale
import java.util.Locale.{ENGLISH, US}
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocalizedTextTest extends FunSuite with Matchers {

  private val HU = new Locale("hu", "HU")

  test("should create localized text with specified text and default locale") {
    val subject = LocalizedText("localized text")
    subject.text should be("localized text")
    subject.locale should be(US)
  }

  test("should create localized text with specified locale") {
    LocalizedText("honosított szöveg")(HU).locale should be(HU)
  }

  test("should create localized text with implicit locale") {
    implicit val hungarianLocale = HU
    LocalizedText("honosított szöveg").locale should be(HU)
  }

  test("should not create localized text with null text") {
    intercept[IllegalArgumentException] { LocalizedText(null) }
  }

  test("should not create localized text with null locale") {
    intercept[IllegalArgumentException]{ LocalizedText("localized text")(null) }
  }

  test("should compare two objects for equality") {
    val localizedText = LocalizedText("localized text")
    val otherLocalizedText = LocalizedText("localized text")
    val otherLocalizedTextWithDifferentText = LocalizedText("different text")
    val otherLocalizedTextWithDifferentLocale = LocalizedText("localized text")(ENGLISH)

    localizedText should not equal null
    localizedText should not equal new AnyRef
    localizedText should not equal otherLocalizedTextWithDifferentText
    localizedText should not equal otherLocalizedTextWithDifferentLocale
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
