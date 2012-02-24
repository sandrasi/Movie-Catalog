package com.github.sandrasi.moviecatalog.domain.entities.base

import com.github.sandrasi.moviecatalog.common.Validate

trait VersionSupport {

  self: BaseEntity[_] =>

  val version: Long

  Validate.isTrue(version >= 0)
}