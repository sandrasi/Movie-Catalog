package com.github.sandrasi.moviecatalog.repository

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class RepositoryConfigurationTest extends FunSuite with BeforeAndAfterEach with Matchers {

  private var subject: TestRepositoryFactory.RepositoryConfiguration = _

  override def beforeEach {
    subject = new TestRepositoryFactory.RepositoryConfiguration
  }

  test("should put and get paramter to and from configuration") {
    subject.set("test parameter", "test value")
    subject.get("test parameter", classOf[String]) should be("test value")
  }

  test("should not get value if the expected type does not match the actual type") {
    intercept[ClassCastException] {
      subject.set("test parameter", 0)
      subject.get("test parameter", classOf[String]) should be("test value")
    }
  }

  test("should create configuration parameter by converting strings") {
    val strToInt = (str: Seq[String]) => try { Right(str.head.toInt) } catch { case e: NumberFormatException => Left(e) }
    subject.setFromString("test parameter", "10")(strToInt)
    subject.get("test parameter", classOf[Int]) should be(10)
  }

  test("should not create configuration parameter from strings if the conversion fails") {
    val exception = intercept[IllegalArgumentException] {
      val strToInt = (str: Seq[String]) => try { Right(str.head.toInt) } catch { case e: NumberFormatException => Left(e) }
      subject.setFromString("test parameter", "not a number")(strToInt)
    }
    exception.getCause.getClass should be(classOf[NumberFormatException])
  }
}

private object TestRepositoryFactory extends RepositoryFactory {

  def apply(repositoryConfiguration: RepositoryConfiguration) = null

  def configurationMetaData = null
}
