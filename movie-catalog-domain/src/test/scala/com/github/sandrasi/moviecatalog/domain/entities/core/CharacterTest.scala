package com.github.sandrasi.moviecatalog.domain.entities.core

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class CharacterTest extends FunSuite with ShouldMatchers {

  test("should create character with given name") {
    val subject = Character("Vincent Vega")
    subject.name should be("Vincent Vega")
    subject.version should be(0)
    subject.id should be(None)
  }
  
  test("should not create character with null name") {
    intercept[IllegalArgumentException] {
      Character(null)
    }
  }

  test("should compare two objects for equality") {
    val character = Character("Vincent Vega")
    val otherCharacter = Character("Vincent Vega")
    val otherCharacterWithDifferentName = Character("Mia Wallace")
    val otherCharacterWithDifferentId = Character("Vincent Vega", id = 1)

    character should not equal(null)
    character should not equal(new AnyRef)
    character should not equal(otherCharacterWithDifferentName)
    character should not equal(otherCharacterWithDifferentId)
    character should equal(character)
    character should equal(otherCharacter)
  }

  test("should calculate hash code") {
    val character = Character("Vincent Vega")
    val otherCharacter = Character("Vincent Vega")

    character.hashCode should equal(otherCharacter.hashCode)
  }
}
