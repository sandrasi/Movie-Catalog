package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType

@RunWith(classOf[JUnitRunner])
class EntityFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: EntityFactory = _

  override protected def beforeEach() {
    subject = EntityFactory(db)
  }
  
  test("should return the same entity factory instance for the same database") {
    subject should be theSameInstanceAs(EntityFactory(db))
  }

  test("should return different entity factory instances for different databases") {
    subject should not be theSameInstanceAs(EntityFactory(createTempDb()))
  }

  test("should not instantiate entity factory if the database is null") {
    intercept[IllegalArgumentException] {
      EntityFactory(null)
    }
  }

  test("should create actor entity from node") {
    val person = insertEntity(JohnDoe)
    val character = insertEntity(Johnny)
    val movie = insertEntity(TestMovie)
    val actorNode = createNodeFrom(Actor(person, character, movie))
    val actor = subject.createEntityFrom(actorNode, classOf[Actor])
    actor.person should be(JohnDoe)
    actor.character should be(Johnny)
    actor.motionPicture should be(TestMovie)
    actor.version should be(0)
    actor.id should be(Some(actorNode.getId))
  }

  test("should create actress entity from node") {
    val person = insertEntity(JaneDoe)
    val character = insertEntity(Jenny)
    val movie = insertEntity(TestMovie)
    val actressNode = createNodeFrom(Actress(person, character, movie))
    val actress = subject.createEntityFrom(actressNode, classOf[Actress])
    actress.person should be(JaneDoe)
    actress.character should be(Jenny)
    actress.motionPicture should be(TestMovie)
    actress.version should be(0)
    actress.id should be(Some(actressNode.getId))
  }

  test("should create character entity from node") {
    val characterNode = createNodeFrom(Johnny)
    val character = subject.createEntityFrom(characterNode, classOf[Character])
    character.name should be(Johnny.name)
    character.discriminator should be(Johnny.discriminator)
    character.version should be(Johnny.version)
    character.id should be(Some(characterNode.getId))
  }
  
  test("should create digital container entity from node") {
    val movie = insertEntity(TestMovie)
    val englishSoundtrack = insertEntity(EnglishSoundtrack)
    val hungarianSoundtrack = insertEntity(HungarianSoundtrack)
    val englishSubtitle = insertEntity(EnglishSubtitle)
    val hungarianSubtitle = insertEntity(HungarianSubtitle)
    val digitalContainerNode = createNodeFrom(DigitalContainer(movie, Set(englishSoundtrack, hungarianSoundtrack), Set(englishSubtitle, hungarianSubtitle)))
    val digitalContainer = subject.createEntityFrom(digitalContainerNode, classOf[DigitalContainer])
    digitalContainer.motionPicture should be(movie)
    digitalContainer.soundtracks should be(Set(englishSoundtrack, hungarianSoundtrack))
    digitalContainer.subtitles should be(Set(englishSubtitle, hungarianSubtitle))
    digitalContainer.version should be(0)
    digitalContainer.id should be(Some(digitalContainerNode.getId))
  }
  
  test("should create movie entity from node") {
    val movieNode = createNodeFrom(TestMovie)
    val movie = subject.createEntityFrom(movieNode, classOf[Movie])
    movie.originalTitle should be(TestMovie.originalTitle)
    movie.localizedTitles should be(TestMovie.localizedTitles)
    movie.length should be(TestMovie.length)
    movie.releaseDate should be(TestMovie.releaseDate)
    movie.version should be(TestMovie.version)
    movie.id should be(Some(movieNode.getId))
  }

  test("should create person entity from node") {
    val personNode = createNodeFrom(JohnDoe)
    val person = subject.createEntityFrom(personNode, classOf[Person])
    person.name should be(JohnDoe.name)
    person.gender should be(JohnDoe.gender)
    person.dateOfBirth should be(JohnDoe.dateOfBirth)
    person.placeOfBirth should be(JohnDoe.placeOfBirth)
    person.version should be(JohnDoe.version)
    person.id should be(Some(personNode.getId))
  }
  
  test("should create soundtrack entity from node") {
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    val soundtrack = subject.createEntityFrom(soundtrackNode, classOf[Soundtrack])
    soundtrack.languageCode should be(EnglishSoundtrack.languageCode)
    soundtrack.formatCode should be(EnglishSoundtrack.formatCode)
    soundtrack.languageName should be(EnglishSoundtrack.languageName)
    soundtrack.formatName should be(EnglishSoundtrack.formatName)
    soundtrack.version should be(EnglishSoundtrack.version)
    soundtrack.id should be(Some(soundtrackNode.getId))
  }
  
  test("should create soundtrack entity from node with language and format names of which locale matches the given locale") {
    val sn = insertEntity(EnglishSoundtrack)
    val soundtrackWithDifferentLocale = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, LocalizedText("Angol")(HungarianLocale), LocalizedText("DTS")(HungarianLocale), sn.version, sn.id.get)
    val soundtrackNode = transaction(db) { updateNodeOf(soundtrackWithDifferentLocale, HungarianLocale) }
    val soundtrack = subject.createEntityFrom(soundtrackNode, classOf[Soundtrack])(AmericanLocale)
    soundtrack.languageName should be(EnglishSoundtrack.languageName)
    soundtrack.formatName should be(EnglishSoundtrack.formatName)
  }

  test("should create soundtrack entity from node without language and format names if the locale does not match any of the saved values") {
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    val soundtrack = subject.createEntityFrom(soundtrackNode, classOf[Soundtrack])(HungarianLocale)
    soundtrack.languageName should be(None)
    soundtrack.formatName should be(None)
  }

  test("should create subtitle entity from node") {
    val subtitleNode = createNodeFrom(EnglishSubtitle)
    val subtitle = subject.createEntityFrom(subtitleNode, classOf[Subtitle])
    subtitle.languageCode should be(EnglishSubtitle.languageCode)
    subtitle.languageName should be(EnglishSubtitle.languageName)
    subtitle.version should be(EnglishSubtitle.version)
    subtitle.id should be(Some(subtitleNode.getId))
  }

  test("should create subtitle entity from node with language name of which locale matches the given locale") {
    val sn = insertEntity(EnglishSubtitle)
    val subtitleWithDifferentLocale = Subtitle(EnglishSubtitle.languageCode, LocalizedText("Angol")(HungarianLocale), sn.version, sn.id.get)
    val subtitleNode = transaction(db) { updateNodeOf(subtitleWithDifferentLocale, HungarianLocale) }
    val subtitle = subject.createEntityFrom(subtitleNode, classOf[Subtitle])(AmericanLocale)
    subtitle.languageName should be(EnglishSubtitle.languageName)
  }

  test("should create subtitle entity from node without language name if the locale does not match any of the saved values") {
    val subtitleNode = createNodeFrom(EnglishSubtitle)
    val subtitle = subject.createEntityFrom(subtitleNode, classOf[Subtitle])(HungarianLocale)
    subtitle.languageName should be(None)
  }

  test("should not create entity if the node represents a different type of entity") {
    intercept[ClassCastException] {
      subject.createEntityFrom(createNodeFrom(Johnny), classOf[Movie]) should be(None)
    }
  }

  test("should not create entity if the node does not represent an entity") {
    intercept[ClassCastException] {
      subject.createEntityFrom(createNode(), classOf[Person]) should be(None)
    }
  }

  test("should not create unsupported entity from node") {
    val node = createNode()
    transaction(db) { node.createRelationshipTo(db.getNodeById(subrefNodeSupp.getSubrefNodeIdFor(classOf[DigitalContainer])), EntityRelationshipType.IsA) }
    intercept[IllegalArgumentException] {
      subject.createEntityFrom(node, classOf[VersionedLongIdEntity])
    }
  }
}
