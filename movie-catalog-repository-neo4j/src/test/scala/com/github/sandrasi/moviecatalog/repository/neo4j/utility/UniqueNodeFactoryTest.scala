package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.LocalDate
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actor
import com.github.sandrasi.moviecatalog.domain.entities.core.{Movie, Character, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import java.util.UUID
import com.github.sandrasi.moviecatalog.domain.entities.container.DigitalContainer

class UniqueNodeFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: UniqueNodeFactory = _

  override protected def beforeEach() {
    subject = UniqueNodeFactory(db)
  }
  
  test("should not create node from actor if a node already exists") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie))
    implicit val tx = db.beginTx()
    transaction(tx) {
      subject.createNodeFrom(actor)
    }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) {
        subject.createNodeFrom(actor)
      }
    }
  }
  
  test("should create node from actor if a different person played the same character in the same movie") {
    val character = insertEntity(Johnny)
    val movie = insertEntity(TestMovie)
    val actor = Actor(insertEntity(JohnDoe), character, movie)
    val anotherActor = Actor(insertEntity(Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown")), character, movie)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should create node from actor if a the same person played a different character in the same movie") {
    val person = insertEntity(JohnDoe)
    val movie = insertEntity(TestMovie)
    val actor = Actor(person, insertEntity(Johnny), movie)
    val anotherActor = Actor(person, insertEntity(Character("Jamie")), movie)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should create node from actor if a the same person played the same character in a different movie") {
    val person = insertEntity(JohnDoe)
    val character = insertEntity(Johnny)
    val actor = Actor(person, character, insertEntity(TestMovie))
    val anotherActor = Actor(person, character, insertEntity(Movie("Foo movie title")))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should not create node from character if a node already exists") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      subject.createNodeFrom(Johnny)
    }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) {
        subject.createNodeFrom(Johnny)
      }
    }
  }

  test("should create node from character if the name is different") {
    val anotherCharacter = Character("Jenny", Johnny.discriminator)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val characterNode = subject.createNodeFrom(Johnny)
      val anotherCharacterNode = subject.createNodeFrom(anotherCharacter)
      characterNode.getId should not equal(anotherCharacterNode.getId)
    }
  }

  test("should create node from character if the discriminator is different") {
    val anotherCharacter = Character(Johnny.name, UUID.randomUUID.toString)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val characterNode = subject.createNodeFrom(Johnny)
      val anotherCharacterNode = subject.createNodeFrom(anotherCharacter)
      characterNode.getId should not equal(anotherCharacterNode.getId)
    }
  }

  test("should not create node from digital container if a node already exists") {
    val digitalContainer = DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle)))
    implicit val tx = db.beginTx()
    transaction(tx) {
      subject.createNodeFrom(digitalContainer)
    }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) {
        subject.createNodeFrom(digitalContainer)
      }
    }
  }

  test("should create node from digital container if it contains a different motion picture with the same soundtracks and subtitles") {
    val soundtracks = Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack))
    val subtitles = Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))
    val digitalContainer = DigitalContainer(insertEntity(TestMovie), soundtracks, subtitles)
    val anotherDigitalContainer = DigitalContainer(insertEntity(Movie("Foo movie title")), soundtracks, subtitles)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val digitalContainerNode = subject.createNodeFrom(digitalContainer)
      val anotherDigitalContainerNode = subject.createNodeFrom(anotherDigitalContainer)
      digitalContainerNode.getId should not equal(anotherDigitalContainerNode.getId)
    }
  }

  test("should create node from digital container if it contains the same movie with different soundtracks and same subtitles") {
    val movie = insertEntity(TestMovie)
    val subtitles = Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))
    val soundtrack = insertEntity(EnglishSoundtrack)
    val digitalContainer = DigitalContainer(movie, Set(soundtrack, insertEntity(HungarianSoundtrack)), subtitles)
    val anotherDigitalContainer = DigitalContainer(movie, Set(soundtrack, insertEntity(ItalianSoundtrack)), subtitles)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val digitalContainerNode = subject.createNodeFrom(digitalContainer)
      val anotherDigitalContainerNode = subject.createNodeFrom(anotherDigitalContainer)
      digitalContainerNode.getId should not equal(anotherDigitalContainerNode.getId)
    }
  }

  test("should create node from digital container if it contains the same movie with subset of soundtracks and same subtitles") {
    val movie = insertEntity(TestMovie)
    val subtitles = Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))
    val soundtrack = insertEntity(EnglishSoundtrack)
    val digitalContainer = DigitalContainer(movie, Set(soundtrack, insertEntity(HungarianSoundtrack)), subtitles)
    val anotherDigitalContainer = DigitalContainer(movie, Set(soundtrack), subtitles)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val digitalContainerNode = subject.createNodeFrom(digitalContainer)
      val anotherDigitalContainerNode = subject.createNodeFrom(anotherDigitalContainer)
      digitalContainerNode.getId should not equal(anotherDigitalContainerNode.getId)
    }
  }

  test("should create node from digital container if it contains the same movie with same soundtracks and different subtitles") {
    val movie = insertEntity(TestMovie)
    val soundtracks = Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack))
    val subtitle = insertEntity(EnglishSubtitle)
    val digitalContainer = DigitalContainer(movie, soundtracks, Set(subtitle, insertEntity(HungarianSubtitle)))
    val anotherDigitalContainer = DigitalContainer(movie, soundtracks, Set(subtitle, insertEntity(ItalianSubtitle)))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val digitalContainerNode = subject.createNodeFrom(digitalContainer)
      val anotherDigitalContainerNode = subject.createNodeFrom(anotherDigitalContainer)
      digitalContainerNode.getId should not equal(anotherDigitalContainerNode.getId)
    }
  }

  test("should create node from digital container if it contains the same movie with same soundtracks and subset subtitles") {
    val movie = insertEntity(TestMovie)
    val soundtracks = Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack))
    val subtitle = insertEntity(EnglishSubtitle)
    val digitalContainer = DigitalContainer(movie, soundtracks, Set(subtitle, insertEntity(HungarianSubtitle)))
    val anotherDigitalContainer = DigitalContainer(movie, soundtracks, Set(subtitle))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val digitalContainerNode = subject.createNodeFrom(digitalContainer)
      val anotherDigitalContainerNode = subject.createNodeFrom(anotherDigitalContainer)
      digitalContainerNode.getId should not equal(anotherDigitalContainerNode.getId)
    }
  }
}
