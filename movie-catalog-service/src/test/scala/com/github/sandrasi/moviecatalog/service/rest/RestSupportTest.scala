package com.github.sandrasi.moviecatalog.service.rest

import javax.servlet.http.HttpServletResponse.{SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR, SC_OK}
import org.eclipse.jetty.util.resource.{Resource, ResourceCollection}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatra._
import org.scalatra.test.scalatest.ScalatraFunSuite

@RunWith(classOf[JUnitRunner])
class RestSupportTest extends ScalatraFunSuite with ShouldMatchers {

  servletContextHandler.setBaseResource(new ResourceCollection(Resource.newResource("src/main/webapp"), Resource.newResource("src/test/test-webapp")))
  addFilter(TestRestSupport, "/*")

  test("should parse specified optional parameter") {
    get("/optional-test-string-parameter-resource?optionalTestStringParameter=testValue") {
      status should be(SC_OK)
      body should include(""""content":"testValue"""")
    }
  }

  test("should parse omitted optional parameter") {
    get("/optional-test-string-parameter-resource") {
      status should be(SC_OK)
      body should not include(""""content":""")
    }
  }

  test("should parse specified required parameter") {
    get("/required-test-int-parameter-resource?requiredTestIntParameter=1") {
      status should be(SC_OK)
      body should include(""""content":1""")
    }
  }

  test("should fail to parse omitted required parameter") {
    get("/required-test-int-parameter-resource") {
      status should be(SC_BAD_REQUEST)
      body should include(""""status":{"code":400,"message":"requiredTestIntParameter: Required parameter is missing"}""")
    }
  }

  test("should use default parameter value when parameter is omitted") {
    get("/optional-test-string-parameter-with-default-value-resource") {
      status should be(SC_OK)
      body should include(""""content":"testValue"""")
    }
  }

  test("should accept only allowed parameter values") {
    get("/multi-value-test-string-parameter-resource?multiValueTestStringParameter=invalid") {
      status should be(SC_BAD_REQUEST)
      body should include(""""status":{"code":400,"message":"multiValueTestStringParameter: 'invalid' is not one of ['testValue1', 'testValue2']"}""")
    }
  }

  test("should accept json as value for the format parameter") {
    get("/test-resource?format=json") {
      response.getContentType should equal("application/json;charset=UTF-8")
    }
  }

  test("should accept xml as value for the format parameter") {
    get("/test-resource?format=xml") {
      response.getContentType should equal("application/xml;charset=UTF-8")
    }
  }

  test("should use json as default value for the format parameter") {
    get("/test-resource") {
      response.getContentType should equal("application/json;charset=UTF-8")
    }
  }

  test("should return parameterized url for the resource") {
    TestRestSupport.TestResource.getUrl(("test-parameter", "test-value")) should be("/test-resource?test-parameter=test-value")
  }

  test("should return result and set ok status code") {
    get("/test-resource") {
      status should be(SC_OK)
      body should be("""{"result":{"actionResult":{"status":{"code":200,"message":"OK"},"body":{},"headers":{}},"links":[],"content":"test content"}}""")
    }
  }

  test("should return error result and set bad request status code when the parameter is not parseable") {
    get("/required-test-int-parameter-resource?requiredTestIntParameter=invalid") {
      status should be(SC_BAD_REQUEST)
      body should include(""""status":{"code":400,"message":"requiredTestIntParameter: For input string: \"invalid\""}""")
    }
  }

  test("should return error result and set internal server error status code when something goes wrong on the server side") {
    get("/internal-server-error-resource") {
      status should be(SC_INTERNAL_SERVER_ERROR)
      body should include(""""status":{"code":500,"message":"Test exception"}""")
    }
  }
}

object TestRestSupport extends ScalatraFilter with RestSupport {

  final val OptionalTestStringParameterResource = new RestResource[String] with GetSupport[String] {

    override def path = "/optional-test-string-parameter-resource"
    override def description = "test-resource-description"
    override protected def get: Result[String] = {
      val parameterValue = optionalStringParameter.parse
      ContentResult(content = if (parameterValue.isDefined) Some(optionalStringParameter.parse.get) else None)
    }

    private val optionalStringParameter = parameter(OptionalParameter[String]("optionalTestStringParameter", "test-parameter-description"))
  }

  final val RequiredTestIntParameterResource = new RestResource[Int] with GetSupport[Int] {

    override def path = "/required-test-int-parameter-resource"
    override def description = "test-resource-description"
    override protected def get: Result[Int] = ContentResult(content = Some(requiredIntParameter.parse.get))

    private val requiredIntParameter = parameter(RequiredParameter[Int]("requiredTestIntParameter", "test-parameter-description"))
  }

  final val OptionalTestStringParameterWithDefaultValueResource = new RestResource[String] with GetSupport[String] {

    override def path = "/optional-test-string-parameter-with-default-value-resource"
    override def description = "test-resource-description"
    override protected def get: Result[String] = ContentResult(content = Some(optionalStringParameterWithDefaultValue.parse.get))

    private val optionalStringParameterWithDefaultValue = parameter(OptionalParameter[String]("optionalTestStringParameter", "test-parameter-description").withDefault("testValue"))
  }

  final val MultiValueTestStringParameterResource = new RestResource[String] with GetSupport[String] {

    override def path = "/multi-value-test-string-parameter-resource"
    override def description = "test-resource-description"
    override protected def get: Result[String] = ContentResult(content = Some(multiValueStringParameter.parse.get))

    private val multiValueStringParameter = parameter(OptionalParameter[String]("multiValueTestStringParameter", "test-parameter-description").oneOf("testValue1", "testValue2"))
  }

  final val InternalServerErrorResource = new RestResource[Nothing] with GetSupport[String] {

    override def path = "/internal-server-error-resource"
    override def description = "test-resource-description"
    override protected def get: Result[Nothing] = throw new RuntimeException("Test exception")
  }

  final val TestResource  = new RestResource[String] with GetSupport[String] {

    override def path = "/test-resource"
    override def description = "test-resource-description"
    override protected def get: Result[String] = ContentResult[String](content = Some("test content"))
  }
}
