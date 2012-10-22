package com.github.sandrasi.moviecatalog.domain.entities.core

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity

class Character(val name: String,
                version: Long,
                _id: Long) extends VersionedLongIdEntity(version, _id) {

  Validate.notNull(name)

  override def equals(o: Any): Boolean = o match {
    case other: Character => (name == other.name) && (id == other.id)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[Character]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + name.hashCode
    result
  }
}

object Character {

  def apply(name: String,
            version: Long = 0,
            id: Long = 0) = new Character(name, version, id)
}
