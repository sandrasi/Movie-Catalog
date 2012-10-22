package com.github.sandrasi.moviecatalog.domain.entities.container

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.core.MotionPicture

class DigitalContainer(val motionPicture: MotionPicture,
                       val soundtracks: Set[Soundtrack],
                       val subtitles: Set[Subtitle],
                       version: Long,
                       _id: Long) extends VersionedLongIdEntity(version, _id) {

  Validate.notNull(motionPicture)
  Validate.noNullElements(soundtracks)
  Validate.noNullElements(subtitles)

  override def equals(o: Any): Boolean = o match {
    case other: DigitalContainer => (motionPicture == other.motionPicture) && (soundtracks == other.soundtracks) && (subtitles == other.subtitles)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[DigitalContainer]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + motionPicture.hashCode
    result = 5 * result + soundtracks.hashCode
    result = 5 * result + subtitles.hashCode
    result
  }

  override def toString: String = "%s(id: %s, version: %d, motionPicture: %s, soundtracks: %s, subtitles: %s)".format(getClass.getSimpleName, id, version, motionPicture, soundtracks, subtitles)
}

object DigitalContainer {
  
  def apply(motionPicture: MotionPicture,
            soundtracks: Set[Soundtrack] = Set(),
            subtitles: Set[Subtitle] = Set(),
            version: Long = 0,
            id: Long = 0) = new DigitalContainer(motionPicture, soundtracks, subtitles, version, id)
}
