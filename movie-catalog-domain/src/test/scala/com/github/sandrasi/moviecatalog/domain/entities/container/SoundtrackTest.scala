package com.github.sandrasi.moviecatalog.domain.entities.container

import java.util.Locale
import java.util.Locale.US
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

@RunWith(classOf[JUnitRunner])
class SoundtrackTest extends FunSuite with ShouldMatchers {

  test("should create soundtrack with specified language and format codes and without language and format names") {
    val subject = Soundtrack("en", "dts")
    subject.languageCode should be("en")
    subject.formatCode should be("dts")
    subject.languageName should be(None)
    subject.formatName should be(None)
    subject.version should be(0)
    subject.id should be(None)
  }
  
  test("should create soundtrack with specified language name") {
    val subject = Soundtrack("en", "dts", languageName = "English")
    subject.languageName should be(Some(LocalizedText("English")))
  }

  test("should create soundtrack with specified format name") {
    val subject = Soundtrack("en", "dts", formatName = "DTS")
    subject.formatName should be(Some(LocalizedText("DTS")))
  }

  test("should not create soundtrack with null language code") {
    intercept[IllegalArgumentException] {
      Soundtrack(null, "dts")
    }
  }

  test("should not create soundtrack with blank language code") {
    intercept[IllegalArgumentException] {
      Soundtrack("  ", "dts")
    }
  }

  test("should not create soundtrack with null format code") {
    intercept[IllegalArgumentException] {
      Soundtrack("en", null)
    }
  }

  test("should not create soundtrack with blank format code") {
    intercept[IllegalArgumentException] {
      Soundtrack("en", "  ")
    }
  }

  test("should not create soundtrack with null language name") {
    intercept[IllegalArgumentException] {
      new Soundtrack("en", "dts", null, Some(LocalizedText("DTS")), 0, 0)
    }
  }

  test("should not create soundtrack with null format name") {
    intercept[IllegalArgumentException] {
      new Soundtrack("en", "dts", Some(LocalizedText("English")), null, 0, 0)
    }
  }

  test("should not create soundtrack with language and format names having different locale") {
    intercept[IllegalArgumentException] {
      Soundtrack("en", "dts", LocalizedText("English")(US), LocalizedText("DTS")(new Locale("hu", "HU")))
    }
  }

  test("should compare two objects for equality") {
    val soundtrack = Soundtrack("en", "dts")
    val otherSoundtrack = Soundtrack("en", "dts")
    val otherSoundtrackWithDifferentLanguageCode = Soundtrack("hu", "dts")
    val otherSoundtrackWithDifferentFormatCode = Soundtrack("en", "dd51")
    val otherSoundtrackWithDifferentLanguageName = Soundtrack("en", "dts", languageName = "English")
    val otherSoundtrackWithDifferentFormatName = Soundtrack("en", "dts", formatName = "DTS")

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
    val soundtrack = Soundtrack("en", "dts")
    val otherSoundtrack = Soundtrack("en", "dts")

    soundtrack.hashCode should equal(otherSoundtrack.hashCode)
  }
}
