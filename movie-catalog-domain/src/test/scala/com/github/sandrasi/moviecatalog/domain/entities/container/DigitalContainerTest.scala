package com.github.sandrasi.moviecatalog.domain.entities.container

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.entities.core.Movie

@RunWith(classOf[JUnitRunner])
class DigitalContainerTest extends FunSuite with ShouldMatchers {

  private final val MotionPicture = Movie("Test movie title")
  private final val SoundtrackEn = Soundtrack("en", "dts")
  private final val SoundtrackHu = Soundtrack("hu", "dts")
  private final val SubtitleEn = Subtitle("en")
  private final val SubtitleHu = Subtitle("hu")

  test("should create digital container with given motion picture and default attributes") {
    val subject = DigitalContainer(MotionPicture)
    subject.motionPicture should be(MotionPicture)
    subject.soundtracks should be('empty)
    subject.subtitles should be('empty)
    subject.version should be (0)
    subject.id should be(None)
  }

  test("should create digital container with specified soundtracks") {
    val subject = DigitalContainer(MotionPicture, soundtracks = Set(SoundtrackEn, SoundtrackHu))
    subject.soundtracks should be(Set(SoundtrackEn, SoundtrackHu))
  }

  test("should create digital container with specified subtitles") {
    val subject = DigitalContainer(MotionPicture, subtitles = Set(SubtitleEn, SubtitleHu))
    subject.subtitles should be(Set(SubtitleEn, SubtitleHu))
  }
  
  test("should not create digital container with null motion picture") {
    intercept[IllegalArgumentException] {
      DigitalContainer(null)
    }
  }

  test("should not create digital container with null soundtracks") {
    intercept[IllegalArgumentException] {
      DigitalContainer(MotionPicture, soundtracks = null)
    }
  }

  test("should not create digital container with null soundtrack") {
    intercept[IllegalArgumentException] {
      DigitalContainer(MotionPicture, soundtracks = Set(null))
    }
  }

  test("should not create digital container with null subtitles") {
    intercept[IllegalArgumentException] {
      DigitalContainer(MotionPicture, subtitles = null)
    }
  }

  test("should not create digital container with null subtitle") {
    intercept[IllegalArgumentException] {
      DigitalContainer(MotionPicture, subtitles = Set(null))
    }
  }

  test("should compare two objects for equality") {
    val digitalContainer = DigitalContainer(MotionPicture, Set(SoundtrackEn), Set(SubtitleEn))
    val otherDigitalContainer = DigitalContainer(MotionPicture, Set(SoundtrackEn), Set(SubtitleEn))
    val otherDigitalContainerWithDifferentMotionPicture = DigitalContainer(Movie("Other movie"), Set(SoundtrackEn), Set(SubtitleEn))
    val otherDigitalContainerWithDifferentSoundtracks = DigitalContainer(MotionPicture, Set(SoundtrackHu), Set(SubtitleEn))
    val otherDigitalContainerWithDifferentSubtitles = DigitalContainer(MotionPicture, Set(SoundtrackEn), Set(SubtitleHu))

    digitalContainer should not equal(null)
    digitalContainer should not equal(new AnyRef)
    digitalContainer should not equal(otherDigitalContainerWithDifferentMotionPicture)
    digitalContainer should not equal(otherDigitalContainerWithDifferentSoundtracks)
    digitalContainer should not equal(otherDigitalContainerWithDifferentSubtitles)
    digitalContainer should equal(digitalContainer)
    digitalContainer should equal(otherDigitalContainer)
  }

  test("should calculate hash code") {
    val digitalContainer = DigitalContainer(MotionPicture, Set(SoundtrackEn), Set(SubtitleEn))
    val otherDigitalContainer = DigitalContainer(MotionPicture, Set(SoundtrackEn), Set(SubtitleEn))

    digitalContainer.hashCode should equal(otherDigitalContainer.hashCode)
  }
}
