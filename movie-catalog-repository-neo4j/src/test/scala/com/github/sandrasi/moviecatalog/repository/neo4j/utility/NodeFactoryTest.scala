package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConversions._
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.neo4j.graphdb.Direction._
import org.neo4j.graphdb.NotFoundException
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container._
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.NodePropertyManager._

@RunWith(classOf[JUnitRunner])
class NodeFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: NodeFactory = _

  override protected def beforeEach() {
    subject = NodeFactory(db)
  }

  test("should return the same node factory instance for the same database") {
    subject should be theSameInstanceAs(NodeFactory(db))
  }

  test("should return different node factory instances for different databases") {
    subject should not be theSameInstanceAs(NodeFactory(createTempDb()))
  }

  test("should not instantiate node factory if the database is null") {
    intercept[IllegalArgumentException] {
      NodeFactory(null)
    }
  }

  test("should create node from character") {
    val characterNode = transaction(db) { subject.createNodeFrom(Johnny) }
    getString(characterNode, CharacterName) should be(Johnny.name)
    getString(characterNode, CharacterDiscriminator) should be(Johnny.discriminator)
    characterNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Character]))
  }

  test("should not create node from character if the character already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(new Character("Character with id", "", 1))
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
    val movie = Movie(TestMovie.originalTitle, TestMovie.localizedTitles, TestMovie.length, TestMovie.releaseDate, db.getAllNodes.iterator().size + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(movie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks has an id referring to a node which does not exist in the database") {
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, EnglishSoundtrack.languageName, EnglishSoundtrack.formatName, db.getAllNodes.iterator().size + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(soundtrack), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles has an id referring to a node which does not exist in the database") {
    val subtitle = Subtitle(EnglishSubtitle.languageCode, EnglishSubtitle.languageName, db.getAllNodes.iterator().size + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(subtitle))) }
    }
  }

  test("should not create node from the digital container if the motion picture has an id referring to a non motion picture node") {
    val node = createNode()
    val movie = Movie(TestMovie.originalTitle, TestMovie.localizedTitles, TestMovie.length, TestMovie.releaseDate, node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(movie, Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks has an id referring to a non soundtrack node") {
    val node = createNode()
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, EnglishSoundtrack.languageName, EnglishSoundtrack.formatName, node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(soundtrack), Set(insertEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles has an id referring to a non subtitle node") {
    val node = createNode()
    val subtitle = Subtitle(EnglishSubtitle.languageCode, EnglishSubtitle.languageName, node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(subtitle))) }
    }
  }
  
  test("should not create node from the digital container if the digital container already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(DigitalContainer(insertEntity(TestMovie), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)), 1))
    }
  }

  test("should create node from movie") {
    val movieNode = transaction(db) { subject.createNodeFrom(TestMovie) }
    getDuration(movieNode, MovieLength) should be(TestMovie.length)
    getLocalDate(movieNode, MovieReleaseDate) should be(TestMovie.releaseDate)
    getLocalizedText(movieNode, MovieOriginalTitle) should be(TestMovie.originalTitle)
    getLocalizedTextSet(movieNode, MovieLocalizedTitles) should be(TestMovie.localizedTitles)
    movieNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Movie]))
  }
  
  test("should not create node from movie if the movie already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(new Movie(LocalizedText("Movie with id"), Set(), Duration.standardMinutes(90), new LocalDate(2011, 1, 1), 1))
    }
  }

  test("should create node from person") {
    val personNode = transaction(db) { subject.createNodeFrom(JohnDoe) }
    getString(personNode, PersonName) should be(JohnDoe.name)
    getString(personNode, PersonGender) should be(JohnDoe.gender.toString)
    getLocalDate(personNode, PersonDateOfBirth) should be(JohnDoe.dateOfBirth)
    getString(personNode, PersonPlaceOfBirth) should be(JohnDoe.placeOfBirth)
    personNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Person]))
  }

  test("should not create node from person if the person already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(new Person("Person With Id", Gender.Male, new LocalDate(1980, 8, 8), "Anytown", 1))
    }
  }

  test("should create node from soundtrack") {
    val soundtrackNode = transaction(db) { subject.createNodeFrom(EnglishSoundtrack) }
    getString(soundtrackNode, SoundtrackLanguageCode) should be(EnglishSoundtrack.languageCode)
    getString(soundtrackNode, SoundtrackFormatCode) should be(EnglishSoundtrack.formatCode)
    Some(getLocalizedText(soundtrackNode, SoundtrackLanguageNames)) should be(EnglishSoundtrack.languageName)
    Some(getLocalizedText(soundtrackNode, SoundtrackFormatNames)) should be(EnglishSoundtrack.formatName)
    soundtrackNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Soundtrack]))
  }

  test("should create node from soundtrack without language name") {
    val soundtrackNode = transaction(db) { subject.createNodeFrom(Soundtrack("en", "dts", None, Some(LocalizedText("DTS")))) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackLanguageNames)
    }
  }

  test("should create node from soundtrack without format name") {
    val soundtrackNode = transaction(db) { subject.createNodeFrom(Soundtrack("en", "dts", Some(LocalizedText("English")), None)) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackFormatNames)
    }
  }

  test("should not create node from soundtrack if the soundtrack already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Soundtrack("soundtrack with id", "dts", None, None, 1))
    }
  }
  
  test("should create node from subtitle") {
    val subtitleNode = transaction(db) { subject.createNodeFrom(EnglishSubtitle) }
    getString(subtitleNode, SubtitleLanguageCode) should be(EnglishSubtitle.languageCode)
    Some(getLocalizedText(subtitleNode, SubtitleLanguageNames)) should be(EnglishSubtitle.languageName)
    subtitleNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Subtitle]))
  }

  test("should create node from subtitle without language name") {
    val subtitleNode = transaction(db) { subject.createNodeFrom(Subtitle("en", None)) }
    intercept[NotFoundException] {
      subtitleNode.getProperty(SubtitleLanguageNames)
    }
  }

  test("should not create node from subtitle if the subtitle already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(Subtitle("subtitle with id", None, 1))
    }
  }

  test("should not create node from unsupported entity") {
    intercept[IllegalArgumentException] {
      subject.createNodeFrom(new LongIdEntity(0) {})
    }
  }

  test("should update soundtrack node") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("foo", "bar", Some(LocalizedText("foo language")), Some(LocalizedText("bar format")), soundtrack.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSoundtrack) }
    getString(updatedNode, SoundtrackLanguageCode) should be("foo")
    getString(updatedNode, SoundtrackFormatCode) should be("bar")
    getLocalizedText(updatedNode, SoundtrackLanguageNames) should be(LocalizedText("foo language"))
    getLocalizedText(updatedNode, SoundtrackFormatNames) should be(LocalizedText("bar format"))
    updatedNode.getId should be(soundtrack.id.get)
  }
  
  test("should add the soundtrack language and format names to the node properties") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("en", "dts", Some(LocalizedText("Angol", HungarianLocale)), Some(LocalizedText("DTS", HungarianLocale)), soundtrack.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSoundtrack) }
    getLocalizedTextSet(updatedNode, SoundtrackLanguageNames) should be(Set(LocalizedText("English"), LocalizedText("Angol", HungarianLocale)))
    getLocalizedTextSet(updatedNode, SoundtrackFormatNames) should be(Set(LocalizedText("DTS"), LocalizedText("DTS", HungarianLocale)))
  }

  test("should remove the soundtrack language and format names from the node properties") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("en", "dts", None, None, soundtrack.id.get)
    val updatedNode = transaction(db) { subject.updateNodeOf(modifiedSoundtrack) }
    hasLocalizedText(updatedNode, SoundtrackLanguageNames) should be(false)
    hasLocalizedText(updatedNode, SoundtrackFormatNames) should be(false)
  }

  test("should not update the soundtrack if it does not have an id") {
    intercept[IllegalStateException] {
      subject.updateNodeOf(EnglishSoundtrack)
    }
  }

  test("should not update unsupported entity") {
    intercept[IllegalArgumentException] {
      subject.updateNodeOf(new LongIdEntity(1) {})
    }
  }
}
