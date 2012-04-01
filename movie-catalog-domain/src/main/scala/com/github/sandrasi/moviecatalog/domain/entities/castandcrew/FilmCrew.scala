package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._

trait FilmCrew {

  def person: Person
  def motionPicture: MotionPicture

  Validate.notNull(person)
  Validate.notNull(motionPicture)
}
