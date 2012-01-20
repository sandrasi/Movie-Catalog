package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

abstract class LongIdEntity(id: Long) extends BaseEntity[Long](if (id != 0) Some(id) else None) {

  Validate.isTrue(id >= 0)
}
