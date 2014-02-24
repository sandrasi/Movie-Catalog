package com.github.sandrasi.moviecatalog.repository.neo4j
package utility

import scala.collection.JavaConverters._
import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import org.neo4j.graphdb.Direction._
import org.neo4j.graphdb.NotFoundException
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CharacterRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.CrewRelationshipType
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.EntityRelationshipType.IsA
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.DigitalContainerRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.relationshiptypes.MotionPictureRelationshipType._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.MovieCatalogDbConstants._
import com.github.sandrasi.moviecatalog.repository.neo4j.utility.PropertyManager._

// TODO (sandrasi): use the genres in the movie entities as soon as the genres are saved with the movie
@RunWith(classOf[JUnitRunner])
class NodeManagerTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with MovieCatalogNeo4jSupport {

  private var subject: NodeManager = _

  override protected def beforeEach() {
    subject = NodeManager(db)
  }

  test("should return the same unique node factory instance for the same database") {
    subject should be theSameInstanceAs(NodeManager(db))
  }

  test("should return different unique node factory instances for different databases") {
    subject should not be theSameInstanceAs(NodeManager(createTempDb()))
  }

  test("should not instantiate the unique node factory if the database is null") {
    intercept[IllegalArgumentException] {
      NodeManager(null)
    }
  }

  test("should create node from actor") {
    val personNode = createNodeFrom(JohnTravolta)
    val characterNode = createNodeFrom(VincentVega)
    val movieNode = createNodeFrom(PulpFiction)
    implicit val tx = db.beginTx()
    val actorNode = transaction(tx) { subject.createNodeFrom(Actor(createPersonFrom(personNode), createCharacterFrom(characterNode), createMovieFrom(movieNode))) }
    actorNode.getSingleRelationship(CrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode should be(personNode)
    actorNode.getSingleRelationship(Played, OUTGOING).getEndNode should be(characterNode)
    actorNode.getSingleRelationship(AppearedIn, OUTGOING).getEndNode should be(movieNode)
    getLong(actorNode, Version) should be(0)
    actorNode.getRelationships(IsA, OUTGOING).iterator().asScala.map(_.getEndNode).toTraversable should(contain(dbMgr.getSubreferenceNode(classOf[Cast])) and contain(dbMgr.getSubreferenceNode(classOf[Actor])))
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
    val anotherActor = actor.copy(person = insertEntity(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S.")))
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
    val anotherActor = actor.copy(character = insertEntity(Character("Jules Winnfield")))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val actorNode = subject.createNodeFrom(actor)
      val anotherActorNode = subject.createNodeFrom(anotherActor)
      actorNode.getId should not equal(anotherActorNode.getId)
    }
  }

  test("should create node from actor if a the same person played the same character in a different motion picture") {
    val person = insertEntity(JohnTravolta)
    val character = insertEntity(VincentVega)
    val actor = Actor(person, character, insertEntity(PulpFiction))
    val anotherActor = actor.copy(motionPicture = insertEntity(Movie("Die hard: With a vengeance")))
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
    getString(characterNode, CharacterName).get should be(VincentVega.name)
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
    implicit val tx = db.beginTx()
    transaction(tx) {
      val characterNode = subject.createNodeFrom(VincentVega)
      val anotherCharacterNode = subject.createNodeFrom(VincentVega.copy(name = "Jules Winnfield"))
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
    val anotherDigitalContainer = digitalContainer.copy(motionPicture = insertEntity(Movie("Die hard: With a vengeance")))
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
    val anotherDigitalContainer = digitalContainer.copy(soundtracks = Set(soundtrack, insertEntity(ItalianSoundtrack)))
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
    val anotherDigitalContainer = digitalContainer.copy(soundtracks = Set(soundtrack))
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
    val anotherDigitalContainer = digitalContainer.copy(subtitles = Set(subtitle, insertEntity(ItalianSubtitle)))
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
    val anotherDigitalContainer = digitalContainer.copy(subtitles = Set(subtitle))
    implicit val tx = db.beginTx()
    transaction(tx) {
      val digitalContainerNode = subject.createNodeFrom(digitalContainer)
      val anotherDigitalContainerNode = subject.createNodeFrom(anotherDigitalContainer)
      digitalContainerNode.getId should not equal(anotherDigitalContainerNode.getId)
    }
  }

  test("should create node from genre") {
    implicit val tx = db.beginTx()
    val genreNode = transaction(tx) { subject.createNodeFrom(Crime) }
    getString(genreNode, GenreCode).get should be(Crime.code)
    getLocalizedText(genreNode, GenreName, AmericanLocale) should be(Crime.name)
    getLong(genreNode, Version) should be(Crime.version)
    genreNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[Genre]))
  }

  test("should create node from genre without name") {
    implicit val tx = db.beginTx()
    val genreNode = transaction(tx) { subject.createNodeFrom(Genre("crime")) }
    intercept[NotFoundException] {
      genreNode.getProperty(GenreName)
    }
  }

  test("should not create node from genre if the name locale does not match the current locale") {
    implicit val tx = db.beginTx()
    implicit val locale = AmericanLocale
    intercept[IllegalStateException] {
      transaction(tx) { subject.createNodeFrom(Genre("crime", LocalizedText("Krimi")(HungarianLocale))) }
    }
  }

  test("should not create node from genre if a node already exists for that genre") {
    implicit val tx = db.beginTx()
    transaction(tx) { subject.createNodeFrom(Crime) }

    intercept[IllegalArgumentException] {
      implicit val tx = db.beginTx()
      transaction(tx) { subject.createNodeFrom(Crime) }
    }
  }

  test("should create node from genre if the code is different") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      val genreNode = subject.createNodeFrom(Crime)
      val anotherGenreNode = subject.createNodeFrom(Crime.copy(code = "thriller"))
      genreNode.getId should not equal(anotherGenreNode.getId)
    }
  }

  test("should create node from movie") {
    val crimeGenreNode = createNodeFrom(Crime)
    val thrillerGenreNode = createNodeFrom(Thriller)
    implicit val tx = db.beginTx()
    val movieNode = transaction(tx) { subject.createNodeFrom(PulpFiction.copy(genres = Set(createGenreFrom(crimeGenreNode), createGenreFrom(thrillerGenreNode)))) }
    getLocalizedText(movieNode, MovieOriginalTitle).get should be(PulpFiction.originalTitle)
    getLocalizedText(movieNode, MovieLocalizedTitle, HungarianLocale) should be(PulpFiction.localizedTitle)
    movieNode.getRelationships(HasGenre, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(crimeGenreNode, thrillerGenreNode))
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
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(PulpFiction.copy(originalTitle = "Die hard: With a vengeance"))
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the original title's locale's language is different") {
    implicit val locale = new Locale("hu", "US")
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(PulpFiction.copy(originalTitle = new LocalizedText(PulpFiction.originalTitle.text)))
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the original title's locale's country is different") {
    implicit val locale = new Locale("en", "GB")
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(PulpFiction.copy(originalTitle = new LocalizedText(PulpFiction.originalTitle.text)))
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the original title's locale's variant is different") {
    implicit val locale = new Locale("en", "US", "California")
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(PulpFiction.copy(originalTitle = new LocalizedText(PulpFiction.originalTitle.text)))
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from movie if the release date is different") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      val movieNode = subject.createNodeFrom(PulpFiction)
      val anotherMovieNode = subject.createNodeFrom(PulpFiction.copy(releaseDate = Some(new LocalDate(1995, 5, 19))))
      movieNode.getId should not equal(anotherMovieNode.getId)
    }
  }

  test("should create node from person") {
    implicit val tx = db.beginTx()
    val personNode = transaction(tx) { subject.createNodeFrom(JohnTravolta) }
    getString(personNode, PersonName).get should be(JohnTravolta.name)
    getString(personNode, PersonGender).get should be(JohnTravolta.gender.toString)
    getLocalDate(personNode, PersonDateOfBirth).get should be(JohnTravolta.dateOfBirth)
    getString(personNode, PersonPlaceOfBirth).get should be(JohnTravolta.placeOfBirth)
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
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(JohnTravolta.copy(name = "Samuel Leroy Jackson"))
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from person if the gender is different") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(JohnTravolta.copy(gender = Female))
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from person if the date of birth is different") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(JohnTravolta.copy(dateOfBirth = new LocalDate(1948, 12, 21)))
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from person if the place of birth is different") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      val personNode = subject.createNodeFrom(JohnTravolta)
      val anotherPersonNode = subject.createNodeFrom(JohnTravolta.copy(placeOfBirth = "Washington, D.C., U.S."))
      personNode.getId should not equal(anotherPersonNode.getId)
    }
  }

  test("should create node from soundtrack") {
    implicit val tx = db.beginTx()
    val soundtrackNode = transaction(tx) { subject.createNodeFrom(EnglishSoundtrack) }
    getString(soundtrackNode, SoundtrackLanguageCode).get should be(EnglishSoundtrack.languageCode)
    getString(soundtrackNode, SoundtrackFormatCode).get should be(EnglishSoundtrack.formatCode)
    getLocalizedText(soundtrackNode, SoundtrackLanguageName, AmericanLocale) should be(EnglishSoundtrack.languageName)
    getLocalizedText(soundtrackNode, SoundtrackFormatName, AmericanLocale) should be(EnglishSoundtrack.formatName)
    getLong(soundtrackNode, Version) should be(EnglishSoundtrack.version)
    soundtrackNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[Soundtrack]))
  }

  test("should create node from soundtrack without language name") {
    implicit val tx = db.beginTx()
    val soundtrackNode = transaction(tx) { subject.createNodeFrom(Soundtrack("en", "dts", formatName = "DTS")) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackLanguageName)
    }
  }

  test("should create node from soundtrack without format name") {
    implicit val tx = db.beginTx()
    val soundtrackNode = transaction(tx) { subject.createNodeFrom(Soundtrack("en", "dts", "English")) }
    intercept[NotFoundException] {
      soundtrackNode.getProperty(SoundtrackFormatName)
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
    implicit val tx = db.beginTx()
    transaction(tx) {
      val soundtrackNode = subject.createNodeFrom(EnglishSoundtrack)
      val anotherSoundtrackNode = subject.createNodeFrom(EnglishSoundtrack.copy(languageCode = "hu"))
      soundtrackNode.getId should not equal(anotherSoundtrackNode.getId)
    }
  }

  test("should create node from soundtrack if the format code is different") {
    implicit val tx = db.beginTx()
    transaction(tx) {
      val soundtrackNode = subject.createNodeFrom(EnglishSoundtrack)
      val anotherSoundtrackNode = subject.createNodeFrom(EnglishSoundtrack.copy(formatCode = "dd5.1"))
      soundtrackNode.getId should not equal(anotherSoundtrackNode.getId)
    }
  }

  test("should create node from subtitle") {
    implicit val tx = db.beginTx()
    val subtitleNode = transaction(tx) { subject.createNodeFrom(EnglishSubtitle) }
    getString(subtitleNode, SubtitleLanguageCode).get should be(EnglishSubtitle.languageCode)
    getLocalizedText(subtitleNode, SubtitleLanguageName, AmericanLocale) should be(EnglishSubtitle.languageName)
    getLong(subtitleNode, Version) should be(EnglishSubtitle.version)
    subtitleNode.getSingleRelationship(IsA, OUTGOING).getEndNode should be(dbMgr.getSubreferenceNode(classOf[Subtitle]))
  }

  test("should create node from subtitle without language name") {
    implicit val tx = db.beginTx()
    val subtitleNode = transaction(tx) { subject.createNodeFrom(Subtitle("en")) }
    intercept[NotFoundException] {
      subtitleNode.getProperty(SubtitleLanguageName)
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
    implicit val tx = db.beginTx()
    transaction(tx) {
      val subtitleNode = subject.createNodeFrom(EnglishSubtitle)
      val anotherSubtitleNode = subject.createNodeFrom(EnglishSubtitle.copy(languageCode = "hu"))
      subtitleNode.getId should not equal(anotherSubtitleNode.getId)
    }
  }

  test("should update actor node") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    val anotherPersonNode = createNodeFrom(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."))
    val anotherCharacterNode = createNodeFrom(Character("Zeus Carver"))
    val anotherMovieNode = createNodeFrom(Movie("Die hard: With a vengeance"))
    val modifiedActor = actor.copy(person = createPersonFrom(anotherPersonNode), character = createCharacterFrom(anotherCharacterNode), motionPicture = createMovieFrom(anotherMovieNode))
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedActor) }
    updatedNode.getSingleRelationship(CrewRelationshipType.forClass(classOf[Actor]), OUTGOING).getEndNode should be(anotherPersonNode)
    updatedNode.getSingleRelationship(Played, OUTGOING).getEndNode should be(anotherCharacterNode)
    updatedNode.getSingleRelationship(AppearedIn, OUTGOING).getEndNode should be(anotherMovieNode)
    getLong(updatedNode, Version) should be (actor.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(actor.id.get.toString)
  }

  test("should not update actor node if a different node already exists for the modified actor") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    val anotherPerson = insertEntity(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."))
    val anotherCharacter = insertEntity(Character("Zeus Carver"))
    val anotherMovie = insertEntity(Movie("Die hard: With a vengeance"))
    insertEntity(Actor(anotherPerson, anotherCharacter, anotherMovie))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(actor.copy(person = anotherPerson, character = anotherCharacter, motionPicture = anotherMovie)) }
    }
  }

  test("should not update actor node if the version of the actor does not match the version of the node") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(actor.copy(person = insertEntity(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S.")), character = insertEntity(Character("Zeus Carver")), motionPicture = insertEntity(Movie("Die hard: With a vengeance")), version = actor.version + 1)) }
    }
  }

  test("should update character node") {
    val character = insertEntity(VincentVega)
    val modifiedCharacter = character.copy(name = "Machete", creator = Some("Robert Rodriguez"), creationDate = Some(new LocalDate(2007, 4, 6)))
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedCharacter) }
    getString(updatedNode, CharacterName).get should be("Machete")
    getString(updatedNode, CharacterCreator).get should be("Robert Rodriguez")
    getLocalDate(updatedNode, CharacterCreationDate).get should be(new LocalDate(2007, 4, 6))
    getLong(updatedNode, Version) should be(modifiedCharacter.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(character.id.get.toString)
  }

  test("should not update character node if a different node already exists for the modified character") {
    val character = insertEntity(VincentVega)
    insertEntity(Character("Jules Winnfield", "Quentin Tarantino", new LocalDate(1994, 10, 14)))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(character.copy(name = "Jules Winnfield")) }
    }
  }

  test("should not update character node if the version of the character does not match the version of the node") {
    val character = insertEntity(VincentVega)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(character.copy(name = "Jules Winnfield", version = character.version + 1)) }
    }
  }

  test("should update digital container node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    val anotherMovieNode = createNodeFrom(Movie("Die hard: With a vengeance"))
    val anotherSoundtrackNode = createNodeFrom(ItalianSoundtrack)
    val anotherSubtitleNode = createNodeFrom(ItalianSubtitle)
    val modifiedDigitalContainer = digitalContainer.copy(motionPicture = createMovieFrom(anotherMovieNode), soundtracks = Set(createSoundtrackFrom(anotherSoundtrackNode)), subtitles = Set(createSubtitleFrom(anotherSubtitleNode)))
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedDigitalContainer) }
    updatedNode.getSingleRelationship(WithContent, OUTGOING).getEndNode should be(anotherMovieNode)
    updatedNode.getRelationships(WithSoundtrack, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(anotherSoundtrackNode))
    updatedNode.getRelationships(WithSubtitle, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(anotherSubtitleNode))
    getLong(updatedNode, Version) should be(modifiedDigitalContainer.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(digitalContainer.id.get.toString)
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
        subject.updateNodeOf(digitalContainer.copy(motionPicture = anotherMovieNode, soundtracks = Set(anotherSoundtrackNode), subtitles = Set(anotherSubtitleNode)))
      }
    }
  }

  test("should not update digital container node if the version of the digital container does not match the version of the node") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(digitalContainer.copy(motionPicture = insertEntity(Movie("Die hard: With a vengeance")), soundtracks = Set(insertEntity(ItalianSoundtrack)), subtitles = Set(insertEntity(ItalianSubtitle)), version = digitalContainer.version + 1)) }
    }
  }

  test("should update genre node") {
    val genre = insertEntity(Crime)
    val modifiedGenre = genre.copy(code = "thriller", name = Some("Thriller"))
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedGenre) }
    getString(updatedNode, GenreCode).get should be("thriller")
    getLocalizedText(updatedNode, GenreName, AmericanLocale).get should be(LocalizedText("Thriller"))
    getLong(updatedNode, Version) should be(modifiedGenre.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(genre.id.get.toString)
  }

  test("should add the genre name to the node properties") {
    val genre = insertEntity(Crime)
    implicit val tx = db.beginTx()
    implicit val locale = HungarianLocale
    val updatedNode = transaction(tx) { subject.updateNodeOf(genre.copy(name = Some(LocalizedText("Krimi")(HungarianLocale)))) }
    getLocalizedText(updatedNode, GenreName, AmericanLocale).get should be(LocalizedText("Crime")(AmericanLocale))
    getLocalizedText(updatedNode, GenreName, HungarianLocale).get should be(LocalizedText("Krimi")(HungarianLocale))
  }

  test("should remove the genre name from the node properties") {
    val genre = insertEntity(Crime)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(genre.copy(name = None)) }
    getLocalizedText(updatedNode, GenreName, AmericanLocale) should be(None)
  }

  test("should not update genre node if a different node already exists for the modified genre") {
    val genre = insertEntity(Crime)
    insertEntity(Thriller)
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(genre.copy(code = "thriller", name = Some("Thriller"))) }
    }
  }

  test("should not update genre node if the version of the genre does not match the version of the node") {
    val genre = insertEntity(Crime)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(genre.copy(code = "thriller", version = genre.version + 1)) }
    }
  }

  test("should not update genre node if the name locale does not match the current locale") {
    val genre = insertEntity(Crime)
    implicit val tx = db.beginTx()
    implicit val locale = AmericanLocale
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(genre.copy(code = "thriller", name = Some(LocalizedText("Krimi")(HungarianLocale)))) }
    }
  }

  test("should update movie node") {
    val movie = insertEntity(PulpFiction)
    val crime = createNodeFrom(Crime)
    val thriller = createNodeFrom(Thriller)
    val modifiedMovie = movie.copy(originalTitle = "Die hard: With a vengeance", localizedTitle = Some(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale)), genres = Set(createGenreFrom(crime), createGenreFrom(thriller)), runtime = Some(Duration.standardMinutes(131)), releaseDate = Some(new LocalDate(1995, 5, 19)))
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedMovie) }
    getLocalizedText(updatedNode, MovieOriginalTitle).get should be(LocalizedText("Die hard: With a vengeance"))
    getLocalizedText(updatedNode, MovieLocalizedTitle, HungarianLocale).get should be(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale))
    updatedNode.getRelationships(HasGenre, OUTGOING).asScala.map(_.getEndNode).toSet should be(Set(crime, thriller))
    getDuration(updatedNode, MovieRuntime).get should be(Duration.standardMinutes(131))
    getLocalDate(updatedNode, MovieReleaseDate).get should be(new LocalDate(1995, 5, 19))
    getLong(updatedNode, Version) should be(modifiedMovie.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(movie.id.get.toString)
  }

  test("should not update movie node if a different node already exists for the modified movie") {
    val movie = insertEntity(PulpFiction)
    insertEntity(Movie("Die hard: With a vengeance", LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale), Set(), Duration.standardMinutes(131), new LocalDate(1995, 5, 19)))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(movie.copy(originalTitle = "Die hard: With a vengeance", localizedTitle = Some(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale)), runtime = Some(Duration.standardMinutes(131)), releaseDate = Some(new LocalDate(1995, 5, 19)))) }
    }
  }

  test("should not update movie node if the version of the movie does not match the version of the node") {
    val movie = insertEntity(PulpFiction)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(movie.copy(originalTitle = "Die hard: With a vengeance", localizedTitle = Some(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale)), runtime = Some(Duration.standardMinutes(131)), releaseDate = Some(new LocalDate(1995, 5, 19)), version = movie.version + 1)) }
    }
  }

  test("should update person node") {
    val person = insertEntity(JohnTravolta)
    val modifiedPerson = person.copy(name = "Uma Karuna Thurman", gender = Female, dateOfBirth = new LocalDate(1970, 4, 29), placeOfBirth = "Boston, Massachusetts, U.S.")
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedPerson) }
    getString(updatedNode, PersonName).get should be("Uma Karuna Thurman")
    getString(updatedNode, PersonGender).get should be(Female.toString)
    getLocalDate(updatedNode, PersonDateOfBirth).get should be(new LocalDate(1970, 4, 29))
    getString(updatedNode, PersonPlaceOfBirth).get should be("Boston, Massachusetts, U.S.")
    getLong(updatedNode, Version) should be(modifiedPerson.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(person.id.get.toString)
  }

  test("should not update person node if a different node already exists for the modified person") {
    val person = insertEntity(JohnTravolta)
    insertEntity(Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S."))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(person.copy(name = "Uma Karuna Thurman", gender = Female, dateOfBirth = new LocalDate(1970, 4, 29), placeOfBirth = "Boston, Massachusetts, U.S.")) }
    }
  }

  test("should not update person node if the version of the person does not match the version of the node") {
    val person = insertEntity(JohnTravolta)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(person.copy(name = "Uma Karuna Thurman", gender = Female, dateOfBirth = new LocalDate(1970, 4, 29), placeOfBirth = "Boston, Massachusetts, U.S.", version = person.version + 1)) }
    }
  }

  test("should update soundtrack node") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = soundtrack.copy(languageCode = "it", formatCode = "dd5.1", languageName = Some("Italian"), formatName = Some("Dolby Digital 5.1"))
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedSoundtrack) }
    getString(updatedNode, SoundtrackLanguageCode).get should be("it")
    getString(updatedNode, SoundtrackFormatCode).get should be("dd5.1")
    getLocalizedText(updatedNode, SoundtrackLanguageName, AmericanLocale).get should be(LocalizedText("Italian"))
    getLocalizedText(updatedNode, SoundtrackFormatName, AmericanLocale).get should be(LocalizedText("Dolby Digital 5.1"))
    getLong(updatedNode, Version) should be(modifiedSoundtrack.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(soundtrack.id.get.toString)
  }

  test("should add the soundtrack language and format names to the node properties") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    implicit val tx = db.beginTx()
    implicit val locale = HungarianLocale
    val updatedNode = transaction(tx) { subject.updateNodeOf(soundtrack.copy(languageName = Some(LocalizedText("Angol")(HungarianLocale)), formatName = Some(LocalizedText("DTS")(HungarianLocale)))) }
    getLocalizedText(updatedNode, SoundtrackLanguageName, AmericanLocale).get should be(LocalizedText("English")(AmericanLocale))
    getLocalizedText(updatedNode, SoundtrackLanguageName, HungarianLocale).get should be(LocalizedText("Angol")(HungarianLocale))
    getLocalizedText(updatedNode, SoundtrackFormatName, AmericanLocale).get should be(LocalizedText("DTS")(AmericanLocale))
    getLocalizedText(updatedNode, SoundtrackFormatName, HungarianLocale).get should be(LocalizedText("DTS")(HungarianLocale))
  }

  test("should remove the soundtrack language and format names from the node properties") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(soundtrack.copy(languageName = None, formatName = None)) }
    getLocalizedText(updatedNode, SoundtrackLanguageName, AmericanLocale) should be(None)
    getLocalizedText(updatedNode, SoundtrackFormatName, AmericanLocale) should be(None)
  }

  test("should not update soundtrack node if a different node already exists for the modified soundtrack") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    insertEntity(Soundtrack("it", "dd5.1", "Italian", "Dolby Digital 5.1"))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(soundtrack.copy(languageCode = "it", formatCode = "dd5.1", languageName = Some("Italian"), formatName = Some("Dolby Digital 5.1"))) }
    }
  }

  test("should not update soundtrack node if the version of the soundtrack does not match the version of the node") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(soundtrack.copy(languageCode = "it", formatCode = "dd5.1", languageName = Some("Italian"), formatName = Some("Dolby Digital 5.1"), version = soundtrack.version + 1)) }
    }
  }

  test("should not update soundtrack node if the language name locale and format name locale do not match the current locale") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    implicit val tx = db.beginTx()
    implicit val locale = AmericanLocale
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(soundtrack.copy(languageCode = "it", formatCode = "dd5.1", languageName = Some(LocalizedText("Olasz")(HungarianLocale)), formatName = Some(LocalizedText("Dolby Digital 5.1")(HungarianLocale)))) }
    }
  }

  test("should update subtitle node") {
    val subtitle = insertEntity(EnglishSubtitle)
    val modifiedSubtitle = subtitle.copy(languageCode = "it", languageName = Some("Italian"))
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(modifiedSubtitle) }
    getString(updatedNode, SubtitleLanguageCode).get should be("it")
    getLocalizedText(updatedNode, SubtitleLanguageName, AmericanLocale).get should be(LocalizedText("Italian"))
    getLong(updatedNode, Version) should be(modifiedSubtitle.version + 1)
    updatedNode.getProperty(Uuid).asInstanceOf[String] should be(subtitle.id.get.toString)
  }

  test("should add the subtitle language name to the node properties") {
    val subtitle = insertEntity(EnglishSubtitle)
    implicit val tx = db.beginTx()
    implicit val locale = HungarianLocale
    val updatedNode = transaction(tx) { subject.updateNodeOf(subtitle.copy(languageName = Some(LocalizedText("Angol")(HungarianLocale)))) }
    getLocalizedText(updatedNode, SubtitleLanguageName, AmericanLocale).get should be(LocalizedText("English")(AmericanLocale))
    getLocalizedText(updatedNode, SubtitleLanguageName, HungarianLocale).get should be(LocalizedText("Angol")(HungarianLocale))
  }

  test("should remove the subtitle language name from the node properties") {
    val subtitle = insertEntity(EnglishSubtitle)
    implicit val tx = db.beginTx()
    val updatedNode = transaction(tx) { subject.updateNodeOf(subtitle.copy(languageName = None)) }
    getLocalizedText(updatedNode, SubtitleLanguageName, AmericanLocale) should be(None)
  }

  test("should not update subtitle node if a different node already exists for the modified subtitle") {
    val subtitle = insertEntity(EnglishSubtitle)
    insertEntity(Subtitle("it", "Italian"))
    implicit val tx = db.beginTx()
    intercept[IllegalArgumentException] {
      transaction(tx) { subject.updateNodeOf(subtitle.copy(languageCode = "it", languageName = Some("Italian"))) }
    }
  }

  test("should not update subtitle node if the version of the subtitle does not match the version of the node") {
    val subtitle = insertEntity(EnglishSubtitle)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(subtitle.copy(languageCode = "it", languageName = Some("Italian"), version = subtitle.version + 1)) }
    }
  }

  test("should not update subtitle node if the language name locale does not match the current locale") {
    val subtitle = insertEntity(EnglishSubtitle)
    implicit val tx = db.beginTx()
    implicit val locale = AmericanLocale
    intercept[IllegalStateException] {
      transaction(tx) { subject.updateNodeOf(subtitle.copy(languageCode = "it", languageName = Some(LocalizedText("Olasz")(HungarianLocale)))) }
    }
  }

  test("should delete actor node") {
    val personNode = createNodeFrom(JohnTravolta)
    val characterNode = createNodeFrom(VincentVega)
    val movieNode = createNodeFrom(PulpFiction)
    val actorNode = createNodeFrom(Actor(createPersonFrom(personNode), createCharacterFrom(characterNode), createMovieFrom(movieNode)))
    assert(dbMgr.getSubreferenceNode(classOf[Cast]).hasRelationship(INCOMING, IsA))
    assert(dbMgr.getSubreferenceNode(classOf[Actor]).hasRelationship(INCOMING, IsA))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.deleteNodeOf(createActorFrom(actorNode)) }
    assert(!personNode.hasRelationship(INCOMING))
    assert(!characterNode.hasRelationship(INCOMING))
    assert(!movieNode.hasRelationship(INCOMING))
    assert(!dbMgr.getSubreferenceNode(classOf[Cast]).hasRelationship(INCOMING, IsA))
    assert(!dbMgr.getSubreferenceNode(classOf[Actor]).hasRelationship(INCOMING, IsA))
    intercept[NotFoundException] {
      db.getNodeById(actorNode.getId)
    }
  }

  test("should delete character node") {
    val characterNode = createNodeFrom(VincentVega)
    assert(dbMgr.getSubreferenceNode(classOf[Character]).hasRelationship(INCOMING, IsA))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.deleteNodeOf(createCharacterFrom(characterNode)) }
    assert(!dbMgr.getSubreferenceNode(classOf[Character]).hasRelationship(INCOMING, IsA))
    intercept[NotFoundException] {
      db.getNodeById(characterNode.getId)
    }
  }

  test("should delete digital container node") {
    val movieNode = createNodeFrom(PulpFiction)
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    val subtitleNode = createNodeFrom(EnglishSubtitle)
    val digitalContainerNode = createNodeFrom(DigitalContainer(createMovieFrom(movieNode), Set(createSoundtrackFrom(soundtrackNode)), Set(createSubtitleFrom(subtitleNode))))
    assert(dbMgr.getSubreferenceNode(classOf[DigitalContainer]).hasRelationship(INCOMING, IsA))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.deleteNodeOf(createDigitalContainerFrom(digitalContainerNode)) }
    assert(!movieNode.hasRelationship(INCOMING))
    assert(!soundtrackNode.hasRelationship(INCOMING))
    assert(!subtitleNode.hasRelationship(INCOMING))
    assert(!dbMgr.getSubreferenceNode(classOf[DigitalContainer]).hasRelationship(INCOMING, IsA))
    intercept[NotFoundException] {
      db.getNodeById(digitalContainerNode.getId)
    }
  }

  test("should delete movie node") {
    val movieNode = createNodeFrom(PulpFiction)
    assert(dbMgr.getSubreferenceNode(classOf[Movie]).hasRelationship(INCOMING, IsA))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.deleteNodeOf(createMovieFrom(movieNode)) }
    assert(!dbMgr.getSubreferenceNode(classOf[Movie]).hasRelationship(INCOMING, IsA))
    intercept[NotFoundException] {
      db.getNodeById(movieNode.getId)
    }
  }

  test("should delete person node") {
    val personNode = createNodeFrom(JohnTravolta)
    assert(dbMgr.getSubreferenceNode(classOf[Person]).hasRelationship(INCOMING, IsA))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.deleteNodeOf(createPersonFrom(personNode)) }
    assert(!dbMgr.getSubreferenceNode(classOf[Person]).hasRelationship(INCOMING, IsA))
    intercept[NotFoundException] {
      db.getNodeById(personNode.getId)
    }
  }

  test("should delete soundtrack node") {
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    assert(dbMgr.getSubreferenceNode(classOf[Soundtrack]).hasRelationship(INCOMING, IsA))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.deleteNodeOf(createSoundtrackFrom(soundtrackNode)) }
    assert(!dbMgr.getSubreferenceNode(classOf[Soundtrack]).hasRelationship(INCOMING, IsA))
    intercept[NotFoundException] {
      db.getNodeById(soundtrackNode.getId)
    }
  }

  test("should delete subtitle node") {
    val subtitleNode = createNodeFrom(EnglishSubtitle)
    assert(dbMgr.getSubreferenceNode(classOf[Subtitle]).hasRelationship(INCOMING, IsA))
    implicit val tx = db.beginTx()
    transaction(tx) { subject.deleteNodeOf(createSubtitleFrom(subtitleNode)) }
    assert(!dbMgr.getSubreferenceNode(classOf[Subtitle]).hasRelationship(INCOMING, IsA))
    intercept[NotFoundException] {
      db.getNodeById(subtitleNode.getId)
    }
  }

  test("should not delete node if the version of the entity does not match the version of the node") {
    val character = insertEntity(VincentVega)
    implicit val tx = db.beginTx()
    intercept[IllegalStateException] {
      transaction(tx) { subject.deleteNodeOf(character.copy(version = character.version + 1)) }
    }
  }

  test("should not delete node if at least one node references it") {
    val characterNode = createNodeFrom(VincentVega)
    val node = createNode()
    implicit val tx = db.beginTx()
    transaction(tx) {
      node.createRelationshipTo(characterNode, new TestRelationshipType("test"))
      intercept[IllegalStateException] {
        subject.deleteNodeOf(createCharacterFrom(characterNode))
      }
    }
  }

  test("should find all cast nodes") {
    val movie = insertEntity(PulpFiction)
    val actorNode = createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), movie))
    val actressNode = createNodeFrom(Actress(insertEntity(UmaThurman), insertEntity(MiaWallace), movie))
    val abstractCastNodes = subject.getNodesOfType(classOf[Cast]).toList
    abstractCastNodes should (contain(actorNode) and contain(actressNode) and have size(2))
  }

  test("should find all actor nodes") {
    val actorNode = createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    val actorNodes = subject.getNodesOfType(classOf[Actor]).toList
    actorNodes should (contain(actorNode) and have size(1))
  }

  test("should find all character nodes") {
    val characterNode = createNodeFrom(VincentVega)
    val characterNodes = subject.getNodesOfType(classOf[Character]).toList
    characterNodes should (contain(characterNode) and have size(1))
  }

  test("should find all digital container nodes") {
    val digitalContainerNode = createNodeFrom(DigitalContainer(insertEntity(PulpFiction)))
    val digitalContainerNodes = subject.getNodesOfType(classOf[DigitalContainer]).toList
    digitalContainerNodes should (contain(digitalContainerNode) and have size(1))
  }

  test("should find all movie nodes") {
    val movieNode = createNodeFrom(PulpFiction)
    val movieNodes = subject.getNodesOfType(classOf[Movie]).toList
    movieNodes should (contain(movieNode) and have size(1))
  }

  test("should find all person nodes") {
    val personNode = createNodeFrom(JohnTravolta)
    val personNodes = subject.getNodesOfType(classOf[Person]).toList
    personNodes should (contain(personNode) and have size(1))
  }

  test("should find all soundtrack nodes") {
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    val soundtrackNodes = subject.getNodesOfType(classOf[Soundtrack]).toList
    soundtrackNodes should (contain(soundtrackNode) and have size(1))
  }

  test("should find all subtitle nodes") {
    val subtitleNode = createNodeFrom(EnglishSubtitle)
    val subtitleNodes = subject.getNodesOfType(classOf[Subtitle]).toList
    subtitleNodes should (contain(subtitleNode) and have size(1))
  }

  test("should not find nodes of unsupported entity type") {
    intercept[IllegalArgumentException] {
      subject.getNodesOfType(classOf[Entity])
    }
  }
}
