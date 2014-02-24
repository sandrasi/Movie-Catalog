package com.github.sandrasi.moviecatalog.common

import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidateTest extends FunSuite with Matchers {

  test("should throw an exception if the expression is false with a default message") {
    val ex = intercept[IllegalArgumentException] {
      Validate.isTrue(false)
    }

    ex.getMessage should be("requirement failed")
  }

  test("should throw an exception if the expression is false with the specified message") {
    val ex = intercept[IllegalArgumentException] {
      Validate.isTrue(false, "test message")
    }

    ex.getMessage should be("requirement failed: test message")
  }

  test("should throw an exception if the predicate is false for an element of the collection") {
    intercept[IllegalArgumentException] {
      Validate.isTrueForAll(Array(true, false), (elem: Boolean) => elem)
    }
  }

  test("should throw an exception if the object is null") {
    intercept[IllegalArgumentException] {
      Validate.notNull(null)
    }
  }

  test("should throw an exception if the string is null") {
    intercept[IllegalArgumentException] {
      Validate.notBlank(null)
    }
  }

  test("should throw an exception if the string is empty") {
    intercept[IllegalArgumentException] {
      Validate.notBlank("")
    }
  }

  test("should throw an exception if the string is blank") {
    intercept[IllegalArgumentException] {
      Validate.notBlank(" \r\n\t")
    }
  }

  test("should throw an exception if the collection is null") {
    intercept[IllegalArgumentException] {
      Validate.noNullElements(null)
    }
  }

  test("should throw an exception if the collection contains a null element") {
    intercept[IllegalArgumentException] {
      Validate.noNullElements(Array("one", "two", null, "four"))
    }
  }

  test("should throw an exception if the string collection is null") {
    intercept[IllegalArgumentException] {
      Validate.noBlankElements(null)
    }
  }

  test("should throw an exception if the string collection contains a null element") {
    intercept[IllegalArgumentException] {
      Validate.noBlankElements(Array("one", "two", null, "four"))
    }
  }

  test("should throw an exception if the string collection contains an empty element") {
    intercept[IllegalArgumentException] {
      Validate.noBlankElements(Array("one", "two", "", "four"))
    }
  }

  test("should throw an exception if the string collection contains a blank element") {
    intercept[IllegalArgumentException] {
      Validate.noBlankElements(Array("one", "two", " \r\n\t", "four"))
    }
  }
}
