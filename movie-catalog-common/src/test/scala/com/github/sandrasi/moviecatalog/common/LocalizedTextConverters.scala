package com.github.sandrasi.moviecatalog.common

import java.util.Locale
import java.util.Locale.US
import scala.language.implicitConversions

object LocalizedTextConverters {

  implicit def stringToLocalizedText(str: String)(implicit locale: Locale = US): LocalizedText = LocalizedText(str)
}
