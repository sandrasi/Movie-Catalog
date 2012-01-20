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

  private final val Hu = new Locale("hu", "HU")
  private final val It = Locale.ITALY
  private final val MovieTitleUs = LocalizedText("Test movie title")
  private final val MovieTitleHu = LocalizedText("Teszt film cím", Hu)
  private final val MovieTitleIt = LocalizedText("Prova film titolo", It)

  test("should create movie with given original title and default attributes") {
    val subject = Movie(MovieTitleUs)
    subject.originalTitle should be(MovieTitleUs)
    subject.localizedTitles should be('empty)
    subject.length should be(Duration.ZERO)
    subject.releaseDate should be(new LocalDate(0))
    subject.id should be(None)
  }

  test("should create movie with specified localized titles") {
    val subject = Movie(MovieTitleUs, localizedTitles = Set(MovieTitleHu, MovieTitleIt))
    subject.localizedTitles should be(Set(MovieTitleHu, MovieTitleIt))
  }

  test("should create movie with specified length") {
    val subject = Movie(MovieTitleUs, length = Duration.standardMinutes(90))
    subject.length should be(Duration.standardMinutes(90))
  }

  test("should create movie with specified release date") {
    val subject = Movie(MovieTitleUs, releaseDate = new LocalDate(2011, 1, 1))
    subject.releaseDate should be(new LocalDate(2011, 1, 1))
  }

  test("should compare two objects for equality") {
    val movie = Movie(MovieTitleUs, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2011, 1, 1))
    val motionPicture = new MotionPicture(MovieTitleUs, Set(MovieTitleHu), Duration.standardMinutes(90), new LocalDate(2011, 1, 1), 0) {}
    movie should not equal(motionPicture)
  }
}
