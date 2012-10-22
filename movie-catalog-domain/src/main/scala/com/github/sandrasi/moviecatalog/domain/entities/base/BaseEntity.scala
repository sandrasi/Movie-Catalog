package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

abstract class BaseEntity[A](_id: Option[A]) {

  Validate.notNull(_id)

  def id: Option[A] = _id

  override def equals(o: Any): Boolean = o match {
    case other: BaseEntity[_] => other.canEqual(this) && _id == other.id
    case _ => false
  }

  protected def canEqual(o: Any): Boolean = o.isInstanceOf[BaseEntity[_]]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + _id.hashCode
    result
  }
}
