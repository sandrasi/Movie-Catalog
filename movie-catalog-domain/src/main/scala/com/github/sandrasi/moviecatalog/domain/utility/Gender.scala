package com.github.sandrasi.moviecatalog.domain.utility

import com.github.sandrasi.moviecatalog.common.Enum

sealed trait Gender extends Gender.Value

case object Gender extends Enum[Gender] {

  case object Male extends Gender
  case object Female extends Gender

  Male; Female
}
