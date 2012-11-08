package com.github.sandrasi.moviecatalog.domain

import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.common.LocalizedText

@RunWith(classOf[JUnitRunner])
class MovieTest extends FunSuite with ShouldMatchers {

  private final val EnglishMovieTitle = LocalizedText("Pulp fiction")
  private final val HungarianMovieTitle = LocalizedText("Ponyvaregény")(new Locale("hu", "HU"))
  private final val ItalianMovieTitle = LocalizedText("Pulp fiction")(Locale.ITALY)

  test("should create movie with given original title and default attributes") {
    val subject = Movie(EnglishMovieTitle)
    subject.originalTitle should be(EnglishMovieTitle)
    subject.localizedTitles should be('empty)
    subject.runtime should be(Duration.ZERO)
    subject.releaseDate should be(new LocalDate(0))
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should create movie with specified localized titles") {
    val subject = Movie(EnglishMovieTitle, localizedTitles = Set(HungarianMovieTitle, ItalianMovieTitle))
    subject.localizedTitles should be(Set(HungarianMovieTitle, ItalianMovieTitle))
  }

  test("should create movie with specified runtime") {
    val subject = Movie(EnglishMovieTitle, runtime = Duration.standardMinutes(154))
    subject.runtime should be(Duration.standardMinutes(154))
  }

  test("should create movie with specified release date") {
    val subject = Movie(EnglishMovieTitle, releaseDate = new LocalDate(1994, 10, 14))
    subject.releaseDate should be(new LocalDate(1994, 10, 14))
  }

  test("should not create motion picture with null original title") {
    intercept[IllegalArgumentException] {
      Movie(null, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null localized titles") {
    intercept[IllegalArgumentException] {
      Movie(EnglishMovieTitle, null, Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null localized title") {
    intercept[IllegalArgumentException] {
      Movie(EnglishMovieTitle, Set(null), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null runtime") {
    intercept[IllegalArgumentException] {
      Movie(EnglishMovieTitle, Set(HungarianMovieTitle), null, new LocalDate(1994, 10, 14))
    }
  }

  test("should not create motion picture with null release date") {
    intercept[IllegalArgumentException] {
      Movie(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), null)
    }
  }

  test("should not create movie with negative version") {
    intercept[IllegalArgumentException] {
      Movie("Pulp fiction", version = -1)
    }
  }

  test("should not create movie with null id") {
    intercept[IllegalArgumentException] {
      new Movie("Pulp fiction", Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14), 0, id = null)
    }
  }

  test("should compare two objects for equality") {
    val movie = Movie(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMovie = Movie(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMovieWithDifferentOriginalTitle = Movie("Die hard: With a vengeance", Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMovieWithDifferentLocalizedTitles = Movie(EnglishMovieTitle, Set(ItalianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMovieWithDifferentRuntime = Movie(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(1), new LocalDate(1994, 10, 14))
    val otherMovieWithDifferentReleaseDate = Movie(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(2000, 1, 1))

    movie should not equal(null)
    movie should not equal(new AnyRef)
    movie should not equal(otherMovieWithDifferentOriginalTitle)
    movie should equal(otherMovieWithDifferentLocalizedTitles)
    movie should equal(otherMovieWithDifferentRuntime)
    movie should not equal(otherMovieWithDifferentReleaseDate)
    movie should equal(movie)
    movie should equal(otherMovie)
  }

  test("should calculate hash code") {
    val movie = Movie(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMovie = Movie(EnglishMovieTitle, Set(ItalianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))

    movie.hashCode should equal(otherMovie.hashCode)
  }
}
