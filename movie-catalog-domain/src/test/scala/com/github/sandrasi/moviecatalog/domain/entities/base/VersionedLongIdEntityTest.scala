package com.github.sandrasi.moviecatalog.domain.entities.base

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class VersionedLongIdEntityTest extends FunSuite with ShouldMatchers {

  test("should create entity with non-negative id") {
    TestVersionedLongIdEntity(0, Some(1)).id should be(Some(1))
  }

  test("should not create entity with negative id") {
    intercept[IllegalArgumentException] {
      TestVersionedLongIdEntity(0, Some(-11))
    }
  }
}

private case class TestVersionedLongIdEntity(version: Long, id: Option[Long]) extends VersionedLongIdEntity
