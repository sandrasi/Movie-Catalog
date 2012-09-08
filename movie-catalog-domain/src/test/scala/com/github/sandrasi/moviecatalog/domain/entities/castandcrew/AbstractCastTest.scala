package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Male

@RunWith(classOf[JUnitRunner])
class AbstractCastTest extends FunSuite with ShouldMatchers {
  
  private final val JohnTravolta = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
  private final val VincentVega = Character("Vincent Vega")
  private final val PulpFiction = Movie("Pulp fiction")

  test("should create abstract cast with specified person, character and motion picture") {
    val subject = TestCast(JohnTravolta, VincentVega, PulpFiction)
    subject.person should be(JohnTravolta)
    subject.character should be(VincentVega)
    subject.motionPicture should be(PulpFiction)
    subject.id should be(None)
  }
  
  test("should not create abstract cast with null person") {
    intercept[IllegalArgumentException] {
      TestCast(null, VincentVega, PulpFiction)
    }
  }

  test("should not create abstract cast with null character") {
    intercept[IllegalArgumentException] {
      TestCast(JohnTravolta, null, PulpFiction)
    }
  }
  
  test("should not create abstract cast with null motion picture") {
    intercept[IllegalArgumentException] {
      TestCast(JohnTravolta, VincentVega, null)
    }
  }
  
  test("should compare two objects for equality") {
    val cast = TestCast(JohnTravolta, VincentVega, PulpFiction)
    val otherCast = TestCast(JohnTravolta, VincentVega, PulpFiction)
    val otherCastWithDifferentPerson = TestCast(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."), VincentVega, PulpFiction)
    val otherCastWithDifferentCharacter = TestCast(JohnTravolta, Character("Jules Winnfield"), PulpFiction)
    val otherCastWithDifferentMovie = TestCast(JohnTravolta, VincentVega, Movie("Die hard: With a vengeance"))

    cast should not equal(null)
    cast should not equal(new AnyRef)
    cast should not equal(otherCastWithDifferentPerson)
    cast should not equal(otherCastWithDifferentCharacter)
    cast should not equals(otherCastWithDifferentMovie)
    cast should equal(cast)
    cast should equal(otherCast)
  }
  
  test("should calculate hash code") {
    val cast = TestCast(JohnTravolta, VincentVega, PulpFiction)
    val otherCast = TestCast(JohnTravolta, VincentVega, PulpFiction)

    cast.hashCode should equal(otherCast.hashCode)
  }
}

private object TestCast {

  def apply(person: Person, character: Character, movie: Movie) = new AbstractCast(person, character, movie, 0, 0) {}
}
