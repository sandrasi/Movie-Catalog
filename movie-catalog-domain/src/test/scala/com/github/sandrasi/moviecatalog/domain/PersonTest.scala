package com.github.sandrasi.moviecatalog.domain

import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PersonTest extends FunSuite with Matchers {

  test("should create person with given name and attributes") {
    val subject = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    subject.name should be("John Joseph Travolta")
    subject.gender should be(Male)
    subject.dateOfBirth should be(new LocalDate(1954, 2, 18))
    subject.placeOfBirth should be("Englewood, New Jersey, U.S.")
    subject.version should be(0)
    subject.id should be(None)
  }
  
  test("should not create person with null name") {
    intercept[IllegalArgumentException] { Person(null, Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.") }
  }

  test("should not create person with null gender") {
    intercept[IllegalArgumentException] { Person("John Joseph Travolta", null, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.") }
  }

  test("should not create person with null date of birth") {
    intercept[IllegalArgumentException] { Person("John Joseph Travolta", Male, null, "Englewood, New Jersey, U.S.") }
  }

  test("should not create person with null place of birth") {
    intercept[IllegalArgumentException] { Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), null) }
  }

  test("should not create person with negative version") {
    intercept[IllegalArgumentException] { Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.", version = -1) }
  }

  test("should not create person with null id") {
    intercept[IllegalArgumentException] { new Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.", 0, id = null) }
  }

  test("should compare two objects for equality") {
    val person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    val otherPerson = person.copy()
    val otherPersonWithDifferentName = person.copy(name = "Samuel Leroy Jackson")
    val otherPersonWithDifferentGender = person.copy(gender =  Female)
    val otherPersonWithDifferentDateOfBirth = person.copy(dateOfBirth = new LocalDate(1948, 12, 21))
    val otherPersonWithDifferentPlaceOfBirth = person.copy(placeOfBirth = "Washington, D.C., U.S.")

    person should not equal null
    person should not equal new AnyRef
    person should not equal otherPersonWithDifferentName
    person should not equal otherPersonWithDifferentGender
    person should not equal otherPersonWithDifferentDateOfBirth
    person should not equal otherPersonWithDifferentPlaceOfBirth
    person should equal(person)
    person should equal(otherPerson)
  }

  test("should calculate hash code") {
    val person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    val otherPerson = person.copy()

    person.hashCode should equal(otherPerson.hashCode)
  }
}
