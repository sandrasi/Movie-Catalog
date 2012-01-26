package com.github.sandrasi.moviecatalog.domain.entities.container

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

class Subtitle(val languageCode: String, val languageName: Option[LocalizedText], id: Long) extends LongIdEntity(id) {

  Validate.notBlank(languageCode)
  Validate.notNull(languageName)

  override def equals(o: Any): Boolean = o match {
    case other: Subtitle => languageCode == other.languageCode
    case _ => false
  }

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + languageCode.hashCode
    result
  }
}

object Subtitle {

  def apply(languageCode: String, languageName: LocalizedText = null, id: Long = 0) = new Subtitle(languageCode, if (languageName != null) Some(languageName) else None, id)
}
