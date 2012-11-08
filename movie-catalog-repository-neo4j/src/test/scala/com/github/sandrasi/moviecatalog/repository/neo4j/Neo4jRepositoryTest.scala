package com.github.sandrasi.moviecatalog.repository.neo4j

import java.io.IOException
import java.nio.file.{Files, Path, Paths, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult._
import java.util.UUID
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.neo4j.graphdb.NotFoundException
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Male
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

@RunWith(classOf[JUnitRunner])
class Neo4jRepositoryTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

  private var subject: Neo4jRepository = _

  override protected def beforeEach() {
    subject = new Neo4jRepository(db)
  }

  test("should return configuration meta data") {
    val configurationMetaData = Neo4jRepository.configurationMetaData
    configurationMetaData.configurationParameters.size should be(1)
    configurationMetaData.get("storeDir").get.name should be("storeDir")
    configurationMetaData.get("storeDir").get.description should be("The directory where Neo4j stores the database")
    configurationMetaData.get("storeDir").get.valueType should be(classOf[String])
    assert(configurationMetaData.get("storeDir").get.parameterConverter.isInstanceOf[(Seq[_]) => _])
  }

  test("should instantiate Neo4jRepository from configuration") {
    val configuration = new Neo4jRepository.RepositoryConfiguration
    val storeDir = Files.createTempDirectory(UUID.randomUUID().toString).toString
    configuration.setFromString("storeDir", storeDir)(Neo4jRepository.configurationMetaData.get("storeDir").get.parameterConverter)
    val subject = Neo4jRepository(configuration)
    subject should not be(null)
    subject.shutdown()
    Files.walkFileTree(Paths.get(storeDir),
      new SimpleFileVisitor[Path] {

        override def visitFile(file: Path, attrs: BasicFileAttributes) = { Files.delete(file); CONTINUE }

        override def postVisitDirectory(dir: Path, e: IOException) = if (e == null) { Files.delete(dir); CONTINUE } else throw e
      })
  }

  test("should fetch actor from the database by id") {
    val actorNode = createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    assert(subject.get(actorNode.getId, classOf[Actor]).get.isInstanceOf[Actor])
  }

  test("should fetch actress from the database by id") {
    val actressNode = createNodeFrom(Actress(insertEntity(UmaThurman), insertEntity(MiaWallace), insertEntity(PulpFiction)))
    assert(subject.get(actressNode.getId, classOf[Actress]).get.isInstanceOf[Actress])
  }

  test("should fetch character from the database by id") {
    val characterNode = createNodeFrom(VincentVega)
    assert(subject.get(characterNode.getId, classOf[Character]).get.isInstanceOf[Character])
  }

  test("should fetch digital container from the database by id") {
    val digitalContainerNode = createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    assert(subject.get(digitalContainerNode.getId, classOf[DigitalContainer]).get.isInstanceOf[DigitalContainer])
  }

  test("should fetch movie from the database by id") {
    val movieNode = createNodeFrom(PulpFiction)
    assert(subject.get(movieNode.getId, classOf[Movie]).get.isInstanceOf[Movie])
  }

  test("should fetch person from the database by id") {
    val personNode = createNodeFrom(JohnTravolta)
    assert(subject.get(personNode.getId, classOf[Person]).get.isInstanceOf[Person])
  }
  
  test("should fetch soundtrack from the database by id") {
    val soundtrackNode = createNodeFrom(EnglishSoundtrack)
    assert(subject.get(soundtrackNode.getId, classOf[Soundtrack]).get.isInstanceOf[Soundtrack])
  }

  test("should fetch subtitle from the database by id") {
    val subtitleNode = createNodeFrom(EnglishSubtitle)
    assert(subject.get(subtitleNode.getId, classOf[Subtitle]).get.isInstanceOf[Subtitle])
  }

  test("should return nothing if there is no node in the database with the specified id") {
    subject.get(getNodeCount + 1, classOf[Actor]) should be(None)
  }
  
  test("should return nothing if the node cannot be converted to the given type") {
    val characterNode = createNodeFrom(VincentVega)
    subject.get(characterNode.getId, classOf[Actor]) should be(None)
  }

  test("should insert actor into the database and return a managed instance") {
    val actor = Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction))
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
    val savedCharacter = subject.save(VincentVega)
    savedCharacter.id should not be(None)
    savedCharacter should equal(VincentVega)
    try {
      db.getNodeById(savedCharacter.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert digital container into the database and return a managed instance") {
    val digitalContainer = DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle)))
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
    val savedMovie = subject.save(PulpFiction)
    savedMovie.id should not be(None)
    savedMovie should equal(PulpFiction)
    try {
      db.getNodeById(savedMovie.id.get)
    } catch {
      case e: NotFoundException => fail("getNodeById(Long) should have returned a node")
    }
  }

  test("should insert person into the database and return a managed instance") {
    val savedPerson = subject.save(JohnTravolta)
    savedPerson.id should not be(None)
    savedPerson should equal(JohnTravolta)
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
  
  test("should update actor in the database and return a managed instance") {
    val actorInDb = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    val modifiedActor = Actor(insertEntity(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S.")), insertEntity(Character("Zeus Carver")), insertEntity(Movie("Die hard: With a vengeance")), actorInDb.version, actorInDb.id.get)
    val updatedActor = subject.save(modifiedActor)
    updatedActor.version should be(actorInDb.version + 1)
    updatedActor.id should be(actorInDb.id)
    updatedActor should equal(modifiedActor)
  }

  test("should update character in the database and return a managed instance") {
    val characterInDb = insertEntity(VincentVega)
    val modifiedCharacter = Character("Machete", "Robert Rodriguez", new LocalDate(2007, 4, 6), characterInDb.version, characterInDb.id.get)
    val updatedCharacter = subject.save(modifiedCharacter)
    updatedCharacter.version should be(characterInDb.version + 1)
    updatedCharacter.id should be (characterInDb.id)
    updatedCharacter should equal(modifiedCharacter)
  }

  test("should update digital container in the database and return a managed instance") {
    val digitalContainerInDb = insertEntity(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    val modifiedDigitalContainer = DigitalContainer(insertEntity(Movie("Die hard: With a vengeance")), Set(insertEntity(HungarianSoundtrack)), Set(insertEntity(HungarianSubtitle)), digitalContainerInDb.version, digitalContainerInDb.id.get)
    val updatedDigitalContainer = subject.save(modifiedDigitalContainer)
    updatedDigitalContainer.version should be(digitalContainerInDb.version + 1)
    updatedDigitalContainer.id should be (digitalContainerInDb.id)
    updatedDigitalContainer should equal(modifiedDigitalContainer)
  }

  test("should update movie in the database and return a managed instance") {
    val movieInDb = insertEntity(PulpFiction)
    val modifiedMovie = Movie("Die hard: With a vengeance", Set(LocalizedText("Die hard: Az élet mindig drága")(HungarianLocale), LocalizedText("Die hard: Duri a morire")(ItalianLocale)), Duration.standardMinutes(131), new LocalDate(1995, 5, 19), movieInDb.version, movieInDb.id.get)
    val updatedMovie = subject.save(modifiedMovie)
    updatedMovie.version should be(movieInDb.version + 1)
    updatedMovie.id should be (movieInDb.id)
    updatedMovie should equal(modifiedMovie)
  }

  test("should update person in the database and return a managed instance") {
    val personInDb = insertEntity(JohnTravolta)
    val modifiedPerson = Person(UmaThurman.name, UmaThurman.gender, UmaThurman.dateOfBirth, UmaThurman.placeOfBirth, personInDb.version, personInDb.id.get)
    val updatedPerson = subject.save(modifiedPerson)
    updatedPerson.version should be(personInDb.version + 1)
    updatedPerson.id should be (personInDb.id)
    updatedPerson should equal(modifiedPerson)
  }

  test("should update soundtrack in the database and return a managed instance with language and format name matching the current locale") {
    val soundtrackInDb = insertEntity(EnglishSoundtrack)
    val modifiedSoundtrack = Soundtrack("hu", "dd5.1", LocalizedText("Magyar")(HungarianLocale), LocalizedText("Dolby Digital 5.1")(HungarianLocale), soundtrackInDb.version, soundtrackInDb.id.get)
    val updatedSoundtrack = subject.save(modifiedSoundtrack)(HungarianLocale)
    updatedSoundtrack.version should be(soundtrackInDb.version + 1)
    updatedSoundtrack.id should be (soundtrackInDb.id)
    updatedSoundtrack should equal(modifiedSoundtrack)
    updatedSoundtrack.languageName.get should be(LocalizedText("Magyar")(HungarianLocale))
    updatedSoundtrack.formatName.get should be(LocalizedText("Dolby Digital 5.1")(HungarianLocale))
  }

  test("should update subtitle in the database and return a managed instance with language name matching the current locale") {
    val subtitleInDb = insertEntity(EnglishSubtitle)
    val modifiedSubtitle = Subtitle("hu", LocalizedText("Magyar")(HungarianLocale), subtitleInDb.version, subtitleInDb.id.get)
    val updatedSubtitle = subject.save(modifiedSubtitle)(HungarianLocale)
    updatedSubtitle.version should be(subtitleInDb.version + 1)
    updatedSubtitle.id should be (subtitleInDb.id)
    updatedSubtitle should equal(modifiedSubtitle)
    updatedSubtitle.languageName.get should be(LocalizedText("Magyar")(HungarianLocale))
  }

  test("should delete actor from the database") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    subject.delete(actor)
    intercept[NotFoundException] {
      db.getNodeById(actor.id.get)
    }
  }

  test("should delete character from the database") {
    val character = insertEntity(VincentVega)
    subject.delete(character)
    intercept[NotFoundException] {
      db.getNodeById(character.id.get)
    }
  }
  
  test("should delete digital container from the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack)), Set(insertEntity(EnglishSubtitle))))
    subject.delete(digitalContainer)
    intercept[NotFoundException] {
      db.getNodeById(digitalContainer.id.get)
    }
  }

  test("should delete movie from the database") {
    val movie = insertEntity(PulpFiction)
    subject.delete(movie)
    intercept[NotFoundException] {
      db.getNodeById(movie.id.get)
    }
  }
  
  test("should delete person from the database") {
    val person = insertEntity(JohnTravolta)
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

  test("should return all abstract cast from the database") {
    val movie = insertEntity(PulpFiction)
    val actress = insertEntity(Actress(insertEntity(UmaThurman), insertEntity(MiaWallace), movie))
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), movie))
    val cast = subject.query(classOf[Cast]).toList
    cast should (contain(actor.asInstanceOf[Cast]) and contain(actress.asInstanceOf[Cast]) and have size(2))
  }
  
  test("should return all actors from the database") {
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    val actors = subject.query(classOf[Actor]).toList
    actors should (contain(actor) and have size(1))
  }

  test("should return all characters from the database") {
    val character = insertEntity(VincentVega)
    val characters = subject.query(classOf[Character]).toList
    characters should (contain(character) and have size(1))
  }

  test("should return all digital containers from the database") {
    val digitalContainer = insertEntity(DigitalContainer(insertEntity(PulpFiction)))
    val digitalContainers = subject.query(classOf[DigitalContainer]).toList
    digitalContainers should (contain(digitalContainer) and have size(1))
  }

  test("should return all movies from the database") {
    val movie = insertEntity(PulpFiction)
    val movies = subject.query(classOf[Movie]).toList
    movies should (contain(movie) and have size(1))
  }

  test("should return all persons from the database") {
    val person = insertEntity(JohnTravolta)
    val persons = subject.query(classOf[Person]).toList
    persons should (contain(person) and have size(1))
  }

  test("should return all soundtracks from the database") {
    val soundtrack = insertEntity(EnglishSoundtrack)
    val soundtracks = subject.query(classOf[Soundtrack]).toList
    soundtracks should (contain(soundtrack) and have size(1))
  }

  test("should return all subtitles from the database") {
    val subtitle = insertEntity(EnglishSubtitle)
    val subtitles = subject.query(classOf[Subtitle]).toList
    subtitles should (contain(subtitle) and have size(1))
  }

  test("should return entities matching the criterion from the database") {
    val movie = insertEntity(PulpFiction)
    insertEntity(Actress(insertEntity(UmaThurman), insertEntity(MiaWallace), movie))
    val actor = insertEntity(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), movie))
    val maleCast = subject.query(classOf[Cast], (c: Cast) => c.isInstanceOf[Actor]).toList
    maleCast should (contain(actor.asInstanceOf[Cast]) and have size(1))
  }

  test("should shut down the repository") {
    subject.shutdown()
    intercept[Exception] {
      subject.save(VincentVega)
    }
  }
}
