package com.github.sandrasi.moviecatalog.domain.entities.core

import java.util.Locale
import org.joda.time.{Duration, LocalDate}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

@RunWith(classOf[JUnitRunner])
class MovieTest extends FunSuite with ShouldMatchers {

  private final val EnglishMovieTitle = LocalizedText("Pulp fiction")
  private final val HungarianMovieTitle = LocalizedText("Ponyvaregény")(new Locale("hu", "HU"))
  private final val ItalianMovieTitle = LocalizedText("Pulp fiction")(Locale.ITALY)

  test("should create movie with given original title and default attributes") {
    val subject = Movie(EnglishMovieTitle)
    subject.originalTitle should be(EnglishMovieTitle)
    subject.localizedTitles should be('empty)
    subject.runtime should be(Duration.ZERO)
    subject.releaseDate should be(new LocalDate(0))
    subject.version should be(0)
    subject.id should be(None)
  }

  test("should create movie with specified localized titles") {
    val subject = Movie(EnglishMovieTitle, localizedTitles = Set(HungarianMovieTitle, ItalianMovieTitle))
    subject.localizedTitles should be(Set(HungarianMovieTitle, ItalianMovieTitle))
  }

  test("should create movie with specified runtime") {
    val subject = Movie(EnglishMovieTitle, runtime = Duration.standardMinutes(154))
    subject.runtime should be(Duration.standardMinutes(154))
  }

  test("should create movie with specified release date") {
    val subject = Movie(EnglishMovieTitle, releaseDate = new LocalDate(1994, 10, 14))
    subject.releaseDate should be(new LocalDate(1994, 10, 14))
  }

  test("should compare two objects for equality") {
    val movie = Movie(EnglishMovieTitle, Set(HungarianMovieTitle), Duration.standardMinutes(154), new LocalDate(1994, 10, 14))
    val motionPicture = new MotionPicture() {
      override def id = None
      override def version = 0l
      override def originalTitle = EnglishMovieTitle
      override def localizedTitles = Set(HungarianMovieTitle)
      override def runtime = Duration.standardMinutes(154)
      override def releaseDate = new LocalDate(1994, 10, 14)
    }
    movie should not equal(motionPicture)
  }
}
