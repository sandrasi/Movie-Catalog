package com.github.sandrasi.moviecatalog.common

import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidateTest extends FunSuite with Matchers {

  test("should throw an exception if the expression is false with a default message") {
    intercept[IllegalArgumentException] { Validate.valid(false) }.getMessage should be("requirement failed")
  }

  test("should throw an exception if the expression is false with the specified message") {
    intercept[IllegalArgumentException] { Validate.valid(false, "test message") }.getMessage should be("requirement failed: test message")
  }

  test("should throw an exception if the predicate is false for an element of the collection") {
    intercept[IllegalArgumentException] { Validate.validForAll(Array(true, false), (elem: Boolean) => elem) }
  }

  test("should throw an exception if the object is null") {
    intercept[IllegalArgumentException] { Validate.notNull(null) }
  }

  test("should throw an exception if the string is null") {
    intercept[IllegalArgumentException] { Validate.notBlank(null) }
  }

  test("should throw an exception if the string is empty") {
    intercept[IllegalArgumentException] { Validate.notBlank("") }
  }

  test("should throw an exception if the string is blank") {
    intercept[IllegalArgumentException] { Validate.notBlank(" \r\n\t") }
  }

  test("should throw an exception if the collection is null") {
    intercept[IllegalArgumentException] { Validate.noNullElements(null) }
  }

  test("should throw an exception if the collection contains a null element") {
    intercept[IllegalArgumentException] { Validate.noNullElements(Array("one", "two", null, "four")) }
  }

  test("should throw an exception if the string collection is null") {
    intercept[IllegalArgumentException] { Validate.noBlankElements(null) }
  }

  test("should throw an exception if the string collection contains a null element") {
    intercept[IllegalArgumentException] { Validate.noBlankElements(Array("one", "two", null, "four")) }
  }

  test("should throw an exception if the string collection contains an empty element") {
    intercept[IllegalArgumentException] { Validate.noBlankElements(Array("one", "two", "", "four")) }
  }

  test("should throw an exception if the string collection contains a blank element") {
    intercept[IllegalArgumentException] { Validate.noBlankElements(Array("one", "two", " \r\n\t", "four")) }
  }
}
