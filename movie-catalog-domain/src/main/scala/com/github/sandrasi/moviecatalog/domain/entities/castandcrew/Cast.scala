package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._

trait Cast extends Crew {

  Validate.notNull(character)

  def character: Character

  override def equals(o: Any): Boolean = o match {
    case other: Cast => other.canEqual(this) && super.equals(o) && character == other.character
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Cast]

  override def hashCode: Int = {
    var result = super.hashCode
    result = 5 * result + character.hashCode
    result
  }
}
