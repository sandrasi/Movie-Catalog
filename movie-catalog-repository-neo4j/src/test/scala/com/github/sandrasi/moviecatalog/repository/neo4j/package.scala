package com.github.sandrasi.moviecatalog.repository

import scala.language.implicitConversions
import java.util.Locale
import java.util.Locale.US
import com.github.sandrasi.moviecatalog.common.LocalizedTextConverters._

package object neo4j {

  implicit def stringToLocalizedTextConverter(str: String)(implicit locale: Locale = US) = stringToLocalizedText(str)(locale)
}
