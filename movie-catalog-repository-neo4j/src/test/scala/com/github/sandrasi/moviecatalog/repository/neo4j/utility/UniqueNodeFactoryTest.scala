package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import scala.collection.JavaConverters._
import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import org.neo4j.graphdb.Direction._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{AbstractCast, Actor}
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}
import com.github.sandrasi.moviecatalog.domain.entities.core.{MotionPicture, Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.FilmCrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import org.neo4j.graphdb.NotFoundException
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity

@RunWith(classOf[JUnitRunner])
class UniqueNodeFactoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: UniqueNodeFactory = _

  override protected def beforeEach() {
    subject = UniqueNodeFactory(db)
  }

  test("should return the same unique node factory instance for the same database") {
    subject should be theSameInstanceAs(UniqueNodeFactory(db))
  }

  test("should return different unique node factory instances for different databases") {
    subject should not be theSameInstanceAs(UniqueNodeFactory(createTempDb()))
  }

  test("should not instantiate the unique node factory if the database is null") {
    intercept[IllegalArgumentException] {
      UniqueNodeFactory(null)
    }
  }

  test("should create node from actor") {
    val personNode = createNodeFrom(JohnTravolta)
    val characterNode = createNodeFrom(VincentVega)
    val movieNode = createNodeFrom(PulpFiction)
    implicit val tx = db.beginTx()
    val actorNode = transaction(tx) { subject.createNodeFrom(Actor(createPersonFrom(personNode), createCharacterFrom(characterNode), createMovieFrom(movieNode))) }
    actorNode.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode should be(personNode)
    actorNode.getSingleRelationship(Played, OUTGOING).getEndNode should be(characterNode)
    actorNode.getSingleRelationship(AppearedIn, OUTGOING).getEndNode should be(movieNode)
    getLong(actorNode, Version) should be(0)
    actorNode.getRelationships(IsA, OUTGOING).iterator().asScala.map(_.getEndNode).toTraversable should(contain(dbMgr.getSubreferenceNode(classOf[AbstractCast])) and contain(dbMgr.getSubreferenceNode(classOf[Actor])))
  }
  
  test("should not create node from actor if a node already exists for that actor") {
    val actor = Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(actor) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(actor) }
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

  test("should create node from character") {
    implicit val tx = db.beginTx()
    val characterNode = transaction(tx) { subject.createNodeFrom(VincentVega) }
    getString(characterNode, CharacterName) should be(VincentVega.name)
    getLong(characterNode, Version) should be(VincentVega.version)
    characterNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[Character]))
  }

  test("should not create node from character if a node already exists for that character") {
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(VincentVega) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(VincentVega) }
    }
  }

  test("should create node from character if the name is different") {
    val anotherCharacter = Character("Jules Winnfield")
    implicit val tx = db.beginTx()
    transaction(tx) {
      val characterNode = subject.createNodeFrom(VincentVega)
      val anotherCharacterNode = subject.createNodeFrom(anotherCharacter)
      characterNode.getId should not equal(anotherCharacterNode.getId)
    }
  }

  test("should create node from digital container") {
    val movieNode = createNodeFrom(PulpFiction)
    val englishSoundtrackNode = createNodeFrom(EnglishSoundtrack)
    val hungarianSoundtrackNode = createNodeFrom(HungarianSoundtrack)
    val englishSubtitleNode = createNodeFrom(EnglishSubtitle)
    val hungarianSubtitleNode = createNodeFrom(HungarianSubtitle)
    implicit val tx = db.beginTx()
    val digitalContainerNode = transaction(tx) { subject.createNodeFrom(DigitalContainer(createMovieFrom(movieNode), Set(createSoundtrackFrom(englishSoundtrackNode), createSoundtrackFrom(hungarianSoundtrackNode)), Set(createSubtitleFrom(englishSubtitleNode), createSubtitleFrom(hungarianSubtitleNode)))) }
    digitalContainerNode.getSingleRelationship(WithContent, OUTGOING).getEndNode should be(movieNode)
    digitalContainerNode.getRelationships(WithSoundtrack, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(englishSoundtrackNode, hungarianSoundtrackNode))
    digitalContainerNode.getRelationships(WithSubtitle, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(englishSubtitleNode, hungarianSubtitleNode))
    getLong(digitalContainerNode, Version) should be(0)
    digitalContainerNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[DigitalContainer]))
  }

  test("should not create node from digital container if a node already exists for that digital container") {
    val digitalContainer = DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle)))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(digitalContainer) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(digitalContainer) }
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

  test("should create node from movie") {
    implicit val tx = db.beginTx()
    val movieNode = transaction(tx) { subject.createNodeFrom(PulpFiction) }
    getLocalizedText(movieNode, MovieOriginalTitle) should be(PulpFiction.originalTitle)
    getLocalizedTextSet(movieNode, MovieLocalizedTitles) should be(PulpFiction.localizedTitles)
    getLocalDate(movieNode, MovieReleaseDate) should be(PulpFiction.releaseDate)
    getDuration(movieNode, MovieRuntime) should be(PulpFiction.runtime)
    getLong(movieNode, Version) should be(PulpFiction.version)
    movieNode.getRelationships(IsA, OUTGOING).iterator().asScala.map(_.getEndNode).toTraversable should(contain(dbMgr.getSubreferenceNode(classOf[MotionPicture])) and contain(dbMgr.getSubreferenceNode(classOf[Movie])))
  }

  test("should not create node from movie if a node already exists for that movie") {
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(PulpFiction) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(PulpFiction) }
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

  test("should create node from person") {
    implicit val tx = db.beginTx()
    val personNode = transaction(tx) { subject.createNodeFrom(JohnTravolta) }
    getString(personNode, PersonName) should be(JohnTravolta.name)
    getString(personNode, PersonGender) should be(JohnTravolta.gender.toString)
    getLocalDate(personNode, PersonDateOfBirth) should be(JohnTravolta.dateOfBirth)
    getString(personNode, PersonPlaceOfBirth) should be(JohnTravolta.placeOfBirth)
    getLong(personNode, Version) should be(JohnTravolta.version)
    personNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[Person]))
  }

  test("should not create node from person if a node already exists for that person") {
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(JohnTravolta) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(JohnTravolta) }
    }
  }

  test("should create node from person if the name is different") {
    val anotherPerson = Person("Samuel Leroy Jackson", JohnTravolta.gender, JohnTravolta.dateOfBirth, JohnTravolta.placeOfBirth)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(anotherPerson)
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from person if the gender is different") {
    val anotherPerson = Person(JohnTravolta.name, Female, JohnTravolta.dateOfBirth, JohnTravolta.placeOfBirth)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(anotherPerson)
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from person if the date of birth is different") {
    val anotherPerson = Person(JohnTravolta.name, JohnTravolta.gender, new LocalDate(1948, 12, 21), JohnTravolta.placeOfBirth)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(anotherPerson)
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from person if the place of birth is different") {
    val anotherPerson = Person(JohnTravolta.name, JohnTravolta.gender, JohnTravolta.dateOfBirth, "Washington, D.C., U.S.")
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(anotherPerson)
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from soundtrack") {
    implicit val tx = db.beginTx()
    val soundtrackNode = transaction(tx) { subject.createNodeFrom(EnglishSoundtrack) }
    getString(soundtrackNode, SoundtrackLanguageCode) should be(EnglishSoundtrack.languageCode)
    getString(soundtrackNode, SoundtrackFormatCode) should be(EnglishSoundtrack.formatCode)
    Some(getLocalizedText(soundtrackNode, SoundtrackLanguageNames)) should be(EnglishSoundtrack.languageName)
    Some(getLocalizedText(soundtrackNode, SoundtrackFormatNames)) should be(EnglishSoundtrack.formatName)
    getLong(soundtrackNode, Version) should be(EnglishSoundtrack.version)
    soundtrackNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[Soundtrack]))
  }

  test("should create node from soundtrack without language name") {
    implicit val tx = db.beginTx()
    val soundtrackNode = transaction(tx) { subject.createNodeFrom(Soundtrack("en", "dts", formatName = "DTS")) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackLanguageNames)
    }
  }

  test("should create node from soundtrack without format name") {
    implicit val tx = db.beginTx()
    val soundtrackNode = transaction(tx) { subject.createNodeFrom(Soundtrack("en", "dts", "English")) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackFormatNames)
    }
  }

  test("should not create node from soundtrack if the language name locale does not match the current locale") {
    implicit val tx = db.beginTx()
    implicit val locale = AmericanLocale
    intercept[IllegalStateException] {
      transaction(tx) { subject.createNodeFrom(Soundtrack("en", "dts", LocalizedText("Angol")(HungarianLocale))) }
    }
  }

  test("should not create node from soundtrack if the format name locale does not match the current locale") {
    implicit val tx = db.beginTx()
    implicit val locale = AmericanLocale
    intercept[IllegalStateException] {
      transaction(tx) { subject.createNodeFrom(Soundtrack("en", "dts", formatName = LocalizedText("DTS")(HungarianLocale))) }
    }
  }

  test("should not create node from soundtrack if a node already exists for that soundtrack") {
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(EnglishSoundtrack) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(EnglishSoundtrack) }
    }
  }

  test("should create node from soundtrack if the language code is different") {
    val anotherSoundtrack = Soundtrack("hu", EnglishSoundtrack.formatCode, EnglishSoundtrack.languageName.get, EnglishSoundtrack.formatName.get)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val soundtrackNode = subject.createNodeFrom(EnglishSoundtrack)
      val anotherSoundtrackNode = subject.createNodeFrom(anotherSoundtrack)
      soundtrackNode.getId should not equal(anotherSoundtrackNode.getId)
    }
  }

  test("should create node from soundtrack if the format code is different") {
    val anotherSoundtrack = Soundtrack(EnglishSoundtrack.languageCode, "dd5.1", EnglishSoundtrack.languageName.get, EnglishSoundtrack.formatName.get)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val soundtrackNode = subject.createNodeFrom(EnglishSoundtrack)
      val anotherSoundtrackNode = subject.createNodeFrom(anotherSoundtrack)
      soundtrackNode.getId should not equal(anotherSoundtrackNode.getId)
    }
  }

  test("should create node from subtitle") {
    implicit val tx = db.beginTx()
    val subtitleNode = transaction(tx) { subject.createNodeFrom(EnglishSubtitle) }
    getString(subtitleNode, SubtitleLanguageCode) should be(EnglishSubtitle.languageCode)
    Some(getLocalizedText(subtitleNode, SubtitleLanguageNames)) should be(EnglishSubtitle.languageName)
    getLong(subtitleNode, Version) should be(EnglishSubtitle.version)
    subtitleNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[Subtitle]))
  }

  test("should create node from subtitle without language name") {
    implicit val tx = db.beginTx()
    val subtitleNode = transaction(tx) { subject.createNodeFrom(Subtitle("en")) }
    intercept[NotFoundException] {
      subtitleNode.getProperty(SubtitleLanguageNames)
    }
  }

  test("should not create node from subtitle if the language name locale does not match the current locale") {
    implicit val tx = db.beginTx()
    implicit val locale = AmericanLocale
    intercept[IllegalStateException] {
      transaction(tx) { subject.createNodeFrom(Subtitle("en", LocalizedText("Angol")(HungarianLocale))) }
    }
  }

  test("should not create node from subtitle if a node already exists for that subtitle") {
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(EnglishSubtitle) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(EnglishSubtitle) }
    }
  }

  test("should create node from subtitle if the language code is different") {
    val anotherSubtitle = Subtitle("hu", EnglishSubtitle.languageName.get)
    implicit val tx = db.beginTx()
    transaction(tx) {
      val soundtrackNode = subject.createNodeFrom(EnglishSubtitle)
      val anotherSoundtrackNode = subject.createNodeFrom(anotherSubtitle)
      soundtrackNode.getId should not equal(anotherSoundtrackNode.getId)
    }
  }

  test("should not create node from unsupported entity") {
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.createNodeFrom(new VersionedLongIdEntity(0, 0) {}) }
    }
  }

  test("should update actor node") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    val anotherPersonNode = createNodeFrom(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."))
    val anotherCharacterNode = createNodeFrom(Character("Zeus Carver"))
    val anotherMovieNode = createNodeFrom(Movie("Die hard: With a vengeance"))
    val modifiedActor = Actor(createPersonFrom(anotherPersonNode), createCharacterFrom(anotherCharacterNode), createMovieFrom(anotherMovieNode), actor.version, actor.id.get)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedActor) }
    updatedNode.getSingleRelationship(FilmCrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode should be(anotherPersonNode)
    updatedNode.getSingleRelationship(Played, OUTGOING).getEndNode should be(anotherCharacterNode)
    updatedNode.getSingleRelationship(AppearedIn, OUTGOING).getEndNode should be(anotherMovieNode)
    getLong(updatedNode, Version) should be (actor.version + 1)
    updatedNode.getId should be(actor.id.get)
  }

  test("should not update actor node if a different node already exists for the modified actor") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    val anotherPerson = insertEntity(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."))
    val anotherCharacter = insertEntity(Character("Zeus Carver"))
    val anotherMovie = insertEntity(Movie("Die hard: With a vengeance"))
    insertEntity(Actor(anotherPerson, anotherCharacter, anotherMovie))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(Actor(anotherPerson, anotherCharacter, anotherMovie, actor.version, actor.id.get)) }
    }
  }

  test("should not update actor node if the version of the actor does not match the version of the node") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(Actor(insertEntity(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S.")), insertEntity(Character("Zeus Carver")), insertEntity(Movie("Die hard: With a vengeance")), actor.version + 1, actor.id.get)) }
    }
  }

  test("should update character node") {
    val character = insertEntity(VincentVega)
    val modifiedCharacter = Character("Jules Winnfield", character.version, character.id.get)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedCharacter) }
    getString(updatedNode, CharacterName) should be("Jules Winnfield")
    getLong(updatedNode, Version) should be(modifiedCharacter.version + 1)
    updatedNode.getId should be(character.id.get)
  }

  test("should not update character node if a different node already exists for the modified character") {
    val character = insertEntity(VincentVega)
    insertEntity(Character("Jules Winnfield"))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(Character("Jules Winnfield", character.version, character.id.get)) }
    }
  }

  test("should not update character node if the version of the character does not match the version of the node") {
    val character = insertEntity(VincentVega)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(Character("Jules Winnfield", character.version + 1, character.id.get)) }
    }
  }

  test("should update digital container node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    val anotherMovieNode = createNodeFrom(Movie("Die hard: With a vengeance"))
    val anotherSoundtrackNode = createNodeFrom(ItalianSoundtrack)
    val anotherSubtitleNode = createNodeFrom(ItalianSubtitle)
    val modifiedDigitalContainer = DigitalContainer(createMovieFrom(anotherMovieNode), Set(createSoundtrackFrom(anotherSoundtrackNode)), Set(createSubtitleFrom(anotherSubtitleNode)), digitalContainer.version, digitalContainer.id.get)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedDigitalContainer) }
    updatedNode.getSingleRelationship(WithContent, OUTGOING).getEndNode should be(anotherMovieNode)
    updatedNode.getRelationships(WithSoundtrack, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(anotherSoundtrackNode))
    updatedNode.getRelationships(WithSubtitle, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(anotherSubtitleNode))
    getLong(updatedNode, Version) should be(modifiedDigitalContainer.version + 1)
    updatedNode.getId should be(digitalContainer.id.get)
  }

  test("should not update digital container node if a different node already exists for the modified digital container") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    val anotherMovieNode = insertEntity(Movie("Die hard: With a vengeance"))
    val anotherSoundtrackNode = insertEntity(ItalianSoundtrack)
    val anotherSubtitleNode = insertEntity(ItalianSubtitle)
    insertEntity(DigitalContainer(anotherMovieNode, Set(anotherSoundtrackNode), Set(anotherSubtitleNode)))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) {
        subject.updateNodeOf(DigitalContainer(anotherMovieNode, Set(anotherSoundtrackNode), Set(anotherSubtitleNode), digitalContainer.version, digitalContainer.id.get))
      }
    }
  }

  test("should not update digital container node if the version of the digital container does not match the version of the node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(DigitalContainer(insertEntity(Movie("Die hard: With a vengeance")), Set(insertEntity(ItalianSoundtrack)), Set(insertEntity(ItalianSubtitle)), digitalContainer.version + 1, digitalContainer.id.get)) }
    }
  }

  test("should update movie node") {
    val movie = insertEntity(PulpFiction)
    val modifiedMovie = Movie("Die hard: With a vengeance", Set(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale), LocalizedText("Die hard: Duri a morire")(ItalianLocale)), Duration.standardMinutes(131), new LocalDate(1995, 5, 19), movie.version, movie.id.get)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedMovie) }
    getLocalizedText(updatedNode, MovieOriginalTitle) should be(LocalizedText("Die hard: With a vengeance"))
    getLocalizedTextSet(updatedNode, MovieLocalizedTitles) should be(Set(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale), LocalizedText("Die hard: Duri a morire")(ItalianLocale)))
    getDuration(updatedNode, MovieRuntime) should be(Duration.standardMinutes(131))
    getLocalDate(updatedNode, MovieReleaseDate) should be(new LocalDate(1995, 5, 19))
    getLong(updatedNode, Version) should be(modifiedMovie.version + 1)
    updatedNode.getId should be(movie.id.get)
  }

  test("should not update movie node if a different node already exists for the modified movie") {
    val movie = insertEntity(PulpFiction)
    insertEntity(Movie("Die hard: With a vengeance", Set(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale), LocalizedText("Die hard: Duri a morire")(ItalianLocale)), Duration.standardMinutes(131), new LocalDate(1995, 5, 19)))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(Movie("Die hard: With a vengeance", Set(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale), LocalizedText("Die hard: Duri a morire")(ItalianLocale)), Duration.standardMinutes(131), new LocalDate(1995, 5, 19), movie.version, movie.id.get)) }
    }
  }

  test("should not update movie node if the version of the movie does not match the version of the node") {
    val movie = insertEntity(PulpFiction)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(Movie("Die hard: With a vengeance", Set(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale), LocalizedText("Die hard: Duri a morire")(ItalianLocale)), Duration.standardMinutes(131), new LocalDate(1995, 5, 19), movie.version + 1, movie.id.get)) }
    }
  }

  test("should update person node") {
    val person = insertEntity(JohnTravolta)
    val modifiedPerson = Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.", person.version, person.id.get)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedPerson) }
    getString(updatedNode, PersonName) should be("Uma Karuna Thurman")
    getString(updatedNode, PersonGender) should be(Female.toString)
    getLocalDate(updatedNode, PersonDateOfBirth) should be(new LocalDate(1970, 4, 29))
    getString(updatedNode, PersonPlaceOfBirth) should be("Boston, Massachusetts, U.S.")
    getLong(updatedNode, Version) should be(modifiedPerson.version + 1)
    updatedNode.getId should be(person.id.get)
  }

  test("should not update person node if a different node already exists for the modified person") {
    val person = insertEntity(JohnTravolta)
    insertEntity(Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S."))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.", person.version, person.id.get)) }
    }
  }

  test("should not update person node if the version of the person does not match the version of the node") {
    val person = insertEntity(JohnTravolta)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.", person.version + 1, person.id.get)) }
    }
  }

  test("should not update node of unsupported entity") {
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(new VersionedLongIdEntity(0, 0) {}) }
    }
  }
}
