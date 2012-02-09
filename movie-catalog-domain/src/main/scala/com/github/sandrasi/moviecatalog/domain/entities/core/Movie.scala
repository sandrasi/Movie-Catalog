package com.github.sandrasi.moviecatalog.domain.entities.core

import org.joda.time.{LocalDate, Duration, ReadableDuration}
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

class Movie(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText],
            length: ReadableDuration,
            releaseDate: LocalDate,
            version: Long,
            id: Long) extends MotionPicture(originalTitle, localizedTitles, length, releaseDate, version, id) {

  override def equals(o: Any): Boolean = o.isInstanceOf[Movie] && super.equals(o)
}

object Movie {

  def apply(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText] = Set(),
            length: ReadableDuration = Duration.ZERO,
            releaseDate: LocalDate = new LocalDate(0),
            version: Long = 0,
            id: Long = 0) = new Movie(originalTitle, localizedTitles, length, releaseDate, version, id)
}
