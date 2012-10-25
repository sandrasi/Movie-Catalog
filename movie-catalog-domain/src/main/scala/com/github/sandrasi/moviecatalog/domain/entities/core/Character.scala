package com.github.sandrasi.moviecatalog.domain.entities.core

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import org.joda.time.LocalDate

class Character(val name: String,
                val creator: String,
                val creationDate: LocalDate,
                version: Long,
                _id: Long) extends VersionedLongIdEntity(version, _id) {

  Validate.notNull(name)
  Validate.notNull(creator)
  Validate.notNull(creationDate)

  override def equals(o: Any): Boolean = o match {
    case other: Character => (name == other.name) && (creator == other.creator) && (creationDate == other.creationDate)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[Character]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + name.hashCode
    result = 5 * result + creator.hashCode
    result = 5 * result + creationDate.hashCode
    result
  }

  override def toString: String = """%s(id: %s, version: %d, name: "%s", creator: "%s", creationDate: %s)""".format(getClass.getSimpleName, id, version, name, creator, creationDate)
}

object Character {

  def apply(name: String,
            creator: String = "",
            creationDate: LocalDate = new LocalDate(0),
            version: Long = 0,
            id: Long = 0) = new Character(name, creator, creationDate, version, id)
}
