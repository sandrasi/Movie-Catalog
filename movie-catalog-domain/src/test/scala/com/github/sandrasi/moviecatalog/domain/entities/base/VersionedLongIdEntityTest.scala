package com.github.sandrasi.moviecatalog.domain.entities.base

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class VersionedLongIdEntityTest extends FunSuite with ShouldMatchers {

  test("should create entity with no id") {
    TestVersionedLongIdEntity(0, 0).id should be(None)
  }

  test("should create entity with the given id") {
    TestVersionedLongIdEntity(0, 1).id should be(Some(1))
  }

  test("should convert to string") {
    TestVersionedLongIdEntity(0, 1).toString should be("anon$1(id: Some(1), version: 0)")
  }
}

private object TestVersionedLongIdEntity {

  def apply(version: Long, id: Long) = new VersionedLongIdEntity(version, id) {}
}
