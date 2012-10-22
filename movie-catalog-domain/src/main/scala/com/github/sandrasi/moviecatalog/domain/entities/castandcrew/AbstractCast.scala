package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.core._

abstract class AbstractCast(override val person: Person,
                            override val character: Character,
                            override val motionPicture: MotionPicture,
                            version: Long,
                            _id: Long) extends VersionedLongIdEntity(version, _id) with Cast {

  override def equals(o: Any): Boolean = o match {
    case other: AbstractCast => other.canEqual(this) && (person == other.person) && (character == other.character) && (motionPicture == other.motionPicture)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[AbstractCast]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + person.hashCode
    result = 5 * result + character.hashCode
    result = 5 * result + motionPicture.hashCode
    result
  }
}
