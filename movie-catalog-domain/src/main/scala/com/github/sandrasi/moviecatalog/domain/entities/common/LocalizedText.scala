package com.github.sandrasi.moviecatalog.domain.entities.common

import java.util.Locale
import com.github.sandrasi.moviecatalog.common.Validate

class LocalizedText(val text: String)(implicit val locale: Locale = Locale.US) {

  Validate.notNull(text)
  Validate.notNull(locale)

  override def equals(o: Any): Boolean = o match {
    case other: LocalizedText => (text == other.text) && (locale == other.locale)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + text.hashCode
    result = 5 * result + locale.hashCode
    result
  }
}

object LocalizedText {

  def apply(text: String)(implicit locale: Locale = Locale.US) = new LocalizedText(text)
  
  implicit def stringToLocalizedText(str: String)(implicit locale: Locale = Locale.US): LocalizedText = LocalizedText(str)
}
