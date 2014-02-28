package com.github.sandrasi.moviecatalog.domain

import com.github.sandrasi.moviecatalog.common.LocalizedText
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SubtitleTest extends FunSuite with Matchers {

  test("should create subtitle with specified language code and without language name") {
    val subject = Subtitle("en")
    subject.languageCode should be("en")
    subject.languageName should be(None)
    subject.version should be(0)
    subject.id should be(None)
  }
  
  test("should create subtitle with specified language name") {
    val subject = Subtitle("en", "English")
    subject.languageName should be(Some(LocalizedText("English")))
  }
  
  test("should not create subtitle with null language code") {
    intercept[IllegalArgumentException] { Subtitle(null) }
  }

  test("should not create subtitle with blank language code") {
    intercept[IllegalArgumentException] { Subtitle("  ") }
  }

  test("should not create subtitle with null language name") {
    intercept[IllegalArgumentException] { new Subtitle("en", null, 0, None) }
  }

  test("should not create subtitle with blank language name") {
    intercept[IllegalArgumentException] { Subtitle("en", languageName = "  ") }
  }

  test("should not create subtitle with negative version") {
    intercept[IllegalArgumentException] { Subtitle("en", version = -1) }
  }

  test("should not create subtitle with null id") {
    intercept[IllegalArgumentException] { new Subtitle("en", Some("English"), 0, id = null) }
  }

  test("should compare two objects for equality") {
    val subtitle = Subtitle("en")
    val otherSubtitle = subtitle.copy()
    val otherSubtitleWithDifferentLanguageCode = subtitle.copy(languageCode = "hu")
    val otherSubtitleWithDifferentLanguageName = subtitle.copy(languageName = Some("English"))

    subtitle should not equal null
    subtitle should not equal new AnyRef
    subtitle should not equal otherSubtitleWithDifferentLanguageCode
    subtitle should equal(subtitle)
    subtitle should equal(otherSubtitle)
    subtitle should equal(otherSubtitleWithDifferentLanguageName)
  }

  test("should calculate hash code") {
    val subtitle = Subtitle("en")
    val otherSubtitle = subtitle.copy()

    subtitle.hashCode should equal(otherSubtitle.hashCode)
  }
}
