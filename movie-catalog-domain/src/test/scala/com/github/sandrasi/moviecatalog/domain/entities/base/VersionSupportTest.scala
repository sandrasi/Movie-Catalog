package com.github.sandrasi.moviecatalog.domain.entities.base

import org.scalatest.FunSuite

class VersionSupportTest extends FunSuite {
  
  test("should not create entity with negative version") {
    intercept[IllegalArgumentException] {
      new TestBaseEntityWithVersionSupport(-1, 0)
    }
  }
}

private class TestBaseEntityWithVersionSupport(val version: Long, id: Int) extends BaseEntity[Int](Some(id)) with VersionSupport
