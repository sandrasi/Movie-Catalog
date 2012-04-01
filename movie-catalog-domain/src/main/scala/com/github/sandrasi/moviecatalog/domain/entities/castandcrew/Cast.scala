package com.github.sandrasi.moviecatalog.domain.entities.castandcrew

import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.core._

trait Cast extends FilmCrew {

  def character: Character

  Validate.notNull(character)
}
