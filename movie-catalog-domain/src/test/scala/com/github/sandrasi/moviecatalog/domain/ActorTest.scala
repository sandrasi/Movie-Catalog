package com.github.sandrasi.moviecatalog.domain

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

@RunWith(classOf[JUnitRunner])
class ActorTest extends FunSuite with ShouldMatchers {

  private final val JohnTravolta = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
  private final val VincentVega = Character("Vincent Vega")
  private final val PulpFiction = Movie("Pulp fiction")

  test("should create actor with specified person, character and motion picture") {
    val subject = Actor(JohnTravolta, VincentVega, PulpFiction)
    subject.person should be(JohnTravolta)
    subject.character should be(VincentVega)
    subject.motionPicture should be(PulpFiction)
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should not create actor with null person") {
    intercept[IllegalArgumentException] {
      Actor(null, VincentVega, PulpFiction)
    }
  }

  test("should not create actor with null character") {
    intercept[IllegalArgumentException] {
      Actor(JohnTravolta, null, PulpFiction)
    }
  }

  test("should not create actor with null motion picture") {
    intercept[IllegalArgumentException] {
      Actor(JohnTravolta, VincentVega, null)
    }
  }
  
  test("should not create female actor") {
    intercept[IllegalArgumentException] {
      Actor(Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S."), Character("Mia Wallace"), Movie("Pulp fiction"))
    }
  }

  test("should not create actor with negative version") {
    intercept[IllegalArgumentException] {
      Actor(JohnTravolta, VincentVega, PulpFiction, version = -1)
    }
  }

  test("should not create actor with null id version") {
    intercept[IllegalArgumentException] {
      new Actor(JohnTravolta, VincentVega, PulpFiction, 0, id = null)
    }
  }
  
  test("should compare two objects for equality") {
    val actor = Actor(JohnTravolta, VincentVega, PulpFiction)
    val otherActor = Actor(JohnTravolta, VincentVega, PulpFiction)
    val otherActorWithDifferentPerson = Actor(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."), VincentVega, PulpFiction)
    val otherActorWithDifferentCharacter = Actor(JohnTravolta, Character("Jules Winnfield"), PulpFiction)
    val otherActorWithDifferentMotionPicture = Actor(JohnTravolta, VincentVega, Movie("Die hard: With a vengeance"))

    actor should not equal(null)
    actor should not equal(new AnyRef)
    actor should not equal(otherActorWithDifferentPerson)
    actor should not equal(otherActorWithDifferentCharacter)
    actor should not equals(otherActorWithDifferentMotionPicture)
    actor should equal(actor)
    actor should equal(otherActor)
  }

  test("should calculate hash code") {
    val actor = Actor(JohnTravolta, VincentVega, PulpFiction)
    val otherActor = Actor(JohnTravolta, VincentVega, PulpFiction)

    actor.hashCode should equal(otherActor.hashCode)
  }
}
