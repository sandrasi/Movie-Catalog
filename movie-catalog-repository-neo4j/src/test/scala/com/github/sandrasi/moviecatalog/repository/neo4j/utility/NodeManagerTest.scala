package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConversions._
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.neo4j.graphdb.Direction._
import org.neo4j.graphdb.NotFoundException
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor}
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._

@RunWith(classOf[JUnitRunner])
class NodeManagerTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: NodeManager = _

  override protected def beforeEach() {
    subject = NodeManager(db)
  }

  test("should return the same node factory instance for the same database") {
    subject should be theSameInstanceAs(NodeManager(db))
  }

  test("should return different node factory instances for different databases") {
    subject should not be theSameInstanceAs(NodeManager(createTempDb()))
  }

  test("should not instantiate node factory if the database is null") {
    intercept[IllegalArgumentException] {
      NodeManager(null)
    }
  }

  test("should create node from actor") {
    val personNode = createNodeFrom(JohnDoe)
    val characterNode = createNodeFrom(Johnny)
    val movieNode = createNodeFrom(TestMovie)
    val actorNode = transaction(db) { subject.createNodeFrom(Actor(createPersonFrom(personNode), createCharacterFrom(characterNode), createMovieFrom(movieNode))) }
    actorNode.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode should be(personNode)
    actorNode.getSingleRelationship(PlayedBy, OUTGOING).getEndNode should be(characterNode)
    actorNode.getSingleRelationship(AppearedIn, OUTGOING).getEndNode should be(movieNode)
    getLong(actorNode, Version) should be(0)
    actorNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[AbstractCast]))
  }

  test("should not create node from the actor if the person does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Actor(JohnDoe, insertEntity(Johnny), insertEntity(TestMovie))) }
    }
  }

  test("should not create node from the actor if the character does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Actor(insertEntity(JohnDoe), Johnny, insertEntity(TestMovie))) }
    }
  }

  test("should not create node from the actor if the motion picture does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Actor(insertEntity(JohnDoe), insertEntity(Johnny), TestMovie)) }
    }
  }

  test("should not create node from the actor if the person has an id referring to a node which does not exist in the database") {
    val person = Person(JohnDoe.name, JohnDoe.gender, JohnDoe.dateOfBirth, JohnDoe.placeOfBirth, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Actor(person, insertEntity(Johnny), insertEntity(TestMovie))) }
    }
  }

  test("should not create node from the actor if the character has an id referring to a node which does not exist in the database") {
    val character = Character(Johnny.name, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Actor(insertEntity(JohnDoe), character, insertEntity(TestMovie))) }
    }
  }

  test("should not create node from the actor if the motion picture has an id referring to a node which does not exist in the database") {
    val movie = Movie(TestMovie.originalTitle, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Actor(insertEntity(JohnDoe), insertEntity(Johnny), movie)) }
    }
  }

  test("should not create node from the actor if the person has an id referring to a non person node") {
    val node = createNode()
    val person = Person(JohnDoe.name, JohnDoe.gender, JohnDoe.dateOfBirth, JohnDoe.placeOfBirth, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(Actor(person, insertEntity(Johnny), insertEntity(TestMovie))) }
    }
  }

  test("should not create node from the actor if the character has an id referring to a non character node") {
    val node = createNode()
    val character = Character(Johnny.name, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(Actor(insertEntity(JohnDoe), character, insertEntity(TestMovie))) }
    }
  }

  test("should not create node from the actor if the motion picture has an id referring to a non motion picture node") {
    val node = createNode()
    val movie = Movie(TestMovie.originalTitle, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(Actor(insertEntity(JohnDoe), insertEntity(Johnny), movie)) }
    }
  }

  test("should not create node from the actor if the actor already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie), id = 1))
    }
  }

  test("should create node from character") {
    val characterNode = transaction(db) { subject.createNodeFrom(Johnny) }
    getString(characterNode, CharacterName) should be(Johnny.name)
    getString(characterNode, CharacterDiscriminator) should be(Johnny.discriminator)
    getLong(characterNode, Version) should be(Johnny.version)
    characterNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Character]))
  }

  test("should not create node from character if the character already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Character("Character with id", "", id = 1))
    }
  }

  test("should create node from digital container") {
    val movieNode = createNodeFrom(TestMovie)
    val englishSoundtrackNode = createNodeFrom(EnglishSoundtrack)
    val hungarianSoundtrackNode = createNodeFrom(HungarianSoundtrack)
    val englishSubtitleNode = createNodeFrom(EnglishSubtitle)
    val hungarianSubtitleNode = createNodeFrom(HungarianSubtitle)
    val digitalContainerNode = transaction(db) { subject.createNodeFrom(DigitalContainer(createMovieFrom(movieNode), Set(createSoundtrackFrom(englishSoundtrackNode), createSoundtrackFrom(hungarianSoundtrackNode)), Set(createSubtitleFrom(englishSubtitleNode), createSubtitleFrom(hungarianSubtitleNode)))) }
    digitalContainerNode.getSingleRelationship(StoredIn, INCOMING).getStartNode should be(movieNode)
    digitalContainerNode.getRelationships(WithSoundtrack, OUTGOING).map(_.getEndNode).toSet should be(Set(englishSoundtrackNode, hungarianSoundtrackNode))
    digitalContainerNode.getRelationships(WithSubtitle, OUTGOING).map(_.getEndNode).toSet should be(Set(englishSubtitleNode, hungarianSubtitleNode))
    getLong(digitalContainerNode, Version) should be(0)
    digitalContainerNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[DigitalContainer]))
  }
  
  test("should not create node from the digital container if the motion picture does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(TestMovie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(EnglishSoundtrack), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(EnglishSubtitle))) }
    }
  }

  test("should not create node from the digital container if the motion picture has an id referring to a node which does not exist in the database") {
    val movie = Movie(TestMovie.originalTitle, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(movie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks has an id referring to a node which does not exist in the database") {
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(soundtrack), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles has an id referring to a node which does not exist in the database") {
    val subtitle = Subtitle(EnglishSubtitle.languageCode, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(subtitle))) }
    }
  }

  test("should not create node from the digital container if the motion picture has an id referring to a non motion picture node") {
    val node = createNode()
    val movie = Movie(TestMovie.originalTitle, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(movie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks has an id referring to a non soundtrack node") {
    val node = createNode()
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(soundtrack), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles has an id referring to a non subtitle node") {
    val node = createNode()
    val subtitle = Subtitle(EnglishSubtitle.languageCode, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(subtitle))) }
    }
  }
  
  test("should not create node from the digital container if the digital container already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)), id = 1))
    }
  }

  test("should create node from movie") {
    val movieNode = transaction(db) { subject.createNodeFrom(TestMovie) }
    getDuration(movieNode, MovieLength) should be(TestMovie.length)
    getLocalDate(movieNode, MovieReleaseDate) should be(TestMovie.releaseDate)
    getLocalizedText(movieNode, MovieOriginalTitle) should be(TestMovie.originalTitle)
    getLocalizedTextSet(movieNode, MovieLocalizedTitles) should be(TestMovie.localizedTitles)
    getLong(movieNode, Version) should be(TestMovie.version)
    movieNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Movie]))
  }
  
  test("should not create node from movie if the movie already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Movie("Movie with id", id = 1))
    }
  }

  test("should create node from person") {
    val personNode = transaction(db) { subject.createNodeFrom(JohnDoe) }
    getString(personNode, PersonName) should be(JohnDoe.name)
    getString(personNode, PersonGender) should be(JohnDoe.gender.toString)
    getLocalDate(personNode, PersonDateOfBirth) should be(JohnDoe.dateOfBirth)
    getString(personNode, PersonPlaceOfBirth) should be(JohnDoe.placeOfBirth)
    getLong(personNode, Version) should be(JohnDoe.version)
    personNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Person]))
  }

  test("should not create node from person if the person already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Person(JohnDoe.name, JohnDoe.gender, JohnDoe.dateOfBirth, JohnDoe.placeOfBirth, id = 1))
    }
  }

  test("should create node from soundtrack") {
    val soundtrackNode = transaction(db) { subject.createNodeFrom(EnglishSoundtrack) }
    getString(soundtrackNode, SoundtrackLanguageCode) should be(EnglishSoundtrack.languageCode)
    getString(soundtrackNode, SoundtrackFormatCode) should be(EnglishSoundtrack.formatCode)
    Some(getLocalizedText(soundtrackNode, SoundtrackLanguageNames)) should be(EnglishSoundtrack.languageName)
    Some(getLocalizedText(soundtrackNode, SoundtrackFormatNames)) should be(EnglishSoundtrack.formatName)
    getLong(soundtrackNode, Version) should be(EnglishSoundtrack.version)
    soundtrackNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Soundtrack]))
  }

  test("should create node from soundtrack without language name") {
    val soundtrackNode = transaction(db) { subject.createNodeFrom(Soundtrack("en", "dts", formatName = "DTS")) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackLanguageNames)
    }
  }

  test("should create node from soundtrack without format name") {
    val soundtrackNode = transaction(db) { subject.createNodeFrom(Soundtrack("en", "dts", "English")) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackFormatNames)
    }
  }

  test("should not create node from soundtrack if the language name locale does not match the current locale") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Soundtrack("en", "dts", LocalizedText("Angol")(HungarianLocale)))(AmericanLocale) }
    }
  }

  test("should not create node from soundtrack if the format name locale does not match the current locale") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Soundtrack("en", "dts", formatName = LocalizedText("DTS")(HungarianLocale)))(AmericanLocale) }
    }
  }

  test("should not create node from soundtrack if the soundtrack already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Soundtrack("soundtrack with id", "dts", null, null, id = 1))
    }
  }
  
  test("should create node from subtitle") {
    val subtitleNode = transaction(db) { subject.createNodeFrom(EnglishSubtitle) }
    getString(subtitleNode, SubtitleLanguageCode) should be(EnglishSubtitle.languageCode)
    Some(getLocalizedText(subtitleNode, SubtitleLanguageNames)) should be(EnglishSubtitle.languageName)
    getLong(subtitleNode, Version) should be(EnglishSubtitle.version)
    subtitleNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Subtitle]))
  }

  test("should create node from subtitle without language name") {
    val subtitleNode = transaction(db) { subject.createNodeFrom(Subtitle("en")) }
    intercept[NotFoundException] {
      subtitleNode.getProperty(SubtitleLanguageNames)
    }
  }

  test("should not create node from subtitle if the language name locale does not match the current locale") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(Subtitle("en", LocalizedText("Angol")(HungarianLocale)))(AmericanLocale) }
    }
  }

  test("should not create node from subtitle if the subtitle already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Subtitle("subtitle with id", null, id = 1))
    }
  }

  test("should not create node from unsupported entity") {
    intercept[IllegalArgumentException] {
      subject.createNodeFrom(new VersionedLongIdEntity(0, 0) {})
    }
  }

  test("should update actor node") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val anotherPersonNode = createNodeFrom(Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown"))
    val anotherCharacterNode = createNodeFrom(Character("Jamie"))
    val anotherMovieNode = createNodeFrom(Movie("Foo movie title"))
    val modifiedActor = Actor(createPersonFrom(anotherPersonNode), createCharacterFrom(anotherCharacterNode), createMovieFrom(anotherMovieNode), actor.version, actor.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedActor) }
    updatedNode.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode should be(anotherPersonNode)
    updatedNode.getSingleRelationship(PlayedBy, OUTGOING).getEndNode should be(anotherCharacterNode)
    updatedNode.getSingleRelationship(AppearedIn, OUTGOING).getEndNode should be(anotherMovieNode)
    getLong(updatedNode, Version) should be (actor.version + 1)
    updatedNode.getId should be(actor.id.get)
  }
  
  test("should not update actor node if the version of the actor does not match the version of the node") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val modifiedActor = Actor(insertEntity(Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown")), insertEntity(Character("Jamie")), insertEntity(Movie("Foo movie title")), actor.version + 1, actor.id.get)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(modifiedActor) }
    }
  }

  test("should not update actor node if the actor has an id referring to a node which does not exist in the database") {
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie), id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(actor) }
    }
  }

  test("should not update actor node if the actor has an id referring to a non actor node") {
    val node = createNode()
    val actor = Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie), id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(actor) }
    }
  }

  test("should not update actor node if the person does not exist in the database") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val person = Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown")
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(Actor(person, insertEntity(Johnny), insertEntity(TestMovie), actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the character does not exist in the database") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val character = Character("Jamie")
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(Actor(insertEntity(JohnDoe), character, insertEntity(TestMovie), actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the motion picture does not exist in the database") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val movie = Movie("Foo movie title")
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(Actor(insertEntity(JohnDoe), insertEntity(Johnny), movie, actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the person has an id referring to a node which does not exist in the database") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val person = Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown", id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(Actor(person, insertEntity(Johnny), insertEntity(TestMovie), actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the character has an id referring to a node which does not exist in the database") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val character = Character("Jamie", id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(Actor(insertEntity(JohnDoe), character, insertEntity(TestMovie), actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the motion picture has an id referring to a node which does not exist in the database") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val movie = Movie("Foo movie title", id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(Actor(insertEntity(JohnDoe), insertEntity(Johnny), movie, actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the person has an id referring to a non person node") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val node = createNode()
    val person = Person("James Doe", Male, new LocalDate(1970, 7, 7), "Anytown", id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(Actor(person, insertEntity(Johnny), insertEntity(TestMovie), actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the character has an id referring to a non character node") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val node = createNode()
    val character = Character("Jamie", id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(Actor(insertEntity(JohnDoe), character, insertEntity(TestMovie), actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the motion picture has an id referring to a non motion picture node") {
    val actor = insertEntity(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    val node = createNode()
    val movie = Movie("Foo movie title", id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(Actor(insertEntity(JohnDoe), insertEntity(Johnny), movie, actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the actor does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(Actor(insertEntity(JohnDoe), insertEntity(Johnny), insertEntity(TestMovie)))
    }
  }

  test("should update character node") {
    val character = insertEntity(Johnny)
    val modifiedCharacter = Character("Jenny", "foo", character.version, character.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedCharacter) }
    getString(updatedNode, CharacterName) should be("Jenny")
    getString(updatedNode, CharacterDiscriminator) should be("foo")
    getLong(updatedNode, Version) should be(modifiedCharacter.version + 1)
    updatedNode.getId should be(character.id.get)
  }
  
  test("should not update character node if the version of the character does not match the version of the node") {
    val character = insertEntity(Johnny)
    val modifiedCharacter = Character("Jenny", "foo", character.version + 1, character.id.get)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(modifiedCharacter) }
    }
  }

  test("should not update character node if the character has an id referring to a node which does not exist in the database") {
    val character = Character(Johnny.name, Johnny.discriminator, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(character) }
    }
  }

  test("should not update character node if the character has an id referring to a non character node") {
    val node = createNode()
    val character = Character(Johnny.name, Johnny.discriminator, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(character) }
    }
  }

  test("should not update character node if the character does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(Johnny)
    }
  }
  
  test("should update digital container node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    val anotherMovieNode = createNodeFrom(Movie("Foo movie title"))
    val italianSoundtrackNode = createNodeFrom(Soundtrack("it", "dts"))
    val italianSubtitleNode = createNodeFrom(Subtitle("it"))
    val modifiedDigitalContainer = DigitalContainer(createMovieFrom(anotherMovieNode), Set(createSoundtrackFrom(italianSoundtrackNode)), Set(createSubtitleFrom(italianSubtitleNode)), digitalContainer.version, digitalContainer.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedDigitalContainer) }
    updatedNode.getSingleRelationship(StoredIn, INCOMING).getStartNode should be(anotherMovieNode)
    updatedNode.getRelationships(WithSoundtrack, OUTGOING).map(_.getEndNode).toSet should be(Set(italianSoundtrackNode))
    updatedNode.getRelationships(WithSubtitle, OUTGOING).map(_.getEndNode).toSet should be(Set(italianSubtitleNode))
    getLong(updatedNode, Version) should be(modifiedDigitalContainer.version + 1)
    updatedNode.getId should be(digitalContainer.id.get)
  }

  test("should not update digital container node if the version of the digital container does not match the version of the node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    val modifiedDigitalContainer = DigitalContainer(insertEntity(Movie("Foo movie title")), Set(insertEntity(Soundtrack("it", "dts"))), Set(insertEntity(Subtitle("it"))), digitalContainer.version + 1, digitalContainer.id.get)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(modifiedDigitalContainer) }
    }
  }

  test("should not update digital container node if the digital container has an id referring to a node which does not exist in the database") {
    val digitalContainer = DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)), id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(digitalContainer) }
    }
  }

  test("should not update digital container node if the digital container has an id referring to a non digital container node") {
    val node = createNode()
    val digitalContainer = DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)), id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(digitalContainer) }
    }
  }

  test("should not update digital container node if the motion picture does not exist in the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val movie = Movie("Foo movie title")
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(movie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if any of the soundtracks does not exist in the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val soundtrack = Soundtrack("it", "dts")
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(insertEntity(TestMovie), Set(soundtrack), Set(insertEntity(EnglishSubtitle)), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if any of the subtitles does not exist in the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val subtitle = Subtitle("it")
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(subtitle), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if the motion picture has an id referring to a node which does not exist in the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val movie = Movie("Foo movie title", id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(movie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if any of the soundtracks has an id referring to a node which does not exist in the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val soundtrack = Soundtrack("it", "dts", id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(insertEntity(TestMovie), Set(soundtrack), Set(insertEntity(EnglishSubtitle)), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if any of the subtitles has an id referring to a node which does not exist in the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val subtitle = Subtitle("it", id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(subtitle), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if the motion picture has an id referring to a non motion picture node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val node = createNode()
    val movie = Movie("Foo movie title", id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(movie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if any of the soundtracks has an id referring to a non soundtrack node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val node = createNode()
    val soundtrack = Soundtrack("it", "dts", id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(insertEntity(TestMovie), Set(soundtrack), Set(insertEntity(EnglishSubtitle)), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if any of the subtitles has an id referring to a non subtitle node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val node = createNode()
    val subtitle = Subtitle("it", id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(subtitle), id = digitalContainer.id.get)) }
    }
  }

  test("should not update digital container node if the digital container does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    }
  }

  test("should update movie node") {
    val movie = insertEntity(TestMovie)
    val modifiedMovie = Movie("Foo movie title", Set(LocalizedText("Foo film cím")(HungarianLocale), LocalizedText("Foo film titolo")(ItalianLocale)), Duration.standardMinutes(100), new LocalDate(2012, 1, 30), movie.version, movie.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedMovie) }
    getLocalizedText(updatedNode, MovieOriginalTitle) should be(LocalizedText("Foo movie title"))
    getLocalizedTextSet(updatedNode, MovieLocalizedTitles) should be(Set(LocalizedText("Foo film cím")(HungarianLocale), LocalizedText("Foo film titolo")(ItalianLocale)))
    getDuration(updatedNode, MovieLength) should be(Duration.standardMinutes(100))
    getLocalDate(updatedNode, MovieReleaseDate) should be(new LocalDate(2012, 1, 30))
    getLong(updatedNode, Version) should be(modifiedMovie.version + 1)
    updatedNode.getId should be(movie.id.get)
  }

  test("should not update movie node if the version of the movie does not match the version of the node") {
    val movie = insertEntity(TestMovie)
    val modifiedMovie = Movie("Foo movie title", version = movie.version + 1, id = movie.id.get)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(modifiedMovie) }
    }
  }

  test("should not update movie node if the movie has an id referring to a node which does not exist in the database") {
    val movie = Movie(TestMovie.originalTitle, TestMovie.localizedTitles, TestMovie.length, TestMovie.releaseDate, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(movie) }
    }
  }

  test("should not update movie node if the movie has an id referring to a non movie node") {
    val node = createNode()
    val movie = Movie(TestMovie.originalTitle, TestMovie.localizedTitles, TestMovie.length, TestMovie.releaseDate, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(movie) }
    }
  }

  test("should not update movie node if the movie does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(TestMovie)
    }
  }

  test("should update person node") {
    val person = insertEntity(JohnDoe)
    val modifiedPerson = Person("Jane Doe", Female, new LocalDate(1990, 9, 9), "Anyville", person.version, person.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedPerson) }
    getString(updatedNode, PersonName) should be("Jane Doe")
    getString(updatedNode, PersonGender) should be(Female.toString)
    getLocalDate(updatedNode, PersonDateOfBirth) should be(new LocalDate(1990, 9, 9))
    getString(updatedNode, PersonPlaceOfBirth) should be("Anyville")
    getLong(updatedNode, Version) should be(modifiedPerson.version + 1)
    updatedNode.getId should be(person.id.get)
  }

  test("should not update person node if the version of the person does not match the version of the node") {
    val person = insertEntity(JohnDoe)
    val modifiedPerson = Person("Jane Doe", Female, new LocalDate(1990, 9, 9), "Anyville", person.version + 1, person.id.get)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(modifiedPerson) }
    }
  }

  test("should not update person node if the person has an id referring to a node which does not exist in the database") {
    val person = Person(JohnDoe.name, JohnDoe.gender, JohnDoe.dateOfBirth, JohnDoe.placeOfBirth, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(person) }
    }
  }

  test("should not update person node if the person has an id referring to a non person node") {
    val node = createNode()
    val person = Person(JohnDoe.name, JohnDoe.gender, JohnDoe.dateOfBirth, JohnDoe.placeOfBirth, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(person) }
    }
  }

  test("should not update person node if the person does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(JohnDoe)
    }
  }

  test("should update soundtrack node") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("foo", "bar", "foo language", "bar format", soundtrack.version, soundtrack.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSoundtrack) }
    getString(updatedNode, SoundtrackLanguageCode) should be("foo")
    getString(updatedNode, SoundtrackFormatCode) should be("bar")
    getLocalizedText(updatedNode, SoundtrackLanguageNames) should be(LocalizedText("foo language"))
    getLocalizedText(updatedNode, SoundtrackFormatNames) should be(LocalizedText("bar format"))
    getLong(updatedNode, Version) should be(modifiedSoundtrack.version + 1)
    updatedNode.getId should be(soundtrack.id.get)
  }
  
  test("should add the soundtrack language and format names to the node properties") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("en", "dts", LocalizedText("Angol")(HungarianLocale), LocalizedText("DTS")(HungarianLocale), soundtrack.version, soundtrack.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSoundtrack)(HungarianLocale) }
    getLocalizedTextSet(updatedNode, SoundtrackLanguageNames) should be(Set(LocalizedText("English"), LocalizedText("Angol")(HungarianLocale)))
    getLocalizedTextSet(updatedNode, SoundtrackFormatNames) should be(Set(LocalizedText("DTS"), LocalizedText("DTS")(HungarianLocale)))
  }

  test("should remove the soundtrack language and format names from the node properties") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("en", "dts", null, null, soundtrack.version, soundtrack.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSoundtrack) }
    hasLocalizedText(updatedNode, SoundtrackLanguageNames) should be(false)
    hasLocalizedText(updatedNode, SoundtrackFormatNames) should be(false)
  }

  test("should not update soundtrack node if the version of the soundtrack does not match the version of the node") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("foo", "bar", version = soundtrack.version + 1, id = soundtrack.id.get)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(modifiedSoundtrack) }
    }
  }

  test("should not update soundtrack node if the language name locale does not match the current locale") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(Soundtrack("en", "dts", LocalizedText("Angol")(HungarianLocale)))(AmericanLocale)
    }
  }

  test("should not update soundtrack node if the format name locale does not match the current locale") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(Soundtrack("en", "dts", formatName = LocalizedText("DTS")(HungarianLocale)))(AmericanLocale)
    }
  }

  test("should not update soundtrack node if the soundtrack has an id referring to a node which does not exist in the database") {
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, EnglishSoundtrack.languageName.get, EnglishSoundtrack.formatName.get, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(soundtrack) }
    }
  }

  test("should not update soundtrack node if the soundtrack has an id referring to a non soundtrack node") {
    val node = createNode()
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, EnglishSoundtrack.languageName.get, EnglishSoundtrack.formatName.get, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(soundtrack) }
    }
  }

  test("should not update soundtrack node if the soundtrack does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(EnglishSoundtrack)
    }
  }

  test("should update subtitle node") {
    val subtitle = insertEntity(EnglishSubtitle)
    val modifiedSubtitle = Subtitle("foo", "foo language", subtitle.version, subtitle.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSubtitle) }
    getString(updatedNode, SubtitleLanguageCode) should be("foo")
    getLocalizedText(updatedNode, SubtitleLanguageNames) should be(LocalizedText("foo language"))
    getLong(updatedNode, Version) should be(modifiedSubtitle.version + 1)
    updatedNode.getId should be(subtitle.id.get)
  }

  test("should add the subtitle language name to the node properties") {
    val subtitle = insertEntity(EnglishSubtitle)
    val modifiedSubtitle = Subtitle("en", LocalizedText("Angol")(HungarianLocale), subtitle.version, subtitle.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSubtitle)(HungarianLocale) }
    getLocalizedTextSet(updatedNode, SubtitleLanguageNames) should be(Set(LocalizedText("English"), LocalizedText("Angol")(HungarianLocale)))
  }

  test("should remove the subtitle language name from the node properties") {
    val subtitle = insertEntity(EnglishSubtitle)
    val modifiedSubtitle = Subtitle("en", null, subtitle.version, subtitle.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSubtitle) }
    hasLocalizedText(updatedNode, SubtitleLanguageNames) should be(false)
  }

  test("should not update subtitle node if the version of the subtitle does not match the version of the node") {
    val subtitle = insertEntity(EnglishSubtitle)
    val modifiedSubtitle = Subtitle("foo", version = subtitle.version + 1, id = subtitle.id.get)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(modifiedSubtitle) }
    }
  }

  test("should not update subtitle node if the language name locale does not match the current locale") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(Subtitle("en", LocalizedText("Angol")(HungarianLocale)))(AmericanLocale)
    }
  }

  test("should not update subtitle node if the subtitle has an id referring to a node which does not exist in the database") {
    val subtitle = Subtitle(EnglishSubtitle.languageCode, EnglishSubtitle.languageName.get, id = getNodeCount + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.updateNodeOf(subtitle) }
    }
  }

  test("should not update subtitle node if the subtitle has an id referring to a non subtitle node") {
    val node = createNode()
    val subtitle = Subtitle(EnglishSubtitle.languageCode, EnglishSubtitle.languageName.get, id = node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.updateNodeOf(subtitle) }
    }
  }

  test("should not update subtitle node if the subtitle does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(EnglishSubtitle)
    }
  }

  test("should not update unsupported entity") {
    intercept[IllegalArgumentException] {
      subject.updateNodeOf(new VersionedLongIdEntity(0, 1) {})
    }
  }
}
