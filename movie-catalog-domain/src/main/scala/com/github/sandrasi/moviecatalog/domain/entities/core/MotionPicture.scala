package com.github.sandrasi.moviecatalog.domain.entities.core

import org.joda.time.{LocalDate, ReadableDuration}
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

trait MotionPicture extends VersionedLongIdEntity {

  Validate.notNull(originalTitle)
  Validate.noNullElements(localizedTitles)
  Validate.notNull(runtime)
  Validate.notNull(releaseDate)

  def originalTitle: LocalizedText
  def localizedTitles: Set[LocalizedText]
  def runtime: ReadableDuration
  def releaseDate: LocalDate

  override def equals(o: Any): Boolean = o match {
    case other: MotionPicture => other.canEqual(this) && (originalTitle == other.originalTitle) && (releaseDate == other.releaseDate)
    case _ => false
  }
  
  override def canEqual(o: Any) = o.isInstanceOf[MotionPicture]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + originalTitle.hashCode
    result = 5 * result + releaseDate.hashCode
    result
  }
}
