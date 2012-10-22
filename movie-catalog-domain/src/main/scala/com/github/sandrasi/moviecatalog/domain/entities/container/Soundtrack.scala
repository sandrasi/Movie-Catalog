package com.github.sandrasi.moviecatalog.domain.entities.container

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.entities.common.LocalizedText

class Soundtrack(val languageCode: String,
                 val formatCode: String,
                 val languageName: Option[LocalizedText],
                 val formatName: Option[LocalizedText],
                 version: Long,
                 _id: Long) extends VersionedLongIdEntity(version, _id) {

  Validate.notBlank(languageCode)
  Validate.notBlank(formatCode)
  Validate.notNull(languageName)
  Validate.notNull(formatName)
  if (languageName != None && formatName != None) Validate.isTrue(languageName.get.locale == formatName.get.locale)

  override def equals(o: Any): Boolean = o match {
    case other: Soundtrack => (languageCode == other.languageCode) && (formatCode == other.formatCode)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[Soundtrack]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + languageCode.hashCode
    result = 5 * result + formatCode.hashCode
    result
  }

  override def toString: String = """%s(id: %s, version: %d, languageCode: "%s", languageName: %s, formatCode: "%s", formatName: %s)""".format(getClass.getSimpleName, id, version, languageCode, languageName, formatCode, formatName)
}

object Soundtrack {

  def apply(languageCode: String,
            formatCode: String,
            languageName: LocalizedText = null,
            formatName: LocalizedText = null,
            version: Long = 0,
            id: Long = 0) = new Soundtrack(languageCode, formatCode, if (languageName != null) Some(languageName) else None, if (formatName != null) Some(formatName) else None, version, id)
}
