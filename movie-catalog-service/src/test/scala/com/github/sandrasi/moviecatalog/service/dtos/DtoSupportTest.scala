package com.github.sandrasi.moviecatalog.service.dtos

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import com.github.sandrasi.moviecatalog.domain.entities.castandcrew.{Actor, Actress}
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain.entities.container.{DigitalContainer, Soundtrack, Subtitle}
import com.github.sandrasi.moviecatalog.domain.entities.core.{Character, Movie, Person}
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.service.dtos.DtoSupport._

class DtoSupportTest extends FunSuite with ShouldMatchers {

  private final val HungarianLocale = new Locale("hu", "HU")
  private final val ItalianLocale = Locale.ITALY

  test("should convert charater to character dto") {
    val character = Character("Vincent Vega")
    toCharacterDto(character) should be(CharacterDto(character.id, character.name))
  }
  
  test("should convert movie to movie dto") {
    val movie = Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    toMotionPictureDto(movie)(HungarianLocale) should be(MovieDto(movie.id, movie.originalTitle.text, Some(movie.localizedTitles.filter(_.locale == HungarianLocale).head.text), movie.runtime.getMillis, movie.releaseDate.toString))
  }
  
  test("should convert movie to movie dto if no localized title matches the current locale") {
    val movie = Movie("Pulp fiction", Set(LocalizedText("Pulp fiction")(ItalianLocale)), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    toMotionPictureDto(movie)(HungarianLocale) should be(MovieDto(movie.id, movie.originalTitle.text, None, movie.runtime.getMillis, movie.releaseDate.toString))
  }
  
  test("should convert person to person dto") {
    val person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    toPersonDto(person) should be(PersonDto(person.id, person.name, person.gender.toString, person.dateOfBirth.toString, person.placeOfBirth))
  }
  
  test("should convert actor to actor dto") {
    val person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    val character = Character("Vincent Vega")
    val movie = Movie("Pulp fiction", runtime = Duration.standardMinutes(154), releaseDate = new LocalDate(1994, 10, 14))
    val actor = Actor(person, character, movie)
    implicit val locale = HungarianLocale
    toActorDto(actor) should be(ActorDto(actor.id, toPersonDto(person), toCharacterDto(character), toMotionPictureDto(movie)))
  }

  test("should convert actress to actress dto") {
    val person = Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.")
    val character = Character("Mia Wallace")
    val movie = Movie("Pulp fiction", runtime = Duration.standardMinutes(154), releaseDate = new LocalDate(1994, 10, 14))
    val actress = Actress(person, character, movie)
    implicit val locale = HungarianLocale
    toActressDto(actress) should be(ActressDto(actress.id, toPersonDto(person), toCharacterDto(character), toMotionPictureDto(movie)))
  }
  
  test("should convert soundtrack to soundtrack dto") {
    val soundtrack = Soundtrack("en", "dts", "English", "DTS")
    toSoundtrackDto(soundtrack) should be(SoundtrackDto(soundtrack.id, soundtrack.languageCode, soundtrack.formatCode, Some(soundtrack.languageName.get.text), Some(soundtrack.formatName.get.text)))
  }

  test("should convert soundtrack to soundtrack dto if the language and format names are not defined") {
    val soundtrack = Soundtrack("en", "dts")
    toSoundtrackDto(soundtrack) should be(SoundtrackDto(soundtrack.id, soundtrack.languageCode, soundtrack.formatCode, None, None))
  }

  test("should convert subtitle to subtitle dto") {
    val subtitle = Subtitle("en", "English")
    toSubtitleDto(subtitle) should be(SubtitleDto(subtitle.id, subtitle.languageCode, Some(subtitle.languageName.get.text)))
  }

  test("should convert subtitle to subtitle dto if the language and format names are not defined") {
    val subtitle = Subtitle("en")
    toSubtitleDto(subtitle) should be(SubtitleDto(subtitle.id, subtitle.languageCode, None))
  }
  
  test("should convert digital container to digital container dto") {
    val movie = Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val englishSoundtrack = Soundtrack("en", "dts", "English", "DTS")
    val hungarianSoundtrack = Soundtrack("hu", "dts", "Hungarian", "DTS")
    val englishSubtitle = Subtitle("en", "English")
    val hungarianSubtitle = Subtitle("hu", "Hungarian")
    val digitalContainer = DigitalContainer(movie, Set(englishSoundtrack, hungarianSoundtrack), Set(englishSubtitle, hungarianSubtitle))
    toDigitalContainerDto(digitalContainer) should be(DigitalContainerDto(digitalContainer.id, movie, digitalContainer.soundtracks.map(toSoundtrackDto(_)), digitalContainer.subtitles.map(toSubtitleDto(_))))
  }
}
