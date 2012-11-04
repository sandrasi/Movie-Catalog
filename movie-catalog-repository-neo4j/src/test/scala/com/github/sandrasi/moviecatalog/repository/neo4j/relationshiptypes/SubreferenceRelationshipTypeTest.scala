package com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.SubreferenceRelationshipType._

@RunWith(classOf[JUnitRunner])
class SubreferenceRelationshipTypeTest extends FunSuite with ShouldMatchers {

  test("should list all subreference relationship type values") {
    SubreferenceRelationshipType.values should be(List(Actor, Actress, Cast, Character, DigitalContainer, MotionPicture, Movie, Person, Soundtrack, Subtitle))
  }

  test("should create subreference relationship type values from string matching enum names") {
    SubreferenceRelationshipType.valueOf("Actor") should be(Actor)
    SubreferenceRelationshipType.valueOf("Actress") should be(Actress)
    SubreferenceRelationshipType.valueOf("Cast") should be(Cast)
    SubreferenceRelationshipType.valueOf("Character") should be(Character)
    SubreferenceRelationshipType.valueOf("DigitalContainer") should be(DigitalContainer)
    SubreferenceRelationshipType.valueOf("MotionPicture") should be(MotionPicture)
    SubreferenceRelationshipType.valueOf("Movie") should be(Movie)
    SubreferenceRelationshipType.valueOf("Person") should be(Person)
    SubreferenceRelationshipType.valueOf("Soundtrack") should be(Soundtrack)
    SubreferenceRelationshipType.valueOf("Subtitle") should be(Subtitle)
  }

  test("should not create subreference type value from string not matching any of the enums' name") {
    intercept[NoSuchElementException] {
      SubreferenceRelationshipType.valueOf("does not match")
    }
  }

  test("should retrieve the name of the subreference relationship type") {
    Actor.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actor].getName)
    Actress.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actress].getName)
    Cast.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Cast].getName)
    Character.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Character].getName)
    DigitalContainer.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.container.DigitalContainer].getName)
    MotionPicture.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.MotionPicture].getName)
    Movie.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Movie].getName)
    Person.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Person].getName)
    Soundtrack.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.container.Soundtrack].getName)
    Subtitle.name should be(classOf[com.github.sandrasi.moviecatalog.domain.entities.container.Subtitle].getName)
  }

  test("should create subreference relationship type from bound classes") {
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actor]) should be(Actor)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actress]) should be(Actress)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Cast]) should be(Cast)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Character]) should be(Character)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.container.DigitalContainer]) should be(DigitalContainer)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.MotionPicture]) should be(MotionPicture)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Movie]) should be(Movie)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.core.Person]) should be(Person)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.container.Soundtrack]) should be(Soundtrack)
    SubreferenceRelationshipType.forClass(classOf[com.github.sandrasi.moviecatalog.domain.entities.container.Subtitle]) should be(Subtitle)
  }

  test("should not create subreference relationship type from unsupported an class") {
    intercept[NoSuchElementException] {
      SubreferenceRelationshipType.forClass(classOf[Any])
    }
  }
}
