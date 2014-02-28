package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import org.scalatest.{FunSuite, Matchers}

class CharacterRelationshipTypeTest extends FunSuite with Matchers {

  test("should list all character relationship type values") {
    CharacterRelationshipType.values should be(List(Played, AppearedIn))
  }

  test("should create character relationship type values from string matching enum names") {
    CharacterRelationshipType.valueOf("Played") should be(Played)
    CharacterRelationshipType.valueOf("AppearedIn") should be(AppearedIn)
  }

  test("should not create character relationship type value from string not matching any of the enum names") {
    intercept[NoSuchElementException] { CharacterRelationshipType.valueOf("does not match") }
  }

  test("should retrieve the name of the character relationship type") {
    Played.name should be("Played")
    AppearedIn.name should be("AppearedIn")
  }
}
