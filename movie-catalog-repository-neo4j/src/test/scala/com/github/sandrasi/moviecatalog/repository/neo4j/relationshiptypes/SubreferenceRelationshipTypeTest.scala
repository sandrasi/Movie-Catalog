package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}

@RunWith(classOf[JUnitRunner])
class SubreferenceRelationshipTypeTest extends FunSuite with ShouldMatchers {

  test("should return subreference relationship type for the class Character") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Character])
    relType.name should be (classOf[Character].getName)
  }

  test("should return subreference relationship type for the class Movie") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Movie])
    relType.name should be (classOf[Movie].getName)
  }

  test("should return subreference relationship type for the class Person") {
    val relType = SubreferenceRelationshipType.forClass(classOf[Person])
    relType.name should be (classOf[Person].getName)
  }
  
  test("should not return subreference relationship type for an unsupported class") {
    intercept[NoSuchElementException] {
      SubreferenceRelationshipType.forClass(classOf[LongIdEntity])
    }
  }
}
