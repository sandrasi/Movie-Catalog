package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

abstract class VersionedLongIdEntity(override val version: Long, id: Long) extends BaseEntity[Long](if (id != 0) Some(id) else None) with VersionSupport {

  Validate.isTrue(id >= 0)
}
