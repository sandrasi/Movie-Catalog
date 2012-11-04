package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Male

case class Actor(person: Person, character: Character, motionPicture: MotionPicture, version: Long, id: Option[Long]) extends Cast {

  Validate.isTrue(person.gender == Male)

  override def equals(o: Any): Boolean = o match {
    case other: Actor => other.canEqual(this) && super.equals(o)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Actor]
}

object Actor {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: Long = 0) = new Actor(person, character, motionPicture, version, if (id == 0) None else Some(id))
}
