package com.github.sandrasi.moviecatalog.domain.entities.core

import org.joda.time.{LocalDate, ReadableDuration}
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

abstract class MotionPicture(val originalTitle: LocalizedText,
                             val localizedTitles: Set[LocalizedText],
                             val length: ReadableDuration,
                             val releaseDate: LocalDate,
                             version: Long,
                             id: Long) extends VersionedLongIdEntity(version, id) {

  Validate.notNull(originalTitle)
  Validate.noNullElements(localizedTitles)
  Validate.notNull(length)

  override def equals(o: Any): Boolean = o match {
    case other: MotionPicture => (originalTitle == other.originalTitle) && (releaseDate == other.releaseDate)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + originalTitle.hashCode
    result = 5 * result + releaseDate.hashCode
    result
  }
}
