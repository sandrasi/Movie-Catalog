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

  private final val Hu = new Locale("hu", "HU")
  private final val It = Locale.ITALY
  private final val MovieTitleUs = LocalizedText("Test movie title")
  private final val MovieTitleHu = LocalizedText("Teszt film cím", Hu)
  private final val MovieTitleIt = LocalizedText("Prova film titolo", It)

  test("should create motion picture with given attributes") {
    val subject = TestMotionPicture(MovieTitleUs, Set(MovieTitleHu, MovieTitleIt), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    subject.originalTitle should be(MovieTitleUs)
    subject.localizedTitles should be(Set(MovieTitleHu, MovieTitleIt))
    subject.length should be(Duration.standardMinutes(90))
    subject.releaseDate should be(new LocalDate(2011, 1, 1))
    subject.id should be(None)
  }

  test("should not create motion picture with null original title") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(null, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    }
  }

  test("should not create motion picture with null localized titles") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(MovieTitleUs, null, Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    }
  }

  test("should not create motion picture with null localized title") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(MovieTitleUs, Set(null), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    }
  }

  test("should not create motion picture with null length") {
    intercept[IllegalArgumentException] {
      TestMotionPicture(MovieTitleUs, Set(MovieTitleHu), null, new LocalDate(2011, 1, 1))
    }
  }

  test("should compare two objects for equality") {
    val motionPicture = TestMotionPicture(MovieTitleUs, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    val otherMotionPicture = TestMotionPicture(MovieTitleUs, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    val otherMotionPictureWithDifferentOriginalTitle = TestMotionPicture(MovieTitleIt, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    val otherMotionPictureWithDifferentLocalizedTitles = TestMotionPicture(MovieTitleUs, Set(MovieTitleIt), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    val otherMotionPictureWithDifferentLength = TestMotionPicture(MovieTitleUs, Set(MovieTitleHu), Duration.standardMinutes(120), new LocalDate(2011, 1, 1))
    val otherMotionPictureWithDifferentReleaseDate = TestMotionPicture(MovieTitleUs, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2010, 1, 1))

    motionPicture should not equal(null)
    motionPicture should not equal(new AnyRef)
    motionPicture should not equal(otherMotionPictureWithDifferentOriginalTitle)
    motionPicture should not equal(otherMotionPictureWithDifferentReleaseDate)
    motionPicture should equal(motionPicture)
    motionPicture should equal(otherMotionPicture)
    motionPicture should equal(otherMotionPictureWithDifferentLocalizedTitles)
    motionPicture should equal(otherMotionPictureWithDifferentLength)
  }

  test("should calculate hash code") {
    val motionPicture = TestMotionPicture(MovieTitleUs, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    val otherMotionPicture = TestMotionPicture(MovieTitleUs, Set(MovieTitleIt), Duration.standardMinutes(120), new LocalDate(2011, 1, 1))

    motionPicture.hashCode should equal(otherMotionPicture.hashCode)
  }
}

private object TestMotionPicture {

  def apply(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText],
            length: ReadableDuration,
            releaseDate: LocalDate) = new MotionPicture(originalTitle, localizedTitles, length, releaseDate, 0) {}
}
