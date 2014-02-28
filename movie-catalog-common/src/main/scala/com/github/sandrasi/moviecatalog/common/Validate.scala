package com.github.sandrasi.moviecatalog.common

object Validate {

  def valid(req: Boolean, msg: String = null) { if (msg != null) require(req, msg) else require(req) }

  def validForAll[A](tr: Traversable[A], pred: A => Boolean, msg: String = null) { tr.foreach(elem => valid(pred(elem), msg)) }

  def notNull(obj: Any, msg: String = null) { valid(obj != null, msg) }

  def notBlank(str: String, msg: String = null) { valid(!isBlank(str), msg) }

  def noNullElements[A](tr: Traversable[A], msg: String = null) { valid(tr != null, msg); validForAll(tr, (e: A) => e != null, msg) }

  def noBlankElements(tr: Traversable[String], msg: String = null) { valid(tr != null, msg); validForAll(tr, (str: String) => !isBlank(str), msg) }

  private def isBlank(str: String) = str == null || str.trim.isEmpty
}
