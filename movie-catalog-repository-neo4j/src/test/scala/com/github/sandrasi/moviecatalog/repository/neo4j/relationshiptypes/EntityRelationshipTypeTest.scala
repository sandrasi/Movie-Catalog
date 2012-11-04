package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType._

class EntityRelationshipTypeTest extends FunSuite with ShouldMatchers {

  test("should list all entity relationship type values") {
    EntityRelationshipType.values should be(List(IsA))
  }

  test("should create entity relationship type values from string matching enum names") {
    EntityRelationshipType.valueOf("IsA") should be(IsA)
  }

  test("should not create entity relationship type value from string not matching any of the enums' name") {
    intercept[NoSuchElementException] {
      EntityRelationshipType.valueOf("does not match")
    }
  }

  test("should retrieve the name of the entity relationship type") {
    IsA.name should be("IsA")
  }
}
