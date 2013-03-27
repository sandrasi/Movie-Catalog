package com.github.sandrasi.moviecatalog.common

import scala.language.implicitConversions
import java.util.Locale
import java.util.Locale.US

object LocalizedTextConverters {

  implicit def stringToLocalizedText(str: String)(implicit locale: Locale = US): LocalizedText = LocalizedText(str)
}
