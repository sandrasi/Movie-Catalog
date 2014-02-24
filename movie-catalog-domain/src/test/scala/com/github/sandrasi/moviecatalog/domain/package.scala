package com.github.sandrasi.moviecatalog

import com.github.sandrasi.moviecatalog.common.LocalizedTextConverters.stringToLocalizedText
import java.util.Locale
import java.util.Locale.US
import scala.language.implicitConversions

package object domain {

  implicit def stringToLocalizedTextConverter(str: String)(implicit locale: Locale = US) = stringToLocalizedText(str)(locale)
}
