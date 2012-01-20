package com.github.sandrasi.moviecatalog.domain.entities.container

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

@RunWith(classOf[JUnitRunner])
class SubtitleTest extends FunSuite with ShouldMatchers {

  test("should create subtitle with specified language code and without language name") {
    val subject = Subtitle("en")
    subject.languageCode should be("en")
    subject.languageName should be(None)
    subject.id should be(None)
  }
  
  test("should create subtitle with specified language name") {
    val subject = Subtitle("en", Some(LocalizedText("English")))
    subject.languageName should be(Some(LocalizedText("English")))
  }
  
  test("should not create subtitle with null language code") {
    intercept[IllegalArgumentException] {
      Subtitle(null)
    }
  }

  test("should not create subtitle with blank language code") {
    intercept[IllegalArgumentException] {
      Subtitle("  ")
    }
  }

  test("should not create subtitle with null language name") {
    intercept[IllegalArgumentException] {
      Subtitle("en", null)
    }
  }

  test("should compare two objects for equality") {
    val subtitle = Subtitle("en")
    val otherSubtitle = Subtitle("en")
    val otherSubtitleWithDifferentLanguageCode = Subtitle("hu")
    val otherSubtitleWithDifferentLanguageName = Subtitle("en", Some(LocalizedText("English")))

    subtitle should not equal(null)
    subtitle should not equal(new AnyRef)
    subtitle should not equal(otherSubtitleWithDifferentLanguageCode)
    subtitle should equal(subtitle)
    subtitle should equal(otherSubtitle)
    subtitle should equal(otherSubtitleWithDifferentLanguageName)
  }

  test("should calculate hash code") {
    val subtitle = Subtitle("en")
    val otherSubtitle = Subtitle("en")

    subtitle.hashCode should equal(otherSubtitle.hashCode)
  }
}
