package com.github.sandrasi.moviecatalog.domain

import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DigitalContainerTest extends FunSuite with Matchers {

  private final val PulpFiction = Movie("Pulp fiction")
  private final val EnglishSoundtrack = Soundtrack("en", "dts")
  private final val HungarianSoundtrack = Soundtrack("hu", "dts")
  private final val EnglishSubtitle = Subtitle("en")
  private final val HungarianSubtitle = Subtitle("hu")

  test("should create digital container with given motion picture and default attributes") {
    val subject = DigitalContainer(PulpFiction)
    subject.motionPicture should be(PulpFiction)
    subject.soundtracks should be('empty)
    subject.subtitles should be('empty)
    subject.version should be (0)
    subject.id should be(None)
  }

  test("should create digital container with specified soundtracks") {
    val subject = DigitalContainer(PulpFiction, soundtracks = Set(EnglishSoundtrack, HungarianSoundtrack))
    subject.soundtracks should be(Set(EnglishSoundtrack, HungarianSoundtrack))
  }

  test("should create digital container with specified subtitles") {
    val subject = DigitalContainer(PulpFiction, subtitles = Set(EnglishSubtitle, HungarianSubtitle))
    subject.subtitles should be(Set(EnglishSubtitle, HungarianSubtitle))
  }
  
  test("should not create digital container with null motion picture") {
    intercept[IllegalArgumentException] { DigitalContainer(null) }
  }

  test("should not create digital container with null soundtracks") {
    intercept[IllegalArgumentException] { DigitalContainer(PulpFiction, soundtracks = null) }
  }

  test("should not create digital container with null soundtrack") {
    intercept[IllegalArgumentException] { DigitalContainer(PulpFiction, soundtracks = Set(null)) }
  }

  test("should not create digital container with null subtitles") {
    intercept[IllegalArgumentException] { DigitalContainer(PulpFiction, subtitles = null) }
  }

  test("should not create digital container with null subtitle") {
    intercept[IllegalArgumentException] { DigitalContainer(PulpFiction, subtitles = Set(null)) }
  }

  test("should not create digital container with negative version") {
    intercept[IllegalArgumentException] { DigitalContainer(PulpFiction, version = -1) }
  }

  test("should not create digital container with null id") {
    intercept[IllegalArgumentException] { new DigitalContainer(PulpFiction, Set(EnglishSoundtrack), Set(EnglishSubtitle), 0, id = null) }
  }

  test("should compare two objects for equality") {
    val digitalContainer = DigitalContainer(PulpFiction, Set(EnglishSoundtrack), Set(EnglishSubtitle))
    val otherDigitalContainer = digitalContainer.copy()
    val otherDigitalContainerWithDifferentMotionPicture = digitalContainer.copy(motionPicture = Movie("Die hard: With a vengeance"))
    val otherDigitalContainerWithDifferentSoundtracks = digitalContainer.copy(soundtracks = Set(HungarianSoundtrack))
    val otherDigitalContainerWithDifferentSubtitles = digitalContainer.copy(subtitles = Set(HungarianSubtitle))

    digitalContainer should not equal null
    digitalContainer should not equal new AnyRef
    digitalContainer should not equal otherDigitalContainerWithDifferentMotionPicture
    digitalContainer should not equal otherDigitalContainerWithDifferentSoundtracks
    digitalContainer should not equal otherDigitalContainerWithDifferentSubtitles
    digitalContainer should equal(digitalContainer)
    digitalContainer should equal(otherDigitalContainer)
  }

  test("should calculate hash code") {
    val digitalContainer = DigitalContainer(PulpFiction, Set(EnglishSoundtrack), Set(EnglishSubtitle))
    val otherDigitalContainer = digitalContainer.copy()

    digitalContainer.hashCode should equal(otherDigitalContainer.hashCode)
  }
}
