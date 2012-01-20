package com.github.sandrasi.moviecatalog.repository

import java.util.Locale
import com.github.sandrasi.moviecatalog.domain.entities.base.LongIdEntity

trait Repository {

  def get[A <: LongIdEntity](id: Long, entityType: Class[A])(implicit locale: Locale): Option[A]

  def save[A <: LongIdEntity](entity: A)(implicit locale: Locale): A

  def search(text: String)(implicit locale: Locale): Set[LongIdEntity]
}
