package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Male

@RunWith(classOf[JUnitRunner])
class CrewTest extends FunSuite with ShouldMatchers {

  private final val JohnTravolta = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
  private final val PulpFiction = Movie("Pulp fiction")

  test("should create crew with specified person and motion picture") {
    val subject = TestCrew(JohnTravolta, PulpFiction)
    subject.person should be(JohnTravolta)
    subject.motionPicture should be(PulpFiction)
    subject.id should be(None)
  }

  test("should not create crew with null person") {
    intercept[IllegalArgumentException] {
      TestCrew(null, PulpFiction)
    }
  }

  test("should not create crew with null motion picture") {
    intercept[IllegalArgumentException] {
      TestCrew(JohnTravolta, null)
    }
  }

  test("should compare two objects for equality") {
    val crew = TestCrew(JohnTravolta, PulpFiction)
    val otherCrew = TestCrew(JohnTravolta, PulpFiction)
    val otherCrewWithDifferentPerson = TestCrew(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."), PulpFiction)
    val otherCrewWithDifferentMovie = TestCrew(JohnTravolta, Movie("Die hard: With a vengeance"))

    crew should not equal(null)
    crew should not equal(new AnyRef)
    crew should not equal(otherCrewWithDifferentPerson)
    crew should not equals(otherCrewWithDifferentMovie)
    crew should equal(crew)
    crew should equal(otherCrew)
  }

  test("should calculate hash code") {
    val cast = TestCrew(JohnTravolta, PulpFiction)
    val otherCast = TestCrew(JohnTravolta, PulpFiction)

    cast.hashCode should equal(otherCast.hashCode)
  }
}

private case class TestCrew(person: Person, motionPicture: MotionPicture, version: Long = 0, id: Option[Long] = None) extends Crew
