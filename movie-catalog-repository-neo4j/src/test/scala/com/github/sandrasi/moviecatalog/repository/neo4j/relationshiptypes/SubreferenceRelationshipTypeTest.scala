package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}

@RunWith(classOf[JUnitRunner])
class SubreferenceRelationshipTypeTest extends FunSuite with ShouldMatchers {
  
  test("should return subreference relationship type for the class AbstractCast") {
    val relType = SubreferenceRelationshipType.forClass(classOf[AbstractCast])
    relType.name should be (classOf[AbstractCast].getName)
  }

  test("should return subreference relationship type for the class Actor") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Actor])
    relType.name should be (classOf[Actor].getName)
  }

  test("should return subreference relationship type for the class Actress") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Actress])
    relType.name should be (classOf[Actress].getName)
  }

  test("should return subreference relationship type for the class Character") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Character])
    relType.name should be (classOf[Character].getName)
  }

  test("should return subreference relationship type for the class DigitalContainer") {
    val relType = SubreferenceRelationshipType.forClass(classOf[DigitalContainer])
    relType.name should be (classOf[DigitalContainer].getName)
  }

  test("should return subreference relationship type for the class Movie") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Movie])
    relType.name should be (classOf[Movie].getName)
  }

  test("should return subreference relationship type for the class Person") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Person])
    relType.name should be (classOf[Person].getName)
  }

  test("should return subreference relationship type for the class Soundtrack") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Soundtrack])
    relType.name should be (classOf[Soundtrack].getName)
  }

  test("should return subreference relationship type for the class Subtitle") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Subtitle])
    relType.name should be (classOf[Subtitle].getName)
  }
  
  test("should not return subreference relationship type for an unsupported class") {
    intercept[NoSuchElementException] {
      SubreferenceRelationshipType.forClass(classOf[VersionedLongIdEntity])
    }
  }
}
