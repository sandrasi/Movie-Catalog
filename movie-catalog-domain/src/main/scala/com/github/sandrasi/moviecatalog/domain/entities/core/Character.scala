package com.github.sandrasi.moviecatalog.domain.entities.core

import java.util.UUID
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity

class Character(val name: String, val discriminator: String, id: Long) extends LongIdEntity(id) {

  Validate.notNull(name)
  Validate.notNull(discriminator)

  override def equals(o: Any): Boolean = o match {
    case other: Character => (name == other.name) && (discriminator == other.discriminator)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + name.hashCode
    result = 5 * result + discriminator.hashCode
    result
  }
}

object Character {

  def apply(name: String, discriminator: String = UUID.randomUUID.toString, id: Long = 0) = new Character(name, discriminator, id)
}
