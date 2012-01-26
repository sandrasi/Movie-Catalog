package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

@RunWith(classOf[JUnitRunner])
class ActressTest extends FunSuite with ShouldMatchers {

  test("should not create male actress") {
    val johnDoe = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
    val johnny = Character("Johnny")
    val testMovie = Movie("Test movie")

    intercept[IllegalArgumentException] {
      Actress(johnDoe, johnny, testMovie)
    }
  }

  test("should compare two objects for equality") {
    val janeDoe = Person("Jane Doe", Female, new LocalDate(1990, 9, 9), "Anyville")
    val jenny = Character("Jenny")
    val testMovie = Movie("Test movie")
    val actress = Actress(janeDoe, jenny, testMovie)
    val cast = new AbstractCast(janeDoe, jenny, testMovie, 0) {}
    actress should not equal(cast)
  }
}
