package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity

trait Crew extends VersionedLongIdEntity {

  Validate.notNull(person)
  Validate.notNull(motionPicture)

  def person: Person
  def motionPicture: MotionPicture

  override def equals(o: Any): Boolean = o match {
    case other: Crew => other.canEqual(this) && (person == other.person) && (motionPicture == other.motionPicture)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + person.hashCode
    result = 5 * result + motionPicture.hashCode
    result
  }

  override def canEqual(o: Any) = o.isInstanceOf[Crew]
}
