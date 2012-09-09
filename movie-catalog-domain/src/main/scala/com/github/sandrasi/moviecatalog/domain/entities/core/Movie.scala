package com.github.sandrasi.moviecatalog.domain.entities.core

import org.joda.time.{LocalDate, Duration, ReadableDuration}
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

class Movie(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText],
            runtime: ReadableDuration,
            releaseDate: LocalDate,
            version: Long,
            id: Long) extends MotionPicture(originalTitle, localizedTitles, runtime, releaseDate, version, id) {

  override def equals(o: Any): Boolean = o match {
    case other: Movie => super.equals(o)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[Movie]
}

object Movie {

  def apply(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText] = Set(),
            runtime: ReadableDuration = Duration.ZERO,
            releaseDate: LocalDate = new LocalDate(0),
            version: Long = 0,
            id: Long = 0) = new Movie(originalTitle, localizedTitles, runtime, releaseDate, version, id)
}
