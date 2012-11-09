package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.LocalizedTextRelationshipType._

class LocalizedTextRelationshipTypeTest extends FunSuite with ShouldMatchers {

  test("should list all localized text relationship type values") {
    LocalizedTextRelationshipType.values should be(List(Locale))
  }

  test("should create localized text relationship type values from string matching enum names") {
    LocalizedTextRelationshipType.valueOf("Locale") should be(Locale)
  }

  test("should not create localized text relationship type value from string not matching any of the enums' name") {
    intercept[NoSuchElementException] {
      LocalizedTextRelationshipType.valueOf("does not match")
    }
  }

  test("should retrieve the name of the localized text relationship type") {
    Locale.name should be("Locale")
  }
}
