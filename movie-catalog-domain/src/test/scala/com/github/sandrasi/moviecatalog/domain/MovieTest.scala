package com.github.sandrasi.moviecatalog.domain

import com.github.sandrasi.moviecatalog.common.LocalizedText
import java.util.Locale
import java.util.Locale.ITALY
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MovieTest extends FunSuite with Matchers {

  private final val EnglishMovieTitle = LocalizedText("Pulp fiction")
  private final val HungarianMovieTitle = LocalizedText("Ponyvaregény")(new Locale("hu", "HU"))
  private final val ItalianMovieTitle = LocalizedText("Pulp fiction")(Locale.ITALY)
  private final val Crime = Genre("crime", "Crime")
  private final val Thriller = Genre("thriller", "Thriller")

  test("should create movie with specified original title and default attributes") {
    val subject = Movie(EnglishMovieTitle)
    subject.originalTitle should be(EnglishMovieTitle)
    subject.genres should be('empty)
    subject.localizedTitle should be('empty)
    subject.runtime should be(None)
    subject.dateOfRelease should be(None)
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should create movie with specified localized title") {
    Movie(EnglishMovieTitle, localizedTitle = HungarianMovieTitle).localizedTitle should be(Some(HungarianMovieTitle))
  }

  test("should create movie with specified genres") {
    Movie(EnglishMovieTitle, genres = Set(Crime, Thriller)).genres should be(Set(Crime, Thriller))
  }

  test("should create movie with specified runtime") {
    Movie(EnglishMovieTitle, runtime = Duration.standardMinutes(154)).runtime should be(Some(Duration.standardMinutes(154)))
  }

  test("should create movie with specified date of release") {
    Movie(EnglishMovieTitle, dateOfRelease = new LocalDate(1994, 10, 14)).dateOfRelease should be(Some(new LocalDate(1994, 10, 14)))
  }

  test("should not create movie with null original title") {
    intercept[IllegalArgumentException] { Movie(null) }
  }

  test("should not create movie with blank original title") {
    intercept[IllegalArgumentException] { Movie("  ") }
  }

  test("should not create movie with null localized title") {
    intercept[IllegalArgumentException] { Movie(EnglishMovieTitle, null, Set(Crime, Thriller), Some(Duration.standardMinutes(154)), Some(new LocalDate(1994, 10, 14)), 0, None) }
  }

  test("should not create movie with blank localized title") {
    intercept[IllegalArgumentException] { Movie(EnglishMovieTitle, "  ", Set(Crime, Thriller), Duration.standardMinutes(154), new LocalDate(1994, 10, 14)) }
  }

  test("should not create movie with null genres") {
    intercept[IllegalArgumentException] { Movie(EnglishMovieTitle, genres = null) }
  }

  test("should not create movie with null genre") {
    intercept[IllegalArgumentException] { Movie(EnglishMovieTitle, genres = Set(null)) }
  }

  test("should not create movie with null runtime") {
    intercept[IllegalArgumentException] { Movie(EnglishMovieTitle, Some(HungarianMovieTitle), Set(Crime, Thriller), null, Some(new LocalDate(1994, 10, 14)), 0, None) }
  }

  test("should not create movie with null date of release") {
    intercept[IllegalArgumentException] { Movie(EnglishMovieTitle, Some(HungarianMovieTitle), Set(Crime, Thriller), Some(Duration.standardMinutes(154)), null, 0, None) }
  }

  test("should not create movie with negative version") {
    intercept[IllegalArgumentException] { Movie("Pulp fiction", version = -1) }
  }

  test("should not create movie with null id") {
    intercept[IllegalArgumentException] { Movie("Pulp fiction", Some(HungarianMovieTitle), Set(Crime, Thriller), Some(Duration.standardMinutes(154)), Some(new LocalDate(1994, 10, 14)), 0, null) }
  }

  test("should compare two objects for equality") {
    val movie = Movie(EnglishMovieTitle, HungarianMovieTitle, Set(Crime, Thriller), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMovie = movie.copy()
    val otherMovieWithDifferentOriginalTitle = movie.copy(originalTitle = "Die hard: With a vengeance")
    val otherMovieWithDifferentLocalizedTitles = movie.copy(localizedTitle = Some(ItalianMovieTitle))
    val otherMovieWithDifferentGenres = movie.copy(genres = Set(Genre("action", "Action")))
    val otherMovieWithDifferentRuntime = movie.copy(runtime = Some(Duration.standardMinutes(131)))
    val otherMovieWithDifferentDateOfRelease = movie.copy(dateOfRelease = Some(new LocalDate(1995, 5, 19)))

    movie should not equal null
    movie should not equal new AnyRef
    movie should not equal otherMovieWithDifferentOriginalTitle
    movie should equal(otherMovieWithDifferentLocalizedTitles)
    movie should equal(otherMovieWithDifferentGenres)
    movie should equal(otherMovieWithDifferentRuntime)
    movie should not equal otherMovieWithDifferentDateOfRelease
    movie should equal(movie)
    movie should equal(otherMovie)
  }

  test("should calculate hash code") {
    val movie = Movie(EnglishMovieTitle, HungarianMovieTitle, Set(Crime), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val otherMovie = movie.copy()

    movie.hashCode should equal(otherMovie.hashCode)
  }
}
