package com.github.sandrasi.moviecatalog.domain.entities.common

import java.util.Locale
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class LocalizedTextTest extends FunSuite with ShouldMatchers {

  test("should create localized text with specified text and default locale") {
    val subject = LocalizedText("localized text")
    subject.text should be("localized text")
    subject.locale should be(Locale.US)
  }

  test("should create localized text with specified locale") {
    val subject = new LocalizedText("honosított szöveg", new Locale("hu", "HU"))
    subject.locale should be(new Locale("hu", "HU"))
  }

  test("should not create localized text with null text") {
    intercept[IllegalArgumentException] {
      LocalizedText(null)
    }
  }

  test("should not create localized text with null locale") {
    intercept[IllegalArgumentException]{
      LocalizedText("localized text", null)
    }
  }

  test("should compare two objects for equality") {
    val localizedText = LocalizedText("localized text")
    val otherLocalizedText = LocalizedText("localized text")
    val otherLocalizedTextWithDifferentText = LocalizedText("different text")
    val otherLocalizedTextWithDifferentLocale = LocalizedText("localized text", Locale.ENGLISH)

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
}
