package com.github.sandrasi.moviecatalog.service.rest

import org.scalatra.{ActionResult, Ok}

case class Link(rel: String, href: String)

sealed trait Result[+A] {

  def actionResult: ActionResult
}

object Result {

  def empty(links: Link*) = ContentResult[Nothing](links = links, content = None)
}

case class ContentResult[+A](
  actionResult: ActionResult = Ok(),
  links: Seq[Link] = Seq.empty,
  content: Option[A]
) extends Result[A]

case class QueryResult[+A](
  actionResult: ActionResult = Ok(),
  pageNumber: Int,
  pageSize: Int,
  pageCount: Int,
  startIndex: Int,
  totalSize: Int,
  links: Seq[Link] = Seq.empty,
  results: Seq[A]) extends Result[A]

case class ErrorResult(actionResult: ActionResult) extends Result[Nothing]
