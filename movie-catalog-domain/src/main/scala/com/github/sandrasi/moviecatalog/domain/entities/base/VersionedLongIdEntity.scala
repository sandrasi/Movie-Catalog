package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

trait VersionedLongIdEntity extends BaseEntity[Long] with VersionSupport {

  Validate.isTrue(id.getOrElse(0l) >= 0l)
}
