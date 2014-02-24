package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CrewRelationshipType._

@RunWith(classOf[JUnitRunner])
class CrewRelationshipTypeTest extends FunSuite with Matchers {

  test("should list all crew relationship type values") {
    CrewRelationshipType.values should be(List(Actor, Actress))
  }

  test("should create crew relationship type values from string matching enum names") {
    CrewRelationshipType.valueOf("Actor") should be(Actor)
    CrewRelationshipType.valueOf("Actress") should be(Actress)
  }

  test("should not create crew relationship type value from string not matching any of the enums' name") {
    intercept[NoSuchElementException] {
      CrewRelationshipType.valueOf("does not match")
    }
  }

  test("should retrieve the name of the crew relationship type") {
    Actor.name should be(classOf[com.github.sandrasi.moviecatalog.domain.Actor].getName)
    Actress.name should be(classOf[com.github.sandrasi.moviecatalog.domain.Actress].getName)
  }

  test("should create crew relationship type from bound classes") {
    CrewRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.Actor]) should be(Actor)
    CrewRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.Actress]) should be(Actress)
  }

  test("should not create crew relationship type from unsupported an class") {
    intercept[NoSuchElementException] {
      CrewRelationshipType.forClass(classOf[Any])
    }
  }
}
