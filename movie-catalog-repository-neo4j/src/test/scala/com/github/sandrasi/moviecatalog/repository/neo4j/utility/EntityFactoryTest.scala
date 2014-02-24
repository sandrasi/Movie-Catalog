package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants.EntityId
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EntityFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with MovieCatalogNeo4jSupport {

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
  
  test("should create cast entity from actor node") {
    val person = insertEntity(JohnTravolta)
    val character = insertEntity(VincentVega)
    val movie = insertEntity(PulpFiction)
    val actorNode = createNodeFrom(Actor(person, character, movie))
    val cast = subject.createEntityFrom(actorNode, classOf[Cast])
    cast.person should be(JohnTravolta)
    cast.character should be(VincentVega)
    cast.motionPicture should be(PulpFiction)
    cast.version should be(0)
    cast.id.get.toString should be(actorNode.getProperty(Uuid).asInstanceOf[String])
    assert(cast.isInstanceOf[Actor])
  }

  test("should create cast entity from actress node") {
    val person = insertEntity(UmaThurman)
    val character = insertEntity(MiaWallace)
    val movie = insertEntity(PulpFiction)
    val actressNode = createNodeFrom(Actress(person, character, movie))
    val cast = subject.createEntityFrom(actressNode, classOf[Cast])
    cast.person should be(UmaThurman)
    cast.character should be(MiaWallace)
    cast.motionPicture should be(PulpFiction)
    cast.version should be(0)
    cast.id.get.toString should be(actressNode.getProperty(Uuid).asInstanceOf[String])
    assert(cast.isInstanceOf[Actress])
  }

  test("should not create cast from node of unspecified cast node") {
    val node = createNode()
    transaction(db) { node.createRelationshipTo(dbMgr.getSubreferenceNode(classOf[Cast]), IsA) }
    intercept[IllegalArgumentException] {
      subject.createEntityFrom(node, classOf[Cast])
    }
  }

  test("should create actor entity from node") {
    val person = insertEntity(JohnTravolta)
    val character = insertEntity(VincentVega)
    val movie = insertEntity(PulpFiction)
    val actorNode = createNodeFrom(Actor(person, character, movie))
    val actor = subject.createEntityFrom(actorNode, classOf[Actor])
    actor.person should be(JohnTravolta)
    actor.character should be(VincentVega)
    actor.motionPicture should be(PulpFiction)
    actor.version should be(0)
    actor.id.get.toString should be(actorNode.getProperty(Uuid).asInstanceOf[String])
  }

  test("should create actress entity from node") {
    val person = insertEntity(UmaThurman)
    val character = insertEntity(MiaWallace)
    val movie = insertEntity(PulpFiction)
    val actressNode = createNodeFrom(Actress(person, character, movie))
    val actress = subject.createEntityFrom(actressNode, classOf[Actress])
    actress.person should be(UmaThurman)
    actress.character should be(MiaWallace)
    actress.motionPicture should be(PulpFiction)
    actress.version should be(0)
    actress.id.get.toString should be(actressNode.getProperty(Uuid).asInstanceOf[String])
  }

  test("should create character entity from node") {
    val characterNode = createNodeFrom(VincentVega)
    val character = subject.createEntityFrom(characterNode, classOf[Character])
    character.name should be(VincentVega.name)
    character.version should be(VincentVega.version)
    character.id.get.toString should be(characterNode.getProperty(Uuid).asInstanceOf[String])
  }
  
  test("should create digital container entity from node") {
    val movie = insertEntity(PulpFiction)
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
    digitalContainer.id.get.toString should be(digitalContainerNode.getProperty(Uuid).asInstanceOf[String])
  }

  test("should create genre entity from node") {
    val genreNode = createNodeFrom(Crime)
    val genre = subject.createEntityFrom(genreNode, classOf[Genre])
    genre.code should be(Crime.code)
    genre.name should be(Crime.name)
    genre.version should be(Crime.version)
    genre.id.get.toString should be(genreNode.getProperty(Uuid).asInstanceOf[String])
  }

  test("should create genre entity from node with name of which locale matches the given locale") {
    val ge = insertEntity(Crime)
    val genreWithDifferentLocale = ge.copy(name = Some(LocalizedText("Krimi")(HungarianLocale)))
    val genreNode = transaction(db) { updateNodeOf(genreWithDifferentLocale, HungarianLocale) }
    val genre = subject.createEntityFrom(genreNode, classOf[Genre])(AmericanLocale)
    genre.name should be(Crime.name)
  }

  test("should create genre entity from node without name if the locale does not match any of the saved values") {
    val genreNode = createNodeFrom(Crime)
    val genre = subject.createEntityFrom(genreNode, classOf[Genre])(HungarianLocale)
    genre.name should be(None)
  }

  test("should create movie entity from node") {
    val movieNode = createNodeFrom(PulpFiction)
    val movie = subject.createEntityFrom(movieNode, classOf[Movie])(HungarianLocale)
    movie.originalTitle should be(PulpFiction.originalTitle)
    movie.localizedTitle should be(PulpFiction.localizedTitle)
    movie.runtime should be(PulpFiction.runtime)
    movie.releaseDate should be(PulpFiction.releaseDate)
    movie.version should be(PulpFiction.version)
    movie.id.get.toString should be(movieNode.getProperty(Uuid).asInstanceOf[String])
  }

  test("should create person entity from node") {
    val personNode = createNodeFrom(JohnTravolta)
    val person = subject.createEntityFrom(personNode, classOf[Person])
    person.name should be(JohnTravolta.name)
    person.gender should be(JohnTravolta.gender)
    person.dateOfBirth should be(JohnTravolta.dateOfBirth)
    person.placeOfBirth should be(JohnTravolta.placeOfBirth)
    person.version should be(JohnTravolta.version)
    person.id.get.toString should be(personNode.getProperty(Uuid).asInstanceOf[String])
  }
  
  test("should create soundtrack entity from node") {
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    val soundtrack = subject.createEntityFrom(soundtrackNode, classOf[Soundtrack])
    soundtrack.languageCode should be(EnglishSoundtrack.languageCode)
    soundtrack.formatCode should be(EnglishSoundtrack.formatCode)
    soundtrack.languageName should be(EnglishSoundtrack.languageName)
    soundtrack.formatName should be(EnglishSoundtrack.formatName)
    soundtrack.version should be(EnglishSoundtrack.version)
    soundtrack.id.get.toString should be(soundtrackNode.getProperty(Uuid).asInstanceOf[String])
  }
  
  test("should create soundtrack entity from node with language and format names of which locales match the given locale") {
    val se = insertEntity(EnglishSoundtrack)
    val soundtrackWithDifferentLocale = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, LocalizedText("Angol")(HungarianLocale), LocalizedText("DTS")(HungarianLocale), se.version, se.id.get)
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
    subtitle.id.get.toString should be(subtitleNode.getProperty(Uuid).asInstanceOf[String])
  }

  test("should create subtitle entity from node with language name of which locale matches the given locale") {
    val se = insertEntity(EnglishSubtitle)
    val subtitleWithDifferentLocale = Subtitle(EnglishSubtitle.languageCode, LocalizedText("Angol")(HungarianLocale), se.version, se.id.get)
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
      subject.createEntityFrom(createNodeFrom(VincentVega), classOf[Movie]) should be(None)
    }
  }

  test("should not create entity if the node does not represent an entity") {
    intercept[ClassCastException] {
      subject.createEntityFrom(createNode(), classOf[Person]) should be(None)
    }
  }

  test("should not create unsupported entity from node") {
    val node = createNode()
    transaction(db) { node.createRelationshipTo(dbMgr.getSubreferenceNode(classOf[DigitalContainer]), EntityRelationshipType.IsA) }
    intercept[IllegalArgumentException] {
      subject.createEntityFrom(node, classOf[Entity])
    }
  }
}
