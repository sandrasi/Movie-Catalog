package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

abstract class VersionedLongIdEntity(override val version: Long, _id: Long) extends BaseEntity[Long](if (_id != 0) Some(_id) else None) with VersionSupport {

  Validate.isTrue(_id >= 0)
}
