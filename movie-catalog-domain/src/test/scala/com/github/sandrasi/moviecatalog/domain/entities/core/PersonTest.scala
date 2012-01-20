package com.github.sandrasi.moviecatalog.domain.entities.core

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import org.joda.time.LocalDate

@RunWith(classOf[JUnitRunner])
class PersonTest extends FunSuite with ShouldMatchers {

  test("should create person with given name and attributes") {
    val subject = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
    subject.name should be("John Doe")
    subject.gender should be(Male)
    subject.dateOfBirth should  be(new LocalDate(1980, 8, 8))
    subject.placeOfBirth should be("Anytown")
    subject.id should be(None)
  }
  
  test("should not create person with null name") {
    intercept[IllegalArgumentException] {
      Person(null, Male, new LocalDate(1980, 8, 8), "Anytown")
    }
  }

  test("should not create person with null gender") {
    intercept[IllegalArgumentException] {
      Person("John Doe", null, new LocalDate(1980, 8, 8), "Anytown")
    }
  }

  test("should not create person with null date of birth") {
    intercept[IllegalArgumentException] {
      Person("John Doe", Male, null, "Anytown")
    }
  }

  test("should not create person with null place of birth") {
    intercept[IllegalArgumentException] {
      Person("John Doe", Male, new LocalDate(1980, 8, 8), null)
    }
  }

  test("should compare two objects for equality") {
    val person = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
    val otherPerson = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
    val otherPersonWithDifferentName = Person("Jane Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
    val otherPersonWithDifferentGender = Person("John Doe", Female, new LocalDate(1980, 8, 8), "Anytown")
    val otherPersonWithDifferentDateOfBirth = Person("John Doe", Male, new LocalDate(1990, 9, 9), "Anytown")
    val otherPersonWithDifferentPlaceOfBirth = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anyville")

    person should not equal(null)
    person should not equal(new AnyRef)
    person should not equal(otherPersonWithDifferentName)
    person should not equal(otherPersonWithDifferentGender)
    person should not equal(otherPersonWithDifferentDateOfBirth)
    person should not equal(otherPersonWithDifferentPlaceOfBirth)
    person should equal(person)
    person should equal(otherPerson)
  }

  test("should calculate hash code") {
    val person = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")
    val otherPerson = Person("John Doe", Male, new LocalDate(1980, 8, 8), "Anytown")

    person.hashCode should equal(otherPerson.hashCode)
  }
}
