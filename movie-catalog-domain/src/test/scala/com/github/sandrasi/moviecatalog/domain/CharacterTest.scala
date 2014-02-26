package com.github.sandrasi.moviecatalog.domain

import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CharacterTest extends FunSuite with Matchers {

  test("should create character with specified name and default attributes") {
    val subject = Character("Vincent Vega")
    subject.name should be("Vincent Vega")
    subject.creator should be(None)
    subject.creationDate should be(None)
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should create character with specified creator") {
    val subject = Character("Vincent Vega", creator = "Quentin Tarantino")
    subject.creator should be(Some("Quentin Tarantino"))
  }

  test("should create character with specified creation date") {
    val subject = Character("Vincent Vega", creationDate = new LocalDate(1994, 10, 14))
    subject.creationDate should be(Some(new LocalDate(1994, 10, 14)))
  }
  
  test("should not create character with null name") {
    intercept[IllegalArgumentException] {
      Character(null, "Quentin Tarantino", new LocalDate(1994, 10, 14))
    }
  }

  test("should not create character with blank name") {
    intercept[IllegalArgumentException] {
      Character("  ", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    }
  }

  test("should not create character with null creator") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", null, Some(new LocalDate(1994, 10, 14)), 0, None)
    }
  }

  test("should not create character with blank creator") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", Some("  "), Some(new LocalDate(1994, 10, 14)), 0, None)
    }
  }

  test("should not create character with null creationDate") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", Some("Quentin Tarantino"), null, 0, None)
    }
  }

  test("should not create character with negative version") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", version = -1)
    }
  }

  test("should not create character with null id") {
    intercept[IllegalArgumentException] {
      Character("Vincent Vega", Some("Quentin Tarantino"), Some(new LocalDate(1994, 10, 14)), 0, null)
    }
  }

  test("should compare two objects for equality") {
    val character = Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    val otherCharacter = character.copy()
    val otherCharacterWithDifferentName = character.copy(name = "Mia Wallace")
    val otherCharacterWithDifferentCreator = character.copy(creator = Some("Robert Rodriguez"))
    val otherCharacterWithDifferentCreationDate = character.copy(creationDate = Some(new LocalDate(2000, 1, 1)))

    character should not equal null
    character should not equal new AnyRef
    character should not equal otherCharacterWithDifferentName
    character should not equal otherCharacterWithDifferentCreator
    character should not equal otherCharacterWithDifferentDateOfCreation
    character should equal(character)
    character should equal(otherCharacter)
  }

  test("should calculate hash code") {
    val character = Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    val otherCharacter = character.copy()

    character.hashCode should equal(otherCharacter.hashCode)
  }
}
