package com.github.sandrasi.moviecatalog.domain.utility

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.github.sandrasi.moviecatalog.domain.utility.Gender._

class GenderTest extends FunSuite with Matchers {

  test("should list all gender values") {
    Gender.values should be(List(Male, Female))
  }

  test("should create gender values from string matching enum names") {
    Gender.valueOf("Male") should be(Male)
    Gender.valueOf("Female") should be(Female)
  }

  test("should not create gender value from string not matching any of the enums' name") {
    intercept[NoSuchElementException] {
      Gender.valueOf("does not match")
    }
  }
}
