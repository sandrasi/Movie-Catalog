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


  trait RestResource[+A] {

    def path: String
    def description: Template

    protected def get: Result[A]

    final def url(params: (String, String)*): String = UrlGenerator.url(route, params: _*)

    protected[this] final def template(uri: String) = templateEngine.load(findTemplate(uri).getOrElse(uri))

    private[this] final val route = outer.get(path) {
      val result = try { get } catch { case e => ErrorResult(InternalServerError(message = e.getMessage)) }
      RestResponse(this, result)
    }
  }
}
