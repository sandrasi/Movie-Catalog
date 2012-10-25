package com.github.sandrasi.moviecatalog.domain.entities.core

import java.util.Locale
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import org.joda.time.{LocalDate, Duration, ReadableDuration}

@RunWith(classOf[JUnitRunner])
class MotionPictureTest extends FunSuite with ShouldMatchers {

  private final val EnglishMovieTitle = LocalizedText("Pulp fiction")
  private final val HungarianMovieTitle = LocalizedText("Ponyvaregény")(new Locale("hu", "HU"))
  private final val ItalianMovieTitle = LocalizedText("Pulp fiction")(Locale.ITALY)

  test("should create motion picture with given attributes") {
    val subject = TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle, ItalianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    subject.originalTitle should be(EnglishMovieTitle)
    subject.localizedTitles should be(Set(HungarianMovieTitle, ItalianMovieTitle))
    subject.runtime should be(Duration.standardMinutes(154))
    subject.releaseDate should be(new LocalDate(1994, 10, 14))
    subject.id should be(None)
  }

  test("should not create motion picture with null original title") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(null, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null localized titles") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(EnglishMovieTitle, null, Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null localized title") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(EnglishMovieTitle, Set(null), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null runtime") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle), null, new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null release date") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), null)
    }
  }

  test("should compare two objects for equality") {
    val motionPicture = TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMotionPicture = TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMotionPictureWithDifferentOriginalTitle = TestMotionPicture("Die hard: With a vengeance", Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMotionPictureWithDifferentLocalizedTitles = TestMotionPicture(EnglishMovieTitle, Set(ItalianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMotionPictureWithDifferentRuntime = TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(1), new LocalDate(1994, 10, 14))
    val otherMotionPictureWithDifferentReleaseDate = TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(2000, 1, 1))

    motionPicture should not equal(null)
    motionPicture should not equal(new AnyRef)
    motionPicture should not equal(otherMotionPictureWithDifferentOriginalTitle)
    motionPicture should not equal(otherMotionPictureWithDifferentReleaseDate)
    motionPicture should equal(motionPicture)
    motionPicture should equal(otherMotionPicture)
    motionPicture should equal(otherMotionPictureWithDifferentLocalizedTitles)
    motionPicture should equal(otherMotionPictureWithDifferentRuntime)
  }

  test("should calculate hash code") {
    val motionPicture = TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMotionPicture = TestMotionPicture(EnglishMovieTitle, Set(ItalianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))

    motionPicture.hashCode should equal(otherMotionPicture.hashCode)
  }

  test("should convert to string") {
    TestMotionPicture(EnglishMovieTitle, Set(HungarianMovieTitle, ItalianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14)).toString should be("""anon$1(id: None, version: 0, originalTitle: "Pulp fiction" [en_US], localizedTitles: Set("Ponyvaregény" [hu_HU], "Pulp fiction" [it_IT]), runtime: PT9240S, releaseDate: 1994-10-14)""")
  }
}

private object TestMotionPicture {

  def apply(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText],
            runtime: ReadableDuration,
            releaseDate: LocalDate) = new MotionPicture(originalTitle, localizedTitles, runtime, releaseDate, 0, 0) {}
}
