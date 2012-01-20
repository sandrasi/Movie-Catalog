package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

@RunWith(classOf[JUnitRunner])
class ActorTest extends FunSuite with ShouldMatchers {

  test("should not create female actor") {
    val janeDoe = Person("Jane Doe", Female, new LocalDate(1990, 9, 9), "Anyville")
    val jane = Character("Jenny")
    val testMovie = Movie(LocalizedText("Test movie"))

    intercept[IllegalArgumentException] {
      Actor(janeDoe, jane, testMovie)
    }
  }
  
  test("should compare two objects for equality") {
    val johnDoe = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
    val johnny = Character("Johnny")
    val testMovie = Movie(LocalizedText("Test movie"))
    val actor = Actor(johnDoe, johnny, testMovie)
    val cast = new AbstractCast(johnDoe, johnny, testMovie, 0) {}
    actor should not equal(cast)
  }
}
