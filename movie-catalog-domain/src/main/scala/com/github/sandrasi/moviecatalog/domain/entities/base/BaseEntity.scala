package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

abstract class BaseEntity[A](val id: Option[A]) {
  
  Validate.notNull(id)

  override def equals(o: Any): Boolean = o match {
    case other: BaseEntity[_] => other.canEqual(this) && id == other.id
    case _ => false
  }

  protected def canEqual(o: Any): Boolean = o.isInstanceOf[BaseEntity[_]]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + id.hashCode
    result
  }
}
