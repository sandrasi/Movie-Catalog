package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._

trait FilmCrew {

  val person: Person
  val motionPicture: MotionPicture

  Validate.notNull(person)
  Validate.notNull(motionPicture)
}
