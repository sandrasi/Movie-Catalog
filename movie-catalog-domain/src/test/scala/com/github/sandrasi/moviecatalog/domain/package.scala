package com.github.sandrasi.moviecatalog

import scala.language.implicitConversions
import java.util.Locale
import java.util.Locale.US
import com.github.sandrasi.moviecatalog.common.LocalizedTextConverters._

package object domain {

  implicit def stringToLocalizedTextConverter(str: String)(implicit locale: Locale = US) = stringToLocalizedText(str)(locale)
}
