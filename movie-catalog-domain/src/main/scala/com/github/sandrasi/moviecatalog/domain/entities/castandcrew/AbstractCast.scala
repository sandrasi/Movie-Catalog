package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.entities.base.{LongIdEntity, VersionSupport}

abstract class AbstractCast(override val person: Person,
                            override val character: Character,
                            override val motionPicture: MotionPicture,
                            override val version: Long,
                            id: Long) extends LongIdEntity(id) with Cast with VersionSupport {

  override def equals(o: Any): Boolean = o match {
    case other: AbstractCast => (person == other.person) && (character == other.character) && (motionPicture == other.motionPicture)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + person.hashCode
    result = 5 * result + character.hashCode
    result = 5 * result + motionPicture.hashCode
    result
  }
}
