package com.github.sandrasi.moviecatalog.repository

import com.github.sandrasi.moviecatalog.common.LocalizedTextConverters._
import java.util.Locale
import java.util.Locale.US
import scala.language.implicitConversions

package object neo4j {

  implicit def stringToLocalizedTextConverter(str: String)(implicit locale: Locale = US) = stringToLocalizedText(str)(locale)
}
