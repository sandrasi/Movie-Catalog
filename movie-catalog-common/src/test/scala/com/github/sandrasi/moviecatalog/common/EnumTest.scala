package com.github.sandrasi.moviecatalog.common

import com.github.sandrasi.moviecatalog.common.TestEnum._
import org.scalatest.{Matchers, FunSuite}

class EnumTest extends FunSuite with Matchers {

  test("should list all enum values") {
    TestEnum.values should be(List(Value1, Value2))
  }

  test("should create enum values from string matching enum names") {
    TestEnum.valueOf("Value1") should be(Value1)
    TestEnum.valueOf("Value2") should be(Value2)
  }

  test("should not create enum value from string not matching any of the enum names") {
    intercept[NoSuchElementException] { TestEnum.valueOf("does not match") }
  }
}

private sealed trait TestEnum extends TestEnum.Value

private case object TestEnum extends Enum[TestEnum] {

  case object Value1 extends TestEnum
  case object Value2 extends TestEnum

  Value1; Value2
}
