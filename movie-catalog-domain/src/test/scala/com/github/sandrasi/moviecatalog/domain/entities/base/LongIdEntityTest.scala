package com.github.sandrasi.moviecatalog.domain.entities.base

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class LongIdEntityTest extends FunSuite with ShouldMatchers {

  test("should create entity with no id") {
    TestLongIdEntity(0).id should be(None)
  }

  test("should create entity with the given id") {
    TestLongIdEntity(1).id should be(Some(1))
  }

  test("should not create entity with negative id") {
    intercept[IllegalArgumentException] {
      TestLongIdEntity(-1)
    }
  }
}

private object TestLongIdEntity {

  def apply(id: Long) = new LongIdEntity(id) {}
}
