package com.github.sandrasi.moviecatalog.domain.entities.base

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class VersionSupportTest extends FunSuite with ShouldMatchers {
  
  test("should create entity with given version") {
    val subject = new TestBaseEntityWithVersionSupport(1, 1)
    subject.version should be(1)
  }

  test("should not create entity with negative version") {
    intercept[IllegalArgumentException] {
      new TestBaseEntityWithVersionSupport(-1, 0)
    }
  }
}

private class TestBaseEntityWithVersionSupport(val version: Long, id: Int) extends BaseEntity[Int](Some(id)) with VersionSupport
