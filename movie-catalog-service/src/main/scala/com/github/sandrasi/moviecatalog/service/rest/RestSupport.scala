package com.github.sandrasi.moviecatalog.service.rest

import net.liftweb.json.Extraction.decompose
import net.liftweb.json.Xml.toXml
import net.liftweb.json.{NoTypeHints, Serialization}
import org.fusesource.scalate.Template
import org.scalatra._
import org.scalatra.scalate.ScalateSupport

case class RestResponse[+A](resource: RestSupport#RestResource[A], result: Result[A])

case class JsonResponse[+A](response: Result[A])

case class XmlResponse[+A](response: Result[A])

class BadRequestException(message: String) extends RuntimeException(message)

case class ParameterException(name: String, errorMessage: String) extends BadRequestException(name + ": " + errorMessage)

trait RestSupport extends ScalateSupport with UrlSupport with ApiFormats { outer =>

  private implicit val serializationFormat = Serialization.formats(NoTypeHints)

  override def renderPipeline: RenderPipeline = {
    case RestResponse(resource, result) =>
      status(result.status.code)
      format match {
        case "json" => JsonResponse(result)
        case "xml" => XmlResponse(result)
        case _ => templateEngine.layout(resource.description.source.uri, resource.description)
      }
    case json @ JsonResponse(_) => write(json)
    case xml @ XmlResponse(_) => write(xml)
    case any => super.renderPipeline(any)
  }

  private def write(jsonResponse: JsonResponse[_]) = Serialization.write(jsonResponse)

  private def write(xmlResponse: XmlResponse[_]) = toXml(decompose(xmlResponse))

  protected[this] type ParameterConversionResult[A] = Either[Exception, A]

  protected[this] type ParameterConverter[A] = Seq[String] => ParameterConversionResult[A]

  trait Parameter[+A] { outer =>

    def name: String
    def description: Template
    def isRequired: Boolean

    protected def parse: A
  }

  case class RequiredParameter[A](name: String, description: Template)(implicit parameterConverter: ParameterConverter[A]) extends Parameter[A] {

    override val isRequired = true

    override protected def parse: A = {
      val parameterValues = multiParams.get(name)
      if (parameterValues.isDefined) {
        parameterConverter(parameterValues.get).fold(
          error => throw ParameterException(name, error.getMessage),
          value => value
        )
      } else {
        throw ParameterException(name, "Required parameter is missing")
      }
    }
  }

  trait RestResource[+A] {

    def path: String
    def description: Template

    protected def get: Result[A]

    final def url(params: (String, String)*): String = UrlGenerator.url(route, params: _*)

    protected[this] final def template(uri: String) = templateEngine.load(findTemplate(uri).getOrElse(uri))

    private[this] final val route = outer.get(path) {
      val result = try {
        get
      } catch {
        case e: ParameterException => ErrorResult(BadRequest(message = e.getMessage))
        case e => ErrorResult(InternalServerError(message = e.getMessage))
      }
      RestResponse(this, result)
    }
  }
}
