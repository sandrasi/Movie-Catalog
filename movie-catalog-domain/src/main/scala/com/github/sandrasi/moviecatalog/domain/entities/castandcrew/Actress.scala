package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Female

case class Actress(person: Person, character: Character, motionPicture: MotionPicture, version: Long, id: Option[Long]) extends Cast {

  Validate.isTrue(person.gender == Female)

  override def equals(o: Any): Boolean = o match {
    case other: Actress => other.canEqual(this) && super.equals(o)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Actress]
}

object Actress {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: Long = 0) = new Actress(person, character, motionPicture, version, if (id == 0) None else Some(id))
}
