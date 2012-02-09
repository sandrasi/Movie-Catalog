package com.github.sandrasi.moviecatalog.repository

import java.util.Locale
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity

trait Repository {

  def get[A <: VersionedLongIdEntity](id: Long, entityType: Class[A])(implicit locale: Locale): Option[A]

  def save[A <: VersionedLongIdEntity](entity: A)(implicit locale: Locale): A
  
  def delete[A <: VersionedLongIdEntity](id: Long, entityType: Class[A])

  def search(text: String)(implicit locale: Locale): Set[VersionedLongIdEntity]
}
