package com.github.sandrasi.moviecatalog.domain.entities.core

import org.joda.time.{LocalDate, ReadableDuration}
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

abstract class MotionPicture(val originalTitle: LocalizedText,
                             val localizedTitles: Set[LocalizedText],
                             val runtime: ReadableDuration,
                             val releaseDate: LocalDate,
                             version: Long,
                             _id: Long) extends VersionedLongIdEntity(version, _id) {

  Validate.notNull(originalTitle)
  Validate.noNullElements(localizedTitles)
  Validate.notNull(runtime)

  override def equals(o: Any): Boolean = o match {
    case other: MotionPicture => (originalTitle == other.originalTitle) && (releaseDate == other.releaseDate)
    case _ => false
  }
  
  override protected def canEqual(o: Any) = o.isInstanceOf[MotionPicture]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + originalTitle.hashCode
    result = 5 * result + releaseDate.hashCode
    result
  }
}
