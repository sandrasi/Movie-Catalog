package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Male

class Actor(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long,
            _id: Long) extends AbstractCast(person, character, motionPicture, version, _id) {

  Validate.isTrue(person.gender == Male)

  override def equals(o: Any) = o match {
    case other: Actor => super.equals(other)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[Actor]
}

object Actor {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: Long = 0) = new Actor(person, character, motionPicture, version, id)
}
