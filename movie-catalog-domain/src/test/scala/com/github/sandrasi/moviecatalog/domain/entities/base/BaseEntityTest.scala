package com.github.sandrasi.moviecatalog.domain.entities.base

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class BaseEntityTest extends FunSuite with ShouldMatchers {
  
  test("should create entity with given id") {
    TestBaseEntity(Some(1)).id should be(Some(1))
  }

  test("should should not create entity with null id") {
    intercept[IllegalArgumentException] {
      TestBaseEntity(null)
    }
  }
  
  test("should compare two objects for equality") {
    val baseEntity = TestBaseEntity(Some(1))
    val otherTestBaseEntity = TestBaseEntity(Some(1))
    val otherTestBaseEntityWithDifferentId = TestBaseEntity(Some(2))

    baseEntity should not equal(null)
    baseEntity should not equal(new AnyRef)
    baseEntity should not equal(otherTestBaseEntityWithDifferentId)
    baseEntity should equal(baseEntity)
    baseEntity should equal(otherTestBaseEntity)
  }

  test("should calculate hash code") {
    val baseEntity = TestBaseEntity(Some(1))
    val otherBaseEntity = TestBaseEntity(Some(1))

    baseEntity.hashCode should equal(otherBaseEntity.hashCode)
  }
}

private case class TestBaseEntity(id: Option[Int]) extends BaseEntity[Int]
