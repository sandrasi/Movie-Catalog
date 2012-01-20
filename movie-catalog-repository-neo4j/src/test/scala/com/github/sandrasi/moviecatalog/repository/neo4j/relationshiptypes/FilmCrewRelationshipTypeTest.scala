package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress, FilmCrew}

@RunWith(classOf[JUnitRunner])
class FilmCrewRelationshipTypeTest extends FunSuite with ShouldMatchers {

  test("should return film crew relationship type for the class Actor") {
    val relType = FilmCrewRelationshipType.forClass(classOf[Actor])
    relType.name should be(classOf[Actor].getName)
  }

  test("should return film crew relationship type for the class Actress") {
    val relType = FilmCrewRelationshipType.forClass(classOf[Actress])
    relType.name should be(classOf[Actress].getName)
  }

  test("should not return relationship type for an unsupported class") {
    intercept[NoSuchElementException] {
      FilmCrewRelationshipType.forClass(classOf[FilmCrew])
    }
  }
}
