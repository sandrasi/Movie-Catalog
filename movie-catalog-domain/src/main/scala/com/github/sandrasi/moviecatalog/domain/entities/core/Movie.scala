package com.github.sandrasi.moviecatalog.domain.entities.core

import org.joda.time.{LocalDate, Duration, ReadableDuration}
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

case class Movie(originalTitle: LocalizedText, localizedTitles: Set[LocalizedText], runtime: ReadableDuration, releaseDate: LocalDate, version: Long, id: Option[Long]) extends MotionPicture {

  override def equals(o: Any): Boolean = o match {
    case other: Movie => other.canEqual(this) && super.equals(o)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Movie]
}

object Movie {

  def apply(originalTitle: LocalizedText,
            localizedTitles: Set[LocalizedText] = Set(),
            runtime: ReadableDuration = Duration.ZERO,
            releaseDate: LocalDate = new LocalDate(0),
            version: Long = 0,
            id: Long = 0) = new Movie(originalTitle, localizedTitles, runtime, releaseDate, version, if (id == 0) None else Some(id))
}
