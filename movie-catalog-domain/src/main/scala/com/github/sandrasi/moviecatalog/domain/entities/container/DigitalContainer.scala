package com.github.sandrasi.moviecatalog.domain.entities.container

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.core.MotionPicture

case class DigitalContainer(motionPicture: MotionPicture, soundtracks: Set[Soundtrack], subtitles: Set[Subtitle], version: Long, id: Option[Long]) extends VersionedLongIdEntity {

  Validate.notNull(motionPicture)
  Validate.noNullElements(soundtracks)
  Validate.noNullElements(subtitles)

  override def equals(o: Any): Boolean = o match {
    case other: DigitalContainer => other.canEqual(this) && (motionPicture == other.motionPicture) && (soundtracks == other.soundtracks) && (subtitles == other.subtitles)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[DigitalContainer]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + motionPicture.hashCode
    result = 5 * result + soundtracks.hashCode
    result = 5 * result + subtitles.hashCode
    result
  }
}

object DigitalContainer {
  
  def apply(motionPicture: MotionPicture,
            soundtracks: Set[Soundtrack] = Set(),
            subtitles: Set[Subtitle] = Set(),
            version: Long = 0,
            id: Long = 0) = new DigitalContainer(motionPicture, soundtracks, subtitles, version, if (id == 0) None else Some(id))
}
