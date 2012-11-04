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
  
  test("should not create female actor") {
    intercept[IllegalArgumentException] {
      Actor(Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S."), Character("Mia Wallace"), Movie("Pulp fiction"))
    }
  }
  
  test("should compare two objects for equality") {
    val _person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    val _character = Character("Vincent Vega")
    val _motionPicture = Movie("Pulp fiction")
    val actor = Actor(_person, _character, _motionPicture)
    val cast = new Cast() {
      override def id = None
      override def version = 0l
      override def person = _person
      override def character = _character
      override def motionPicture = _motionPicture
    }
    actor should not equal(cast)
  }
}
