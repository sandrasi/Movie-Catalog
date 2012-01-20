package com.github.sandrasi.moviecatalog.domain.entities.container

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

class Soundtrack(val languageCode: String,
                 val formatCode: String,
                 val languageName: Option[LocalizedText],
                 val formatName: Option[LocalizedText],
                 id: Long) extends LongIdEntity(id) {

  Validate.notBlank(languageCode)
  Validate.notBlank(formatCode)
  Validate.notNull(languageName)
  Validate.notNull(formatName)

  override def equals(o: Any): Boolean = o match {
    case other: Soundtrack => (languageCode == other.languageCode) && (formatCode == other.formatCode)
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + languageCode.hashCode
    result = 5 * result + formatCode.hashCode
    result
  }
}

object Soundtrack {

  def apply(languageCode: String,
            formatCode: String,
            languageName: Option[LocalizedText] = None,
            formatName: Option[LocalizedText] = None,
            id: Long = 0) = new Soundtrack(languageCode, formatCode, languageName, formatName, id)
}
