package com.github.sandrasi.moviecatalog.domain

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.common.LocalizedText

class GenreTest extends FunSuite with ShouldMatchers {

  test("should create genre with given code and name") {
    val subject = Genre("thriller", "Thriller")
    subject.code should be("thriller")
    subject.name should be(Some(LocalizedText("Thriller")))
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should not create genre with null code") {
    intercept[IllegalArgumentException] {
      Genre(null, "Thriller")
    }
  }

  test("should not create genre with blank code") {
    intercept[IllegalArgumentException] {
      Genre("  ", "Thriller")
    }
  }

  test("should not create genre with null name") {
    intercept[IllegalArgumentException] {
      new Genre("thriller", null, 0, None)
    }
  }

  test("should not create genre with blank name") {
    intercept[IllegalArgumentException] {
      Genre("thriller", "  ")
    }
  }

  test("should compare two objects for equality") {
    val genre = Genre("thriller", "Thriller")
    val otherGenre = genre.copy()
    val otherGenreWithDifferentCode = genre.copy(code = "crime")
    val otherGenreWithDifferentName = genre.copy(name = Some("Crime"))

    genre should not equal(null)
    genre should not equal(new AnyRef)
    genre should not equal(otherGenreWithDifferentCode)
    genre should equal(otherGenreWithDifferentName)
    genre should equal(genre)
    genre should equal(otherGenre)
  }

  test("should calculate hash code") {
    val genre = Genre("thriller", "Thriller")
    val otherGenre = genre.copy()

    genre.hashCode should equal(otherGenre.hashCode)
  }
}
