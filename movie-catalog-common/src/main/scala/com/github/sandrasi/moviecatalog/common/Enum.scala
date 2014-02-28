package com.github.sandrasi.moviecatalog.common

import scala.collection.immutable.ListMap

trait Enum[A] {

  private var _values = ListMap.empty[String, A]

  def values: List[A] = _values.values.toList
  def valueOf(str: String): A = if (_values.contains(str)) _values(str) else throw new NoSuchElementException(s"$this.$str")

  trait Value { self: A =>
    _values += this.toString -> this
  }
}
