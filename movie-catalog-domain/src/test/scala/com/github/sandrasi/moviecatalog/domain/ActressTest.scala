package com.github.sandrasi.moviecatalog.domain

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

@RunWith(classOf[JUnitRunner])
class ActressTest extends FunSuite with Matchers {

  private final val UmaThurman = Person("John Joseph Travolta", Female, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
  private final val MiaWallace = Character("Vincent Vega")
  private final val PulpFiction = Movie("Pulp fiction")

  test("should create actress with specified person, character and motion picture") {
    val subject = Actress(UmaThurman, MiaWallace, PulpFiction)
    subject.person should be(UmaThurman)
    subject.character should be(MiaWallace)
    subject.motionPicture should be(PulpFiction)
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should not create actress with null person") {
    intercept[IllegalArgumentException] {
      Actress(null, MiaWallace, PulpFiction)
    }
  }

  test("should not create actress with null character") {
    intercept[IllegalArgumentException] {
      Actress(UmaThurman, null, PulpFiction)
    }
  }

  test("should not create actress with null motion picture") {
    intercept[IllegalArgumentException] {
      Actress(UmaThurman, MiaWallace, null)
    }
  }

  test("should not create actress with negative version") {
    intercept[IllegalArgumentException] {
      Actress(UmaThurman, MiaWallace, PulpFiction, version = -1)
    }
  }

  test("should not create actress with null id") {
    intercept[IllegalArgumentException] {
      new Actress(UmaThurman, MiaWallace, PulpFiction, 0, id = null)
    }
  }

  test("should not create male actress") {
    intercept[IllegalArgumentException] {
      Actress(Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S."), Character("Vincent Vega"), Movie("Pulp fiction"))
    }
  }

  test("should compare two objects for equality") {
    val actress = Actress(UmaThurman, MiaWallace, PulpFiction)
    val otherActress = actress.copy()
    val otherActressWithDifferentPerson = actress.copy(person = Person("Amanda Plummer", Female, new LocalDate(1957, 3, 23), "New York City, New York, U.S."))
    val otherActressWithDifferentCharacter = actress.copy(character = Character("Yolanda"))
    val otherActressWithDifferentMotionPicture = actress.copy(motionPicture = Movie("Die hard: With a vengeance"))

    actress should not equal(null)
    actress should not equal(new AnyRef)
    actress should not equal(otherActressWithDifferentPerson)
    actress should not equal(otherActressWithDifferentCharacter)
    actress should not equals(otherActressWithDifferentMotionPicture)
    actress should equal(actress)
    actress should equal(otherActress)
  }

  test("should calculate hash code") {
    val actress = Actress(UmaThurman, MiaWallace, PulpFiction)
    val otherActress = actress.copy()

    actress.hashCode should equal(otherActress.hashCode)
  }
}
