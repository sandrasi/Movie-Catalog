package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConversions._
import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.neo4j.graphdb.Direction._
import org.neo4j.graphdb.{Node, NotFoundException}
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
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.LocalizedTextManager._

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
    characterNode.getProperty(CharacterName) should be(Johnny.name)
    characterNode.getProperty(CharacterDiscriminator) should be(Johnny.discriminator)
    characterNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Character]))
  }

  test("should not create node from character if the character already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(new Character("Character with id", "", 1))
    }
  }
  
  test("should create node from digital container") {
    val movieNode = createNode(TestMovie)
    val englishSoundtrackNode = createNode(EnglishSoundtrack)
    val hungarianSoundtrackNode = createNode(HungarianSoundtrack)
    val englishSubtitleNode = createNode(EnglishSubtitle)
    val hungarianSubtitleNode = createNode(HungarianSubtitle)
    val digitalContainerNode = transaction(db) { subject.createNodeFrom(DigitalContainer(createMovieEntity(movieNode), Set(createSoundtrackEntity(englishSoundtrackNode), createSoundtrackEntity(hungarianSoundtrackNode)), Set(createSubtitleEntity(englishSubtitleNode), createSubtitleEntity(hungarianSubtitleNode)))) }
    digitalContainerNode.getSingleRelationship(StoredIn, INCOMING).getStartNode should be(movieNode)
    digitalContainerNode.getRelationships(WithSoundtrack, OUTGOING).map(_.getEndNode).toSet should be(Set(englishSoundtrackNode, hungarianSoundtrackNode))
    digitalContainerNode.getRelationships(WithSubtitle, OUTGOING).map(_.getEndNode).toSet should be(Set(englishSubtitleNode, hungarianSubtitleNode))
    digitalContainerNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[DigitalContainer]))
  }
  
  test("should not create node from the digital container if the motion picture does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(TestMovie, Set(saveEntity(EnglishSoundtrack)), Set(saveEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(saveEntity(TestMovie), Set(EnglishSoundtrack), Set(saveEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles does not exist in the database") {
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(saveEntity(TestMovie), Set(saveEntity(EnglishSoundtrack)), Set(EnglishSubtitle))) }
    }
  }

  test("should not create node from the digital container if the motion picture has an id referring to a node which does not exist in the database") {
    val movie = Movie(TestMovie.originalTitle, TestMovie.localizedTitles, TestMovie.length, TestMovie.releaseDate, db.getAllNodes.iterator().size + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(movie, Set(saveEntity(EnglishSoundtrack)), Set(saveEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks has an id referring to a node which does not exist in the database") {
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, EnglishSoundtrack.languageName, EnglishSoundtrack.formatName, db.getAllNodes.iterator().size + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(saveEntity(TestMovie), Set(soundtrack), Set(saveEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles has an id referring to a node which does not exist in the database") {
    val subtitle = Subtitle(EnglishSubtitle.languageCode, EnglishSubtitle.languageName, db.getAllNodes.iterator().size + 1)
    intercept[IllegalStateException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(saveEntity(TestMovie), Set(saveEntity(EnglishSoundtrack)), Set(subtitle))) }
    }
  }

  test("should not create node from the digital container if the motion picture has an id referring to a non motion picture node") {
    val node = createNode()
    val movie = Movie(TestMovie.originalTitle, TestMovie.localizedTitles, TestMovie.length, TestMovie.releaseDate, node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(movie, Set(saveEntity(EnglishSoundtrack)), Set(saveEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the soundtracks has an id referring to a non soundtrack node") {
    val node = createNode()
    val soundtrack = Soundtrack(EnglishSoundtrack.languageCode, EnglishSoundtrack.formatCode, EnglishSoundtrack.languageName, EnglishSoundtrack.formatName, node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(saveEntity(TestMovie), Set(soundtrack), Set(saveEntity(EnglishSubtitle)))) }
    }
  }

  test("should not create node from the digital container if any of the subtitles has an id referring to a non subtitle node") {
    val node = createNode()
    val subtitle = Subtitle(EnglishSubtitle.languageCode, EnglishSubtitle.languageName, node.getId)
    intercept[ClassCastException] {
      transaction(db) { subject.createNodeFrom(DigitalContainer(saveEntity(TestMovie), Set(saveEntity(EnglishSoundtrack)), Set(subtitle))) }
    }
  }
  
  test("should not create node from the digital container if the digital container already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(DigitalContainer(saveEntity(TestMovie), Set(saveEntity(EnglishSoundtrack)), Set(saveEntity(EnglishSubtitle)), 1))
    }
  }

  test("should create node from movie") {
    val movieNode = transaction(db) { subject.createNodeFrom(TestMovie) }
    movieNode.getProperty(MovieLength) should be(TestMovie.length.getMillis)
    movieNode.getProperty(MovieReleaseDate) should be(TestMovie.releaseDate.toDateTimeAtStartOfDay.getMillis)
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
    personNode.getProperty(PersonName) should be(JohnDoe.name)
    personNode.getProperty(PersonGender) should be(JohnDoe.gender.toString)
    personNode.getProperty(PersonDateOfBirth) should be(JohnDoe.dateOfBirth.toDateTimeAtStartOfDay.getMillis)
    personNode.getProperty(PersonPlaceOfBirth) should be(JohnDoe.placeOfBirth)
    personNode.getSingleRelationship(IsA, OUTGOING).getEndNode.getId should be(subrefNodeSupp.getSubrefNodeIdFor(classOf[Person]))
  }

  test("should not create node from person if the person already has an id") {
    intercept[IllegalStateException] {
      subject.createNodeFrom(new Person("Person With Id", Gender.Male, new LocalDate(1980, 8, 8), "Anytown", 1))
    }
  }

  test("should create node from soundtrack") {
    val soundtrackNode = transaction(db) { subject.createNodeFrom(EnglishSoundtrack) }
    soundtrackNode.getProperty(SoundtrackLanguageCode) should be(EnglishSoundtrack.languageCode)
    Some(getLocalizedText(soundtrackNode, SoundtrackLanguageNames)) should be(EnglishSoundtrack.languageName)
    soundtrackNode.getProperty(SoundtrackFormatCode) should be(EnglishSoundtrack.formatCode)
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
    subtitleNode.getProperty(SubtitleLanguageCode) should be(EnglishSubtitle.languageCode)
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
  }
  
  test("should add the soundtrack language and format names to the node properties") {
  }

  test("should remove the soundtrack language and format names from the node properties") {
  }
}
