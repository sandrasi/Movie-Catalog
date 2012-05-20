package com.github.sandrasi.moviecatalog.service.rest

import scala.collection.mutable.ArrayBuffer
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

trait RestSupport extends ScalateSupport with ApiFormats { outer =>

  private implicit val serializationFormat = Serialization.formats(NoTypeHints)

  override def renderPipeline: RenderPipeline = {
    case response @ RestResponse(resource, result) =>
      status = result.actionResult.status.code
      format match {
        case "json" => JsonResponse(result)
        case "xml" => XmlResponse(result)
        case _ => layoutTemplate(loadTemplate("resource"), Map("restResponse" -> response))
      }
    case json @ JsonResponse(_) => write(json)
    case xml @ XmlResponse(_) => write(xml)
    case any => super.renderPipeline(any)
  }

  private def write(jsonResponse: JsonResponse[_]) = Serialization.write(jsonResponse)

  private def write(xmlResponse: XmlResponse[_]) = toXml(decompose(xmlResponse))

  private def loadTemplate(template: String) = templateEngine.load(findTemplate(template).getOrElse(template))

  private def layoutTemplate(template: Template, attributes: Map[String, Any] = Map()) = templateEngine.layout(template.source.uri, template, attributes)

  protected[this] type ParameterConversionResult[A] = Either[Exception, A]

  protected[this] type ParameterConverter[A] = Seq[String] => ParameterConversionResult[A]

  trait Parameter[A] { outer =>

    def name: String
    def isRequired: Boolean
    def description: Template

    def parse(implicit parameterConverter: ParameterConverter[A]): Option[A] = {
      val parameterValues = multiParams.get(name)
      if (parameterValues.isDefined) {
        parameterConverter(parameterValues.get).fold(
          error => throw ParameterException(name, error.getMessage),
          value => Some(value)
        )
      } else {
        None
      }
    }

    def oneOf(values: A*): Parameter[A] = new Parameter[A] {
      override val name = outer.name
      override val isRequired = outer.isRequired
      override def description = outer.description // Use a new template here that adds the possible values to the parameter description
      override def parse(implicit parameterConverter: ParameterConverter[A]): Option[A] = outer.parse match {
        case validResult @ Some(_) if values.contains(validResult.get) => validResult
        case None => None
        case invalidResult @ Some(_) => throw new ParameterException(name, "'%s' is not one of %s".format(invalidResult.get, values.mkString(", ")))
      }
    }

    def withDefault(default: A): Parameter[A] = new Parameter[A] {
      override val name = outer.name
      override val isRequired = outer.isRequired
      override def description = outer.description // Use a new template here that adds the default value to the parameter description
      override def parse(implicit parameterConverter: ParameterConverter[A]): Option[A] = Some(outer.parse.getOrElse(default))
    }
  }

  abstract class DescribedParameter[A](descriptionTemplate: String) extends Parameter[A] {

    override def description: Template = loadTemplate(descriptionTemplate)
  }

  case class RequiredParameter[A](name: String, descriptionTemplate: String)(implicit parameterConverter: ParameterConverter[A]) extends DescribedParameter[A](descriptionTemplate) {

    override val isRequired: Boolean = true

    override def parse(implicit parameterConverter: ParameterConverter[A]): Option[A] = super.parse match {
      case parsedParameter @ Some(_) => parsedParameter
      case _ => throw ParameterException(name, "Required parameter is missing")
    }
  }

  case class OptionalParameter[A](name: String, descriptionTemplate: String)(implicit parameterConverter: ParameterConverter[A]) extends DescribedParameter[A](descriptionTemplate) { outer =>

    override val isRequired: Boolean = false
  }

  trait RestResource[+A] {

    protected implicit def stringParameterConverter(values: Seq[String]): ParameterConversionResult[String] = Right(values.head)
    protected implicit def intParameterConverter(values: Seq[String]): ParameterConversionResult[Int] = try {
      Right(values.head.trim.toInt)
    } catch {
      case e: NumberFormatException => Left(e)
    }


    def path: String
    def description: Template

    final def parameters: Map[String, Parameter[_]] = params.view.map(p => p.name -> p).toMap
    private[this] val params = ArrayBuffer[Parameter[_]]()

    protected def parameter(p: Parameter[_]) = params += p

    protected def get: Result[A]

    final def url(params: (String, String)*): String = UrlGenerator.url(route, params: _*)

    protected[this] final def template(uri: String) = loadTemplate(uri)

    private[this] final val route = outer.get(path) {
      val result = try {
        get
      } catch {
        case e: ParameterException => ErrorResult(BadRequest(reason = e.getMessage))
        case e => ErrorResult(InternalServerError(reason = e.getMessage))
      }
      RestResponse(this, result)
    }

    parameter(OptionalParameter[String]("format", "parameter-format").withDefault("json").oneOf("json", "xml", "html"))
  }
}
