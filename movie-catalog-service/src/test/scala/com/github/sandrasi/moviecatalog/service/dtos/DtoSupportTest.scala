package com.github.sandrasi.moviecatalog.service.dtos

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import com.github.sandrasi.moviecatalog.common.LocalizedText
import com.github.sandrasi.moviecatalog.domain._
import com.github.sandrasi.moviecatalog.domain.utility.Gender._
import com.github.sandrasi.moviecatalog.service.dtos.DtoSupport._

@RunWith(classOf[JUnitRunner])
class DtoSupportTest extends FunSuite with ShouldMatchers {

  private final val HungarianLocale = new Locale("hu", "HU")
  private final val ItalianLocale = Locale.ITALY

  test("should convert actor to actor dto") {
    val person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    val character = Character("Vincent Vega")
    val movie = Movie("Pulp fiction", runtime = Duration.standardMinutes(154), releaseDate = new LocalDate(1994, 10, 14))
    val actor = Actor(person, character, movie)
    implicit val locale = HungarianLocale
    toActorDto(actor) should be(ActorDto(None, toPersonDto(person), toCharacterDto(character), toMotionPictureDto(movie)))
  }

  test("should convert actress to actress dto") {
    val person = Person("Uma Karuna Thurman", Female, new LocalDate(1970, 4, 29), "Boston, Massachusetts, U.S.")
    val character = Character("Mia Wallace")
    val movie = Movie("Pulp fiction", runtime = Duration.standardMinutes(154), releaseDate = new LocalDate(1994, 10, 14))
    val actress = Actress(person, character, movie)
    implicit val locale = HungarianLocale
    toActressDto(actress) should be(ActressDto(None, toPersonDto(person), toCharacterDto(character), toMotionPictureDto(movie)))
  }

  test("should convert charater to character dto") {
    val character = Character("Vincent Vega", "Quentin Tarantino", new LocalDate(1994, 10, 14))
    toCharacterDto(character) should be(CharacterDto(None, character.name, character.creator, character.creationDate.toString))
  }

  test("should convert digital container to digital container dto") {
    val movie = Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Set(Genre("crime", "Crime"), Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val englishSoundtrack = Soundtrack("en", "dts", "English", "DTS")
    val hungarianSoundtrack = Soundtrack("hu", "dts", "Hungarian", "DTS")
    val englishSubtitle = Subtitle("en", "English")
    val hungarianSubtitle = Subtitle("hu", "Hungarian")
    val digitalContainer = DigitalContainer(movie, Set(englishSoundtrack, hungarianSoundtrack), Set(englishSubtitle, hungarianSubtitle))
    toDigitalContainerDto(digitalContainer) should be(DigitalContainerDto(None, toMotionPictureDto(movie), digitalContainer.soundtracks.map(toSoundtrackDto(_)), digitalContainer.subtitles.map(toSubtitleDto(_))))
  }

  test("should convert genre to genre dto") {
    val genre = Genre("crime", "Crime")
    toGenreDto(genre) should be(GenreDto(None, genre.code, Some(genre.name.get.text)))
  }
  
  test("should convert movie to movie dto") {
    val movie = Movie("Pulp fiction", Set(LocalizedText("Ponyvaregény")(HungarianLocale), LocalizedText("Pulp fiction")(ItalianLocale)), Set(Genre("crime", "Crime"), Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    toMotionPictureDto(movie)(HungarianLocale) should be(MovieDto(None, movie.originalTitle.text, Some(movie.localizedTitles.filter(_.locale == HungarianLocale).head.text), movie.genres.map(toGenreDto(_)), movie.runtime.getMillis, movie.releaseDate.toString))
  }
  
  test("should convert movie to movie dto if no localized title matches the current locale") {
    val movie = Movie("Pulp fiction", Set(LocalizedText("Pulp fiction")(ItalianLocale)), Set(Genre("crime", "Crime"), Genre("thriller", "Thriller")), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    toMotionPictureDto(movie)(HungarianLocale) should be(MovieDto(None, movie.originalTitle.text, None, movie.genres.map(toGenreDto(_)), movie.runtime.getMillis, movie.releaseDate.toString))
  }
  
  test("should convert person to person dto") {
    val person = Person("John Joseph Travolta", Male, new LocalDate(1954, 2, 18), "Englewood, New Jersey, U.S.")
    toPersonDto(person) should be(PersonDto(None, person.name, person.gender.toString, person.dateOfBirth.toString, person.placeOfBirth))
  }
  
  test("should convert soundtrack to soundtrack dto") {
    val soundtrack = Soundtrack("en", "dts", "English", "DTS")
    toSoundtrackDto(soundtrack) should be(SoundtrackDto(None, soundtrack.languageCode, soundtrack.formatCode, Some(soundtrack.languageName.get.text), Some(soundtrack.formatName.get.text)))
  }

  test("should convert soundtrack to soundtrack dto if the language and format names are not defined") {
    val soundtrack = Soundtrack("en", "dts")
    toSoundtrackDto(soundtrack) should be(SoundtrackDto(None, soundtrack.languageCode, soundtrack.formatCode, None, None))
  }

  test("should convert subtitle to subtitle dto") {
    val subtitle = Subtitle("en", "English")
    toSubtitleDto(subtitle) should be(SubtitleDto(None, subtitle.languageCode, Some(subtitle.languageName.get.text)))
  }

  test("should convert subtitle to subtitle dto if the language and format names are not defined") {
    val subtitle = Subtitle("en")
    toSubtitleDto(subtitle) should be(SubtitleDto(None, subtitle.languageCode, None))
  }
}
