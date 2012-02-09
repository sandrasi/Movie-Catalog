package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._
import com.github.sandrasi.moviecatalog.domain.utility.Gender.Female

class Actress(person: Person,
              character: Character,
              motionPicture: MotionPicture,
              version: Long,
              id: Long) extends AbstractCast(person, character, motionPicture, version, id) {

  Validate.isTrue(person.gender == Female)

  override def equals(o: Any): Boolean = o.isInstanceOf[Actress] && super.equals(o)
}

object Actress {

  def apply(person: Person,
            character: Character,
            motionPicture: MotionPicture,
            version: Long = 0,
            id: Long = 0) = new Actress(person, character, motionPicture, version, id)
}
