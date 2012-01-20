package com.github.sandrasi.moviecatalog.common

object Validate {

  def isTrue(req: Boolean, msg: String = null) { if (msg != null) require(req, msg) else require(req) }

  def isTrueForAll[A](tr: Traversable[A], pred: A => Boolean, msg: String = null) { tr.foreach(elem => isTrue(pred(elem), msg)) }

  def notNull(obj: Any, msg: String = null) { isTrue(obj != null, msg) }

  def notBlank(str: String, msg: String = null) { isTrue(!isBlankStr(str), msg) }

  def noNullElements[A](tr: Traversable[A], msg: String = null) { isTrue(tr != null, msg); isTrueForAll(tr, (e: A) => e != null, msg) }

  def noBlankElements(tr: Traversable[String], msg: String = null) { isTrue(tr != null, msg); isTrueForAll(tr, (str: String) => !isBlankStr(str), msg) }

  private def isBlankStr(str: String) = str == null || str.trim.isEmpty
}
