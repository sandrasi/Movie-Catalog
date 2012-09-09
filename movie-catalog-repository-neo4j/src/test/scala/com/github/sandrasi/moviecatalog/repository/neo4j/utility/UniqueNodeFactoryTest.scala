package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import java.util.Locale
import org.joda.time.LocalDate
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actor
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container.DigitalContainer
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

@RunWith(classOf[JUnitRunner])
class UniqueNodeFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: UniqueNodeFactory = _

  override protected def beforeEach() {
    subject = UniqueNodeFactory(db)
  }
  
  test("should not create node from actor if a node already exists") {
    val actor = Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction))
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
    val character = insertEntity(VincentVega)
    val movie = insertEntity(PulpFiction)
    val actor = Actor(insertEntity(JohnTravolta), character, movie)
    val anotherActor = Actor(insertEntity(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S.")), character, movie)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should create node from actor if a the same person played a different character in the same movie") {
    val person = insertEntity(JohnTravolta)
    val movie = insertEntity(PulpFiction)
    val actor = Actor(person, insertEntity(VincentVega), movie)
    val anotherActor = Actor(person, insertEntity(Character("Jules Winnfield")), movie)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should create node from actor if a the same person played the same character in a different movie") {
    val person = insertEntity(JohnTravolta)
    val character = insertEntity(VincentVega)
    val actor = Actor(person, character, insertEntity(PulpFiction))
    val anotherActor = Actor(person, character, insertEntity(Movie("Die hard: With a vengeance")))
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
      subject.createNodeFrom(VincentVega)
    }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) {
        subject.createNodeFrom(VincentVega)
      }
    }
  }

  test("should create node from character if the name is different") {
    val anotherCharacter = Character("Jules Winnfield", VincentVega.discriminator)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val characterNode = subject.createNodeFrom(VincentVega)
      val anotherCharacterNode = subject.createNodeFrom(anotherCharacter)
      characterNode.getId should not equal(anotherCharacterNode.getId)
    }
  }

  test("should create node from character if the discriminator is different") {
    val anotherCharacter = Character(VincentVega.name, "discriminator")
    implicit val tx = db.beginTx()
    transaction(tx) {
      val characterNode = subject.createNodeFrom(VincentVega)
      val anotherCharacterNode = subject.createNodeFrom(anotherCharacter)
      characterNode.getId should not equal(anotherCharacterNode.getId)
    }
  }

  test("should not create node from digital container if a node already exists") {
    val digitalContainer = DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle)))
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
    val digitalContainer = DigitalContainer(insertEntity(PulpFiction), soundtracks, subtitles)
    val anotherDigitalContainer = DigitalContainer(insertEntity(Movie("Die hard: With a vengeance")), soundtracks, subtitles)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val digitalContainerNode = subject.createNodeFrom(digitalContainer)
      val anotherDigitalContainerNode = subject.createNodeFrom(anotherDigitalContainer)
      digitalContainerNode.getId should not equal(anotherDigitalContainerNode.getId)
    }
  }

  test("should create node from digital container if it contains the same movie with different soundtracks and same subtitles") {
    val movie = insertEntity(PulpFiction)
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
    val movie = insertEntity(PulpFiction)
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
    val movie = insertEntity(PulpFiction)
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
    val movie = insertEntity(PulpFiction)
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

  test("should not create node from movie if a node already exists") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      subject.createNodeFrom(PulpFiction)
    }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) {
        subject.createNodeFrom(PulpFiction)
      }
    }
  }

  test("should create node from movie if the original title is different") {
    val anotherMovie = Movie("Die hard: With a vengeance", releaseDate = PulpFiction.releaseDate)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(anotherMovie)
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the original title's locale's language is different") {
    implicit val locale = new Locale("hu", "US")
    val anotherMovie = Movie(new LocalizedText(PulpFiction.originalTitle.text), releaseDate = PulpFiction.releaseDate)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(anotherMovie)
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the original title's locale's country is different") {
    implicit val locale = new Locale("en", "GB")
    val anotherMovie = Movie(new LocalizedText(PulpFiction.originalTitle.text), releaseDate = PulpFiction.releaseDate)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(anotherMovie)
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the original title's locale's variant is different") {
    implicit val locale = new Locale("en", "US", "California")
    val anotherMovie = Movie(new LocalizedText(PulpFiction.originalTitle.text), releaseDate = PulpFiction.releaseDate)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(anotherMovie)
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the release date is different") {
    val anotherMovie = Movie(PulpFiction.originalTitle, releaseDate = new LocalDate(1995, 5, 19))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(anotherMovie)
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }
}
