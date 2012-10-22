package com.github.sandrasi.moviecatalog.domain.entities.core

import org.joda.time.LocalDate
import com.github.sandrasi.moviecatalog.common.Validate
import com.github.sandrasi.moviecatalog.domain.entities.base.VersionedLongIdEntity
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

class Person(val name: String,
             val gender: Gender,
             val dateOfBirth: LocalDate,
             val placeOfBirth: String,
             version: Long,
             _id: Long) extends VersionedLongIdEntity(version, _id) {
  
  Validate.notNull(name)
  Validate.notNull(gender)
  Validate.notNull(dateOfBirth)
  Validate.notNull(placeOfBirth)

  override def equals(o: Any): Boolean = o match {
    case other: Person => (name == other.name) && (gender == other.gender) && (dateOfBirth == other.dateOfBirth) && (placeOfBirth == other.placeOfBirth)
    case _ => false
  }

  override protected def canEqual(o: Any) = o.isInstanceOf[Person]

  override def hashCode: Int = {
    var result = 3
    result = 5 * result + name.hashCode
    result = 5 * result + dateOfBirth.hashCode
    result = 5 * result + gender.hashCode
    result
  }
}

object Person {
  
  def apply(name: String,
            gender: Gender,
            dateOfBirth: LocalDate,
            placeOfBirth: String,
            version: Long = 0,
            id: Long = 0) = new Person(name, gender, dateOfBirth, placeOfBirth, version, id)
}
