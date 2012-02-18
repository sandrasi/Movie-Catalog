package com.github.sandrasi.moviecatalog.repository.neo4j

import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.neo4j.graphdb.NotFoundException
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Male
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

@RunWith(classOf[JUnitRunner])
class Neo4jRepositoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: Neo4jRepository = _

  override protected def beforeEach() {
    subject = new Neo4jRepository(db)
  }

  test("should fetch actor from the database by id") {
    val actorNode = createNodeFrom(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    subject.get(actorNode.getId, classOf[Actor]).get.isInstanceOf[Actor] should be(true)
  }

  test("should fetch actress from the database by id") {
    val actressNode = createNodeFrom(Actress(insertEntity(JaneDoe), insertEntity(Jenny), insertEntity(TestMovie)))
    subject.get(actressNode.getId, classOf[Actress]).get.isInstanceOf[Actress] should be(true)
  }

  test("should fetch character from the database by id") {
    val characterNode = createNodeFrom(Johnny)
    subject.get(characterNode.getId, classOf[Character]).get.isInstanceOf[Character] should be(true)
  }

  test("should fetch digital container from the database by id") {
    val digitalContainerNode = createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    subject.get(digitalContainerNode.getId, classOf[DigitalContainer]).get.isInstanceOf[DigitalContainer] should be(true)
  }

  test("should fetch movie from the database by id") {
    val movieNode = createNodeFrom(TestMovie)
    subject.get(movieNode.getId, classOf[Movie]).get.isInstanceOf[Movie] should be(true)
  }

  test("should fetch person from the database by id") {
    val personNode = createNodeFrom(JohnDoe)
    subject.get(personNode.getId, classOf[Person]).get.isInstanceOf[Person] should be(true)
  }
  
  test("should fetch soundtrack from the database by id") {
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    subject.get(soundtrackNode.getId, classOf[Soundtrack]).get.isInstanceOf[Soundtrack] should be(true)
  }

  test("should fetch subtitle from the database by id") {
    val subtitleNode = createNodeFrom(EnglishSubtitle)
    subject.get(subtitleNode.getId, classOf[Subtitle]).get.isInstanceOf[Subtitle] should be(true)
  }

  test("should return nothing if there is no node in the database with the specified id") {
    subject.get(getNodeCount + 1, classOf[Actor]) should be(None)
  }
  
  test("should return nothing if the node cannot be converted to the given type") {
    val characterNode = createNodeFrom(Johnny)
    subject.get(characterNode.getId, classOf[Actor]) should be(None)
  }

  test("should insert actor into the database and return a managed instance") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie))
    val savedActor = subject.save(actor)
    savedActor.id should not be(None)
    savedActor should equal(actor)
    try {
      db.getNodeById(savedActor.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert character into the database and return a managed instance") {
    val savedCharacter = subject.save(Johnny)
    savedCharacter.id should not be(None)
    savedCharacter should equal(Johnny)
    try {
      db.getNodeById(savedCharacter.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert digital container into the database and return a managed instance") {
    val digitalContainer = DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)))
    val savedDigitalContainer = subject.save(digitalContainer)
    savedDigitalContainer.id should not be(None)
    savedDigitalContainer should equal(digitalContainer)
    try {
      db.getNodeById(savedDigitalContainer.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert movie into the database and return a managed instance") {
    val savedMovie = subject.save(TestMovie)
    savedMovie.id should not be(None)
    savedMovie should equal(TestMovie)
    try {
      db.getNodeById(savedMovie.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert person into the database and return a managed instance") {
    val savedPerson = subject.save(JohnDoe)
    savedPerson.id should not be(None)
    savedPerson should equal(JohnDoe)
    try {
      db.getNodeById(savedPerson.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert soundtrack into the database and return a managed instance") {
    val savedSoundtrack = subject.save(EnglishSoundtrack)
    savedSoundtrack.id should not be(None)
    savedSoundtrack should equal(EnglishSoundtrack)
    try {
      db.getNodeById(savedSoundtrack.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert subtitle into the database and return a managed instance") {
    val savedSubtitle = subject.save(EnglishSubtitle)
    savedSubtitle.id should not be(None)
    savedSubtitle should equal(EnglishSubtitle)
    try {
      db.getNodeById(savedSubtitle.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }
  
  test("should not insert unsupported entity into the database") {
    intercept[IllegalArgumentException] {
      subject.save(new VersionedLongIdEntity(0, 0) {})
    }
  }

  test("should update actor in the database and return a managed instance") {
    val actorInDb = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val modifiedActor = Actor(insertEntity(Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown")), insertEntity(Character("Jamie")), insertEntity(Movie("Foo movie title")), actorInDb.version, actorInDb.id.get)
    val updatedActor = subject.save(modifiedActor)
    updatedActor.version should be(actorInDb.version + 1)
    updatedActor.id should be(actorInDb.id)
    updatedActor should equal(modifiedActor)
  }

  test("should update character in the database and return a managed instance") {
    val characterInDb = insertEntity(Johnny)
    val modifiedCharacter = Character("Jenny", "foo", characterInDb.version, characterInDb.id.get)
    val updatedCharacter = subject.save(modifiedCharacter)
    updatedCharacter.version should be(characterInDb.version + 1)
    updatedCharacter.id should be (characterInDb.id)
    updatedCharacter should equal(modifiedCharacter)
  }

  test("should update digital container in the database and return a managed instance") {
    val digitalContainerInDb = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val modifiedDigitalContainer = DigitalContainer(insertEntity(Movie("Foo movie title")), Set(insertEntity(HungarianSoundtrack)), Set(insertEntity(HungarianSubtitle)), digitalContainerInDb.version, digitalContainerInDb.id.get)
    val updatedDigitalContainer = subject.save(modifiedDigitalContainer)
    updatedDigitalContainer.version should be(digitalContainerInDb.version + 1)
    updatedDigitalContainer.id should be (digitalContainerInDb.id)
    updatedDigitalContainer should equal(modifiedDigitalContainer)
  }

  test("should update movie in the database and return a managed instance") {
    val movieInDb = insertEntity(TestMovie)
    val modifiedMovie = Movie("Foo movie title", Set(LocalizedText("Foo film cím")(HungarianLocale), LocalizedText("Foo film titolo")(ItalianLocale)), Duration.standardMinutes(100), new LocalDate(2012, 1, 30), movieInDb.version, movieInDb.id.get)
    val updatedMovie = subject.save(modifiedMovie)
    updatedMovie.version should be(movieInDb.version + 1)
    updatedMovie.id should be (movieInDb.id)
    updatedMovie should equal(modifiedMovie)
  }

  test("should update person in the database and return a managed instance") {
    val personInDb = insertEntity(JohnDoe)
    val modifiedPerson = Person(JaneDoe.name, JaneDoe.gender, JaneDoe.dateOfBirth, JaneDoe.placeOfBirth, personInDb.version, personInDb.id.get)
    val updatedPerson = subject.save(modifiedPerson)
    updatedPerson.version should be(personInDb.version + 1)
    updatedPerson.id should be (personInDb.id)
    updatedPerson should equal(modifiedPerson)
  }

  test("should update soundtrack in the database and return a managed instance with language and format name matching the current locale") {
    val soundtrackInDb = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("modified language code", "modified format code", LocalizedText("Angol")(HungarianLocale), LocalizedText("DTS")(HungarianLocale), soundtrackInDb.version, soundtrackInDb.id.get)
    val updatedSoundtrack = subject.save(modifiedSoundtrack)(HungarianLocale)
    updatedSoundtrack.version should be(soundtrackInDb.version + 1)
    updatedSoundtrack.id should be (soundtrackInDb.id)
    updatedSoundtrack should equal(modifiedSoundtrack)
    updatedSoundtrack.languageName.get should be(LocalizedText("Angol")(HungarianLocale))
    updatedSoundtrack.formatName.get should be(LocalizedText("DTS")(HungarianLocale))
  }

  test("should update subtitle in the database and return a managed instance with language name matching the current locale") {
    val subtitleInDb = insertEntity(EnglishSubtitle)
    val modifiedSubtitle = Subtitle("modified language code", LocalizedText("Angol")(HungarianLocale), subtitleInDb.version, subtitleInDb.id.get)
    val updatedSubtitle = subject.save(modifiedSubtitle)(HungarianLocale)
    updatedSubtitle.version should be(subtitleInDb.version + 1)
    updatedSubtitle.id should be (subtitleInDb.id)
    updatedSubtitle should equal(modifiedSubtitle)
    updatedSubtitle.languageName.get should be(LocalizedText("Angol")(HungarianLocale))
  }

  test("should not update unsupported entity in the database") {
    intercept[IllegalArgumentException] {
      subject.save(new VersionedLongIdEntity(0, 1) {})
    }
  }

  test("should delete actor from the database") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    subject.delete(actor)
    intercept[NotFoundException] {
      db.getNodeById(actor.id.get)
    }
  }

  test("should delete character from the database") {
    val character = insertEntity(Johnny)
    subject.delete(character)
    intercept[NotFoundException] {
      db.getNodeById(character.id.get)
    }
  }
  
  test("should delete digital container from the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    subject.delete(digitalContainer)
    intercept[NotFoundException] {
      db.getNodeById(digitalContainer.id.get)
    }
  }

  test("should delete movie from the database") {
    val movie = insertEntity(TestMovie)
    subject.delete(movie)
    intercept[NotFoundException] {
      db.getNodeById(movie.id.get)
    }
  }
  
  test("should delete person from the database") {
    val person = insertEntity(JohnDoe)
    subject.delete(person)
    intercept[NotFoundException] {
      db.getNodeById(person.id.get)
    }
  }
  
  test("should delete soundtrack from the database") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    subject.delete(soundtrack)
    intercept[NotFoundException] {
      db.getNodeById(soundtrack.id.get)
    }
  }

  test("should delete subtitle from the database") {
    val subtitle = insertEntity(EnglishSubtitle)
    subject.delete(subtitle)
    intercept[NotFoundException] {
      db.getNodeById(subtitle.id.get)
    }
  }

  test("should not delete unsupported entity from the database") {
    intercept[IllegalArgumentException] {
      subject.delete(new VersionedLongIdEntity(0, 1) {})
    }
  }
}
