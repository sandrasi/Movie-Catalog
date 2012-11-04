package com.github.sandrasi.moviecatalog.domain.entities.container

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

case class Soundtrack(languageCode: String, formatCode: String, languageName: Option[LocalizedText], formatName: Option[LocalizedText], version: Long, id: Option[Long]) extends VersionedLongIdEntity {

  Validate.notBlank(languageCode)
  Validate.notBlank(formatCode)
  Validate.notNull(languageName)
  Validate.notNull(formatName)
  if (languageName.isDefined && formatName.isDefined) Validate.isTrue(languageName.get.locale == formatName.get.locale)

  override def equals(o: Any): Boolean = o match {
    case other: Soundtrack => other.canEqual(this) && (languageCode == other.languageCode) && (formatCode == other.formatCode)
    case _ => false
  }

  override def canEqual(o: Any) = o.isInstanceOf[Soundtrack]

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
            languageName: LocalizedText = null,
            formatName: LocalizedText = null,
            version: Long = 0,
            id: Long = 0) = new Soundtrack(languageCode, formatCode, Option(languageName), Option(formatName), version, if (id == 0) None else Some(id))
}
