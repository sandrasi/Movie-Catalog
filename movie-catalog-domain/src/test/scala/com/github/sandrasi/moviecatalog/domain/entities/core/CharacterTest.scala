package com.github.sandrasi.moviecatalog.domain.entities.core

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class CharacterTest extends FunSuite with ShouldMatchers {

  test("should create character with given name and generated discriminator") {
    val subject = Character("Johnny")
    subject.name should be("Johnny")
    subject.discriminator.length should be(36)
    subject.version should be(0)
    subject.id should be(None)
  }
  
  test("should not create character with null name") {
    intercept[IllegalArgumentException] {
      Character(null)
    }
  }

  test("should not create character with null discriminator") {
    intercept[IllegalArgumentException] {
      Character("Johnny", null)
    }
  }

  test("should compare two objects for equality") {
    val character = Character("Johnny", "discriminator")
    val otherCharacter = Character("Johnny", "discriminator")
    val otherCharacterWithDifferentName = Character("Jenny", "discriminator")
    val otherCharacterWithDifferentDiscriminator = Character("Johnny", "other discriminator")

    character should not equal(null)
    character should not equal(new AnyRef)
    character should not equal(otherCharacterWithDifferentName)
    character should not equal(otherCharacterWithDifferentDiscriminator)
    character should equal(character)
    character should equal(otherCharacter)
  }

  test("should calculate hash code") {
    val character = Character("Johnny", "discriminator")
    val otherCharacter = Character("Johnny", "discriminator")

    character.hashCode should equal(otherCharacter.hashCode)
  }
}
