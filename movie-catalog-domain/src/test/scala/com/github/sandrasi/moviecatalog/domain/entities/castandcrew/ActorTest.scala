package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

@RunWith(classOf[JUnitRunner])
class ActorTest extends FunSuite with ShouldMatchers {
  
  test("should create actor with default version") {
    val subject = Actor(Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S."), Character("Vincent Vega"), Movie("Pulp fiction"))
    subject.version should be(0)
  }

  test("should not create female actor") {
    intercept[IllegalArgumentException] {
      Actor(Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S."), Character("Mia Wallace"), Movie("Pulp fiction"))
    }
  }
  
  test("should compare two objects for equality") {
    val person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    val character = Character("Vincent Vega")
    val movie = Movie("Pulp fiction")
    val actor = Actor(person, character, movie)
    val cast = new AbstractCast(person, character, movie, 0, 0) {}
    actor should not equal(cast)
  }
}
