package com.github.sandrasi.moviecatalog.domain.entities.container

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

@RunWith(classOf[JUnitRunner])
class SoundtrackTest extends FunSuite with ShouldMatchers {

  test("should create soundtrack with specified language and format codes and without language and format names") {
    val subject = Soundtrack("en", "DD 5.1")
    subject.languageCode should be("en")
    subject.formatCode should be("DD 5.1")
    subject.languageName should be(None)
    subject.formatName should be(None)
    subject.id should be(None)
  }
  
  test("should create soundtrack with specified language name") {
    val subject = Soundtrack("en", "DD 5.1", languageName = Some(LocalizedText("English")))
    subject.languageName should be(Some(LocalizedText("English")))
  }

  test("should create soundtrack with specified format name") {
    val subject = Soundtrack("en", "DD 5.1", formatName = Some(LocalizedText("Dolby Digital 5.1")))
    subject.formatName should be(Some(LocalizedText("Dolby Digital 5.1")))
  }

  test("should not create soundtrack with null language code") {
    intercept[IllegalArgumentException] {
      Soundtrack(null, "DD 5.1")
    }
  }

  test("should not create soundtrack with blank language code") {
    intercept[IllegalArgumentException] {
      Soundtrack("  ", "DD 5.1")
    }
  }

  test("should not create soundtrack with null format code") {
    intercept[IllegalArgumentException]{
      Soundtrack("en", null)
    }
  }

  test("should not create soundtrack with blank format code") {
    intercept[IllegalArgumentException]{
      Soundtrack("en", "  ")
    }
  }

  test("should not create soundtrack with null language name") {
    intercept[IllegalArgumentException]{
      Soundtrack("en", "DD 5.1", languageName = null)
    }
  }

  test("should not create soundtrack with null format name") {
    intercept[IllegalArgumentException]{
      Soundtrack("en", "DD 5.1", formatName = null)
    }
  }

  test("should compare two objects for equality") {
    val soundtrack = Soundtrack("en", "DD 5.1")
    val otherSoundtrack = Soundtrack("en", "DD 5.1")
    val otherSoundtrackWithDifferentLanguageCode = Soundtrack("hu", "DD 5.1")
    val otherSoundtrackWithDifferentFormatCode = Soundtrack("en", "DTS")
    val otherSoundtrackWithDifferentLanguageName = Soundtrack("en", "DD 5.1", languageName = Some(LocalizedText("English")))
    val otherSoundtrackWithDifferentFormatName = Soundtrack("en", "DD 5.1", formatName = Some(LocalizedText("Dolby Digital 5.1")))

    soundtrack should not equal(null)
    soundtrack should not equal(new AnyRef)
    soundtrack should not equal(otherSoundtrackWithDifferentLanguageCode)
    soundtrack should not equal(otherSoundtrackWithDifferentFormatCode)
    soundtrack should equal(soundtrack)
    soundtrack should equal(otherSoundtrack)
    soundtrack should equal(otherSoundtrackWithDifferentLanguageName)
    soundtrack should equal(otherSoundtrackWithDifferentFormatName)
  }

  test("should calculate hash code") {
    val soundtrack = Soundtrack("en", "DD 5.1")
    val otherSoundtrack = Soundtrack("en", "DD 5.1")

    soundtrack.hashCode should equal(otherSoundtrack.hashCode)
  }
}
