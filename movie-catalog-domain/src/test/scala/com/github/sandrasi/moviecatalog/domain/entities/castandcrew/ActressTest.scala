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
    intercept[IllegalArgumentException] {
      Actress(Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S."), Character("Vincent Vega"), Movie("Pulp fiction"))
    }
  }

  test("should compare two objects for equality") {
    val _person = Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.")
    val _character = Character("Mia Wallace")
    val _motionPicture = Movie("Pulp fiction")
    val actress = Actress(_person, _character, _motionPicture)
    val cast = new Cast() {
      override def id = None
      override def version = 0l
      override def person = _person
      override def character = _character
      override def motionPicture = _motionPicture
    }
    println("cast equals actress: " + (cast.equals(actress)))
    println("actress equals actress: " + (actress.equals(cast)))
    actress should not equal(cast)
  }
}
