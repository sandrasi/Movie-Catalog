package com.github.sandrasi.moviecatalog.domain

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.joda.time.LocalDate
import java.util.UUID

@RunWith(classOf[JUnitRunner])
class CharacterTest extends FunSuite with ShouldMatchers {

  test("should create character with given name, creator and creationDate") {
    val subject = Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    subject.name should be("Vincent Vega")
    subject.creator should be("Quentin Tarantino")
    subject.creationDate should be(new LocalDate(1994, 10, 14))
    subject.version should be(0)
    subject.id should be(None)
  }
  
  test("should not create character with null name") {
    intercept[IllegalArgumentException] {
      Character(null, "Quentin Tarantino", new LocalDate(1994, 10, 14))
    }
  }

  test("should not create character with null creator") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", null, new LocalDate(1994, 10, 14))
    }
  }

  test("should not create character with null creationDate") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", "Quentin Tarantino", null)
    }
  }

  test("should not create character with negative version") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", version = -1)
    }
  }

  test("should not create character with null id") {
    intercept[IllegalArgumentException] {
      new Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14), 0, id = null)
    }
  }

  test("should compare two objects for equality") {
    val character = Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    val otherCharacter = character.copy()
    val otherCharacterWithDifferentName = character.copy(name = "Mia Wallace")
    val otherCharacterWithDifferentCreator = character.copy(creator = "Robert Rodriguez")
    val otherCharacterWithDifferentCreationDate = character.copy(creationDate = new LocalDate(2000, 1, 1))

    character should not equal(null)
    character should not equal(new AnyRef)
    character should not equal(otherCharacterWithDifferentName)
    character should not equal(otherCharacterWithDifferentCreator)
    character should not equal(otherCharacterWithDifferentCreationDate)
    character should equal(character)
    character should equal(otherCharacter)
  }

  test("should calculate hash code") {
    val character = Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    val otherCharacter = character.copy()

    character.hashCode should equal(otherCharacter.hashCode)
  }
}
