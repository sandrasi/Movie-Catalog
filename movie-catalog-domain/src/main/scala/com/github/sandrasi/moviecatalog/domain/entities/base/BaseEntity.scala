package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

trait BaseEntity[A] extends Equals {

  Validate.notNull(id)

  def id: Option[A]

  override def equals(o: Any): Boolean = o match {
    case other: BaseEntity[_] => other.canEqual(this) && id == other.id
    case _ => false
  }

  override def canEqual(o: Any): Boolean = o.isInstanceOf[BaseEntity[_]]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + id.hashCode
    result
  }
}
