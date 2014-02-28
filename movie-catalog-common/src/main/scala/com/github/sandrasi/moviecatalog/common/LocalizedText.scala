package com.github.sandrasi.moviecatalog.common

import java.util.Locale
import java.util.Locale.US

case class LocalizedText(text: String)(implicit val locale: Locale = US) {

  Validate.notNull(text)
  Validate.notNull(locale)

  override def equals(o: Any): Boolean = o match {
    case other: LocalizedText => other.canEqual(this) && (text == other.text) && (locale == other.locale)
    case _ => false
  }
  
  def canEqual(o: Any): Boolean = o.isInstanceOf[LocalizedText]

  override def hashCode: Int = {
    var result = 2
    result = 3 * result + text.hashCode
    result = 3 * result + locale.hashCode
    result
  }

  override def toString: String = """"%s" [%s]""".format(text, locale)
}
