package com.github.sandrasi.moviecatalog.domain.entities.base

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class VersionSupportTest extends FunSuite with ShouldMatchers {
  
  test("should create entity with given version") {
    TestBaseEntityWithVersionSupport(1, Some(1)).version should be(1)
  }

  test("should not create entity with negative version") {
    intercept[IllegalArgumentException] {
      TestBaseEntityWithVersionSupport(-1, None)
    }
  }
}

private case class TestBaseEntityWithVersionSupport(version: Long, id: Option[Int]) extends BaseEntity[Int] with VersionSupport
