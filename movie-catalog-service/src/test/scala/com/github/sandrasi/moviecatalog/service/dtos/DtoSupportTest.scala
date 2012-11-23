package com.github.sandrasi.moviecatalog.service.dtos

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.service.dtos.DtoSupport._

@RunWith(classOf[JUnitRunner])
class DtoSupportTest extends FunSuite with ShouldMatchers {

  private final val HungarianLocale = new Locale("hu", "HU")
  private final val ItalianLocale = Locale.ITALY

  test("should convert actor to actor dto") {
    val person = domain.Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    val character = domain.Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    val movie = domain.Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Set(domain.Genre("crime", "Crime"), domain.Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val actor = domain.Actor(person, character, movie)
    implicit val locale = HungarianLocale
    toActorDto(actor) should be(Actor(None, Person(None, "John Joseph Travolta"), Character(None, "Vincent Vega"), Movie(None, "Pulp fiction", Some("Ponyvaregény"))))
  }

  test("should convert actress to actress dto") {
    val person = domain.Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.")
    val character = domain.Character("Mia Wallace", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    val movie = domain.Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Set(domain.Genre("crime", "Crime"), domain.Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val actress = domain.Actress(person, character, movie)
    implicit val locale = HungarianLocale
    toActressDto(actress) should be(Actress(None, Person(None, "Uma Karuna Thurman"), Character(None, "Mia Wallace"), Movie(None, "Pulp fiction", Some("Ponyvaregény"))))
  }

  test("should convert charater to character dto") {
    val character = domain.Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    toCharacterDto(character) should be(Character(None, character.name, character.creator, character.creationDate.map(_.toString)))
  }

  test("should convert digital container to digital container dto") {
    val movie = domain.Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Set(domain.Genre("crime", "Crime"), domain.Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val englishSoundtrack = domain.Soundtrack("en", "dts", "English", "DTS")
    val hungarianSoundtrack = domain.Soundtrack("hu", "dts", "Hungarian", "DTS")
    val englishSubtitle = domain.Subtitle("en", "English")
    val hungarianSubtitle = domain.Subtitle("hu", "Hungarian")
    val digitalContainer = domain.DigitalContainer(movie, Set(englishSoundtrack, hungarianSoundtrack), Set(englishSubtitle, hungarianSubtitle))
    toDigitalContainerDto(digitalContainer) should be(DigitalContainer(None, Movie(None, "Pulp fiction"), Set(Soundtrack(None, "en", "dts"), Soundtrack(None, "hu", "dts")), Set(Subtitle(None, "en"), Subtitle(None, "hu"))))
  }

  test("should convert genre to genre dto") {
    val genre = domain.Genre("crime", "Crime")
    toGenreDto(genre) should be(Genre(None, genre.code, Some(genre.name.get.text)))
  }

  test("should convert genre to genre dto if the name is not defined") {
    val genre = domain.Genre("crime")
    toGenreDto(genre) should be(Genre(None, genre.code))
  }
  
  test("should convert movie to movie dto") {
    val movie = domain.Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Set(domain.Genre("crime", "Crime"), domain.Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    toMotionPictureDto(movie)(HungarianLocale) should be(Movie(None, movie.originalTitle.text, Some(movie.localizedTitles.filter(_.locale == HungarianLocale).head.text), Some(Set(Genre(None, "crime"), Genre(None, "thriller"))), Some(movie.runtime.getMillis), Some(movie.releaseDate.toString)))
  }
  
  test("should convert movie to movie dto if no localized title matches the current locale") {
    val movie = domain.Movie("Pulp fiction", Set(LocalizedText("Pulp fiction")(ItalianLocale)), Set(domain.Genre("crime", "Crime"), domain.Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    toMotionPictureDto(movie)(HungarianLocale) should be(Movie(None, movie.originalTitle.text, None, Some(Set(domain.Genre("crime"), domain.Genre("thriller"))), Some(movie.runtime.getMillis), Some(movie.releaseDate.toString)))
  }
  
  test("should convert person to person dto") {
    val person = domain.Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    toPersonDto(person) should be(Person(None, person.name, Some(person.gender.toString), Some(person.dateOfBirth.toString), Some(person.placeOfBirth)))
  }
  
  test("should convert soundtrack to soundtrack dto") {
    val soundtrack = domain.Soundtrack("en", "dts", "English", "DTS")
    toSoundtrackDto(soundtrack) should be(Soundtrack(None, soundtrack.languageCode, soundtrack.formatCode, Some(soundtrack.languageName.get.text), Some(soundtrack.formatName.get.text)))
  }

  test("should convert soundtrack to soundtrack dto if the language and format names are not defined") {
    val soundtrack = domain.Soundtrack("en", "dts")
    toSoundtrackDto(soundtrack) should be(Soundtrack(None, soundtrack.languageCode, soundtrack.formatCode))
  }

  test("should convert subtitle to subtitle dto") {
    val subtitle = domain.Subtitle("en", "English")
    toSubtitleDto(subtitle) should be(Subtitle(None, subtitle.languageCode, Some(subtitle.languageName.get.text)))
  }

  test("should convert subtitle to subtitle dto if the language and format names are not defined") {
    val subtitle = domain.Subtitle("en")
    toSubtitleDto(subtitle) should be(Subtitle(None, subtitle.languageCode))
  }
}
