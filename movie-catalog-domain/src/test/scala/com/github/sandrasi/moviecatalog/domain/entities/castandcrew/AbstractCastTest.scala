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
class AbstractCastTest extends FunSuite with ShouldMatchers {
  
  private final val JohnDoe = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
  private final val Johnny = Character("Johnny")
  private final val TestMovie = Movie(LocalizedText("Test movie"))

  test("should create abstract cast with specified person, character and motion picture") {
    val subject = TestCast(JohnDoe, Johnny, TestMovie)
    subject.person should be(JohnDoe)
    subject.character should be(Johnny)
    subject.motionPicture should be(TestMovie)
    subject.id should be(None)
  }
  
  test("should not create abstract cast with null person") {
    intercept[IllegalArgumentException] {
      TestCast(null, Johnny, TestMovie)
    }
  }

  test("should not create abstract cast with null character") {
    intercept[IllegalArgumentException] {
      TestCast(JohnDoe, null, TestMovie)
    }
  }
  
  test("should not create abstract cast with null motion picture") {
    intercept[IllegalArgumentException] {
      TestCast(JohnDoe, Johnny, null)
    }
  }
  
  test("should compare two objects for equality") {
    val cast = TestCast(JohnDoe, Johnny, TestMovie)
    val otherCast = TestCast(JohnDoe, Johnny, TestMovie)
    val otherCastWithDifferentPerson = TestCast(Person("Jane Doe", Female, new LocalDate(1990, 9, 9), "Anyville"), Johnny, TestMovie)
    val otherCastWithDifferentCharacter = TestCast(JohnDoe, Character("Jenny"), TestMovie)
    val otherCastWithDifferentMovie = TestCast(JohnDoe, Johnny, Movie(LocalizedText("Other movie")))

    cast should not equal(null)
    cast should not equal(new AnyRef)
    cast should not equal(otherCastWithDifferentPerson)
    cast should not equal(otherCastWithDifferentCharacter)
    cast should not equals(otherCastWithDifferentMovie)
    cast should equal(cast)
    cast should equal(otherCast)
  }
  
  test("should calculate hash code") {
    val cast = TestCast(JohnDoe, Johnny, TestMovie)
    val otherCast = TestCast(JohnDoe, Johnny, TestMovie)

    cast.hashCode should equal(otherCast.hashCode)
  }
}

private object TestCast {

  def apply(person: Person, character: Character, movie: Movie) = new AbstractCast(person, character, movie, 0) {}
}
