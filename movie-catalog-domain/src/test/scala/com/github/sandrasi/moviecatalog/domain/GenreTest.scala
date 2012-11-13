package com.github.sandrasi.moviecatalog.domain

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.common.LocalizedText
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class GenreTest extends FunSuite with ShouldMatchers {

  test("should create genre with given code and name") {
    val subject = Genre("crime", "Crime")
    subject.code should be("crime")
    subject.name should be(Some(LocalizedText("Crime")))
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should not create genre with null code") {
    intercept[IllegalArgumentException] {
      Genre(null, "Crime")
    }
  }

  test("should not create genre with blank code") {
    intercept[IllegalArgumentException] {
      Genre("  ", "Crime")
    }
  }

  test("should not create genre with null name") {
    intercept[IllegalArgumentException] {
      new Genre("crime", null, 0, None)
    }
  }

  test("should not create genre with blank name") {
    intercept[IllegalArgumentException] {
      Genre("crime", "  ")
    }
  }

  test("should not create genre with negative version") {
    intercept[IllegalArgumentException] {
      Genre("crime", version = -1)
    }
  }

  test("should not create genre with null id") {
    intercept[IllegalArgumentException] {
      new Genre("crime", Some("Crime"), 0, id = null)
    }
  }

  test("should compare two objects for equality") {
    val genre = Genre("crime", "Crime")
    val otherGenre = genre.copy()
    val otherGenreWithDifferentCode = genre.copy(code = "thriller")
    val otherGenreWithDifferentName = genre.copy(name = Some("Thriller"))

    genre should not equal(null)
    genre should not equal(new AnyRef)
    genre should not equal(otherGenreWithDifferentCode)
    genre should equal(otherGenreWithDifferentName)
    genre should equal(genre)
    genre should equal(otherGenre)
  }

  test("should calculate hash code") {
    val genre = Genre("crime", "Crime")
    val otherGenre = genre.copy()

    genre.hashCode should equal(otherGenre.hashCode)
  }
}
