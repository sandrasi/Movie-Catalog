package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.MotionPictureRelationshipType._

class MotionPictureRelationshipTypeTest extends FunSuite with ShouldMatchers {

  test("should list all motion picture relationship type values") {
    MotionPictureRelationshipType.values should be(List(HasGenre))
  }

  test("should create motion picture relationship type values from string matching enum names") {
    MotionPictureRelationshipType.valueOf("HasGenre") should be(HasGenre)
  }

  test("should not create motion picture relationship type value from string not matching any of the enums' name") {
    intercept[NoSuchElementException] {
      MotionPictureRelationshipType.valueOf("does not match")
    }
  }

  test("should retrieve the name of the mition picture relationship type") {
    HasGenre.name should be("HasGenre")
  }
}
