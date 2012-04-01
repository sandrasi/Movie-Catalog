package com.github.sandrasi.moviecatalog.service.rest

import net.liftweb.json.Extraction.decompose
import net.liftweb.json.Serialization.{write}
import net.liftweb.json.Xml.toXml
import org.scalatra.scalate.ScalateSupport
import org.fusesource.scalate.Template
import org.scalatra.{ApiFormats, RenderPipeline, UrlGenerator, UrlSupport}

sealed trait Result[+A] {

  def status: Int
  def message: String
}

case class QueryResponse[+A](resource: RestSupport#Resource[A], result: Result[A])

case class JsonResponse[+A](response: Result[A])

case class XmlResponse[+A](response: Result[A])

trait RestSupport extends ScalateSupport with UrlSupport with ApiFormats { outer =>

  trait Resource[+A] {

    def path: String
    def htmlDescription: Template

    def get: Result[A]

    final def url(params: (String, String)*): String = UrlGenerator.url(route, params: _*)

    private[this] final val route = outer.get(path) {
      val result = QueryResponse(this, result)
    }
  }

  override def renderPipeline: RenderPipeline = {
    case QueryResponse(resource, result) =>
    case json @ JsonResponse(_) => write(json)
    case xml @ XmlResponse(_) => toXml(decompose(xml))
    case any => super.renderPipeline(any)
  }
}
