package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._

class DigitalContainerRelationshipTypeTest extends FunSuite with ShouldMatchers {

  test("should list all digital container relationship type values") {
    DigitalContainerRelationshipType.values should be(List(WithContent, WithSoundtrack, WithSubtitle))
  }

  test("should create digital container relationship type values from string matching enum names") {
    DigitalContainerRelationshipType.valueOf("WithContent") should be(WithContent)
    DigitalContainerRelationshipType.valueOf("WithSoundtrack") should be(WithSoundtrack)
    DigitalContainerRelationshipType.valueOf("WithSubtitle") should be(WithSubtitle)
  }

  test("should not create digital container relationship type value from string not matching any of the enums' name") {
    intercept[NoSuchElementException] {
      DigitalContainerRelationshipType.valueOf("does not match")
    }
  }

  test("should retrieve the name of the digital container relationship type") {
    WithContent.name should be("WithContent")
    WithSoundtrack.name should be("WithSoundtrack")
    WithSubtitle.name should be("WithSubtitle")
  }
}
