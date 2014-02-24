package com.github.sandrasi.moviecatalog.repository.neo4j
package utility

import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport
import org.joda.time.LocalDate
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

class IndexManagerTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with MovieCatalogNeo4jSupport {

  private var subject: IndexManager = _

  override protected def beforeEach() {
    subject = IndexManager(db)
  }

  test("should return the same index manager instance for the same database") {
    subject should be theSameInstanceAs(IndexManager(db))
  }

  test("should return different index manager instances for different databases") {
    subject should not be theSameInstanceAs(IndexManager(createTempDb()))
  }

  test("should not instantiate the index manager if the database is null") {
    intercept[IllegalArgumentException] {
      IndexManager(null)
    }
  }

  test("should find the same actor") {
    createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    assert(subject.exists(Actor(JohnTravolta, VincentVega, PulpFiction)))
  }

  test("should not find the actor if the person is different") {
    createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    assert(!subject.exists(Actor(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."), VincentVega, PulpFiction)))
  }

  test("should not find the actor if the character is different") {
    createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    assert(!subject.exists(Actor(JohnTravolta, Character("Jules Winnfield"), PulpFiction)))
  }

  test("should not find actor if the motion picture is different") {
    createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    assert(!subject.exists(Actor(JohnTravolta, VincentVega, Movie("Die hard: With a vengeance"))))
  }

  test("should find the same character") {
    createNodeFrom(VincentVega)
    assert(subject.exists(VincentVega))
  }

  test("should not find the character if the name is different") {
    createNodeFrom(VincentVega)
    assert(!subject.exists(VincentVega.copy(name = "Jules Winnfield")))
  }

  test("should not find the character if the creator is different") {
    createNodeFrom(VincentVega)
    assert(!subject.exists(VincentVega.copy(creator = Some("Robert Rodriguez"))))
  }

  test("should not find the character if the creation date is different") {
    createNodeFrom(VincentVega)
    assert(!subject.exists(VincentVega.copy(creationDate = Some(new LocalDate(2000, 1, 1)))))
  }

  test("should not find the character if the creator is missing") {
    createNodeFrom(VincentVega)
    assert(!subject.exists(VincentVega.copy(creator = None)))
  }

  test("should not find the character if the creation date is missing") {
    createNodeFrom(VincentVega)
    assert(!subject.exists(VincentVega.copy(creationDate = None)))
  }

  test("should find the same digital container") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    assert(subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))))
  }

  test("should not find the digital container if the motion picture is different") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    assert(!subject.exists(DigitalContainer(Movie("Die hard: With a vengeance"), Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))))
  }

  test("should not find the digital container if the soundtracks are different") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    assert(!subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, ItalianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))))
  }

  test("should not find the digital container if it has less soundtracks") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack), insertEntity(ItalianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    assert(!subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))))
  }

  test("should not find the digital container if the subtitles are different") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    assert(!subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, ItalianSubtitle))))
  }

  test("should not find the digital container if it has less subtitles") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle), insertEntity(ItalianSubtitle))))
    assert(!subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))))
  }

  test("should find the same genre") {
    createNodeFrom(Crime)
    assert(subject.exists(Crime))
  }

  test("should not find the genre if it has different code") {
    createNodeFrom(Crime)
    assert(!subject.exists(Genre("thriller")))
  }

  test("should find the same motion picture") {
    createNodeFrom(PulpFiction)
    assert(subject.exists(PulpFiction))
  }

  test("should not find the motion picture if it has different original title") {
    createNodeFrom(PulpFiction)
    assert(!subject.exists(PulpFiction.copy(originalTitle = LocalizedText("Die hard: With a vengeance")(PulpFiction.originalTitle.locale))))
  }

  test("should not find the motion picture if it has a different original title locale") {
    createNodeFrom(PulpFiction)
    assert(!subject.exists(PulpFiction.copy(originalTitle = LocalizedText(PulpFiction.originalTitle.text)(HungarianLocale))))
  }

  test("should not find the motion picture if it has a different release date") {
    createNodeFrom(PulpFiction)
    assert(!subject.exists(PulpFiction.copy(releaseDate = Some(new LocalDate(1995, 5, 19)))))
  }

  test("should not find the motion picture if the release date is missing") {
    createNodeFrom(PulpFiction)
    assert(!subject.exists(PulpFiction.copy(releaseDate = None)))
  }

  test("should find the same person") {
    createNodeFrom(JohnTravolta)
    assert(subject.exists(JohnTravolta))
  }

  test("should not find the person if it has different name") {
    createNodeFrom(JohnTravolta)
    assert(!subject.exists(Person("Samuel Leroy Jackson", JohnTravolta.gender, JohnTravolta.dateOfBirth, JohnTravolta.placeOfBirth)))
  }

  test("should not find the person if it has different gender") {
    createNodeFrom(JohnTravolta)
    assert(!subject.exists(Person(JohnTravolta.name, Female, JohnTravolta.dateOfBirth, JohnTravolta.placeOfBirth)))
  }

  test("should not find the person if it has different date of birth") {
    createNodeFrom(JohnTravolta)
    assert(!subject.exists(Person(JohnTravolta.name, JohnTravolta.gender, new LocalDate(1948, 12, 21), JohnTravolta.placeOfBirth)))
  }

  test("should not find the person if it has different place of birth") {
    createNodeFrom(JohnTravolta)
    assert(!subject.exists(Person(JohnTravolta.name, JohnTravolta.gender, JohnTravolta.dateOfBirth, "Washington, D.C., U.S.")))
  }

  test("should find the same soundtrack") {
    createNodeFrom(EnglishSoundtrack)
    assert(subject.exists(EnglishSoundtrack))
  }

  test("should not find the soundtrack if it has different language code") {
    createNodeFrom(EnglishSoundtrack)
    assert(!subject.exists(Soundtrack("hu", EnglishSoundtrack.formatCode)))
  }

  test("should not find the soundtrack if it has different format code") {
    createNodeFrom(EnglishSoundtrack)
    assert(!subject.exists(Soundtrack(EnglishSoundtrack.languageCode, "dd5.1")))
  }

  test("should find the same subtitle") {
    createNodeFrom(EnglishSubtitle)
    assert(subject.exists(EnglishSubtitle))
  }

  test("should not find the subtitle if it has different language code") {
    createNodeFrom(EnglishSubtitle)
    assert(!subject.exists(Subtitle("hu")))
  }
}
