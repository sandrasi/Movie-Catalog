package com.github.sandrasi.moviecatalog.repository.neo4j

import org.junit.runner.RunWith
import org.neo4j.graphdb.NotFoundException
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
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
    val actorRelationship = createRelationshipFrom(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    subject.get(actorRelationship.getId, classOf[Actor]).get.isInstanceOf[Actor] should be(true)
  }

  test("should fetch actress from the database by id") {
    val actorRelationship = createRelationshipFrom(Actress(insertEntity(JaneDoe), insertEntity(Jenny), insertEntity(TestMovie)))
    subject.get(actorRelationship.getId, classOf[Actress]).get.isInstanceOf[Actress] should be(true)
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

  test("should return nothing if there is no node or relationship in the database with the specified id") {
    subject.get(Long.MaxValue, classOf[Person]) should be(None)
    subject.get(Long.MaxValue, classOf[Actor]) should be(None)
  }
  
  test("should return nothing if the node cannot be converted to the given type") {
    val characterNode = createNodeFrom(Johnny)
    subject.get(characterNode.getId, classOf[Movie]) should be(None)
  }

  test("should insert actor into the database and return a managed instance") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie))
    val savedActor = subject.save(actor)
    savedActor.id should not be(None)
    savedActor should equal(actor)
    try {
      db.getRelationshipById(savedActor.id.get)
    } catch {
      case e: NotFoundException => fail("getRelationshipById(Long) should have returned a relationship")
    }
  }

  test("should insert actress into the database and return a managed instance") {
    val actress = Actress(insertEntity(JaneDoe), insertEntity(Jenny), insertEntity(TestMovie))
    val savedActress = subject.save(actress)
    savedActress.id should not be(None)
    savedActress should equal(actress)
    try {
      db.getRelationshipById(savedActress.id.get)
    } catch {
      case e: NotFoundException => fail("getRelationshipById(Long) should have returned a relationship")
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
      subject.save(new LongIdEntity(0) {})
    }
  }

  test("should update soundtrack in the database and return a managed instance with language and format name matching the current locale") {
    val soundtrackInDb = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("modified language code", "modified format code", LocalizedText("Angol")(HungarianLocale), LocalizedText("DTS")(HungarianLocale), id = soundtrackInDb.id.get)
    val updatedSoundtrack = subject.save(modifiedSoundtrack)
    updatedSoundtrack.id should be (soundtrackInDb.id)
    updatedSoundtrack should equal(modifiedSoundtrack)
    updatedSoundtrack.languageName.get should be(LocalizedText("English"))
    updatedSoundtrack.formatName.get should be(LocalizedText("DTS"))
  }
}
