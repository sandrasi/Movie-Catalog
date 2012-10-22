package com.github.sandrasi.moviecatalog.repository.neo4j.utility

import org.joda.time.LocalDate
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.Actor
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.repository.neo4j.test.utility.MovieCatalogNeo4jSupport

class IndexManagerTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers with MovieCatalogNeo4jSupport {

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
    subject.exists(Actor(JohnTravolta, VincentVega, PulpFiction)) should be(true)
  }

  test("should not find the actor if the person is different") {
    createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    subject.exists(Actor(Person("Samuel Leroy Jackson", Male, new LocalDate(1948, 12, 21), "Washington, D.C., U.S."), VincentVega, PulpFiction)) should be(false)
  }

  test("should not find the actor if the character is different") {
    createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    subject.exists(Actor(JohnTravolta, Character("Jules Winnfield"), PulpFiction)) should be(false)
  }

  test("should not find actor if the motion picture is different") {
    createNodeFrom(Actor(insertEntity(JohnTravolta), insertEntity(VincentVega), insertEntity(PulpFiction)))
    subject.exists(Actor(JohnTravolta, VincentVega, Movie("Die hard: With a vengeance"))) should be(false)
  }

  test("should find the same character") {
    createNodeFrom(VincentVega)
    subject.exists(VincentVega) should be(true)
  }

  test("should not find the character if the name is different") {
    createNodeFrom(VincentVega)
    subject.exists(Character("Jules Winnfield")) should be(false)
  }

  test("should find the same digital container") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))) should be(true)
  }

  test("should not find the digital container if the motion picture is different") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    subject.exists(DigitalContainer(Movie("Die hard: With a vengeance"), Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))) should be(false)
  }

  test("should not find the digital container if the soundtracks are different") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, ItalianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))) should be(false)
  }

  test("should not find the digital container if it has less soundtracks") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack), insertEntity(ItalianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))) should be(false)
  }

  test("should not find the digital container if the subtitles are different") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle))))
    subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, ItalianSubtitle))) should be(false)
  }

  test("should not find the digital container if it has less subtitles") {
    createNodeFrom(DigitalContainer(insertEntity(PulpFiction), Set(insertEntity(EnglishSoundtrack), insertEntity(HungarianSoundtrack)), Set(insertEntity(EnglishSubtitle), insertEntity(HungarianSubtitle), insertEntity(ItalianSubtitle))))
    subject.exists(DigitalContainer(PulpFiction, Set(EnglishSoundtrack, HungarianSoundtrack), Set(EnglishSubtitle, HungarianSubtitle))) should be(false)
  }

  test("should find the same motion picture") {
    createNodeFrom(PulpFiction)
    subject.exists(PulpFiction) should be(true)
  }

  test("should not find the motion picture if it has different original title") {
    createNodeFrom(PulpFiction)
    subject.exists(Movie(LocalizedText("Die hard: With a vengeance")(PulpFiction.originalTitle.locale), releaseDate = PulpFiction.releaseDate)) should be(false)
  }

  test("should not find the motion picture if it has a different original title locale") {
    createNodeFrom(PulpFiction)
    subject.exists(Movie(LocalizedText(PulpFiction.originalTitle.text)(HungarianLocale), releaseDate = PulpFiction.releaseDate)) should be(false)
  }

  test("should not find the motion picture if it has a different release date") {
    createNodeFrom(PulpFiction)
    subject.exists(Movie(PulpFiction.originalTitle, releaseDate = new LocalDate(1995, 5, 19))) should be(false)
  }

  test("should find the same person") {
    createNodeFrom(JohnTravolta)
    subject.exists(JohnTravolta) should be(true)
  }

  test("should not find the person if it has different name") {
    createNodeFrom(JohnTravolta)
    subject.exists(Person("Samuel Leroy Jackson", JohnTravolta.gender, JohnTravolta.dateOfBirth, JohnTravolta.placeOfBirth)) should be(false)
  }

  test("should not find the person if it has different gender") {
    createNodeFrom(JohnTravolta)
    subject.exists(Person(JohnTravolta.name, Female, JohnTravolta.dateOfBirth, JohnTravolta.placeOfBirth)) should be(false)
  }

  test("should not find the person if it has different date of birth") {
    createNodeFrom(JohnTravolta)
    subject.exists(Person(JohnTravolta.name, JohnTravolta.gender, new LocalDate(1948, 12, 21), JohnTravolta.placeOfBirth)) should be(false)
  }

  test("should not find the person if it has different place of birth") {
    createNodeFrom(JohnTravolta)
    subject.exists(Person(JohnTravolta.name, JohnTravolta.gender, JohnTravolta.dateOfBirth, "Washington, D.C., U.S.")) should be(false)
  }

  test("should find the same soundtrack") {
    createNodeFrom(EnglishSoundtrack)
    subject.exists(EnglishSoundtrack) should be(true)
  }

  test("should not find the soundtrack if it has different language code") {
    createNodeFrom(EnglishSoundtrack)
    subject.exists(Soundtrack("hu", EnglishSoundtrack.formatCode)) should be(false)
  }

  test("should not find the soundtrack if it has different format code") {
    createNodeFrom(EnglishSoundtrack)
    subject.exists(Soundtrack(EnglishSoundtrack.languageCode, "dd5.1")) should be(false)
  }

  test("should find the same subtitle") {
    createNodeFrom(EnglishSubtitle)
    subject.exists(EnglishSubtitle) should be(true)
  }

  test("should not find the subtitle if it has different language code") {
    createNodeFrom(EnglishSubtitle)
    subject.exists(Subtitle("hu")) should be(false)
  }
}
