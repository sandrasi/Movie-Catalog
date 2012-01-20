package com.github.sandrasi.moviecatalog.domain.entities.container

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.core.MotionPicture

class DigitalContainer(val motionPicture: MotionPicture,
                       val soundtracks: Set[Soundtrack],
                       val subtitles: Set[Subtitle],
                       id: Long) extends LongIdEntity(id) {

  Validate.notNull(motionPicture)
  Validate.noNullElements(soundtracks)
  Validate.noNullElements(subtitles)

  override def equals(o: Any): Boolean = o match {
    case other: DigitalContainer => (motionPicture == other.motionPicture) && (soundtracks == other.soundtracks) && (subtitles == other.subtitles)
    case _ => false
  }

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
            id: Long = 0) = new DigitalContainer(motionPicture, soundtracks, subtitles, id)
}
