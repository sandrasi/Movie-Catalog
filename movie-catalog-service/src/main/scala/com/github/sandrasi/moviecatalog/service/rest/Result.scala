package com.github.sandrasi.moviecatalog.service.rest

sealed trait HttpStatus {

  def code: Int
  def message: String
}

case class Ok(code: Int = 200, message: String = "OK") extends HttpStatus

case class InternalServerError(code: Int = 500, message: String = "Internal server error") extends HttpStatus

case class Link(rel: String, href: String)

sealed trait Result[+A] {

  def status: HttpStatus
}

object Result {

  def empty(links: Link*) = ContentResult[Nothing](links = links, content = None)
}

case class ContentResult[+A](
  status: HttpStatus = Ok(),
  links: Seq[Link],
  content: Option[A]
) extends Result[A]

case class QueryResult[+A](
  status: HttpStatus = Ok(),
  pageNumber: Int,
  pageSize: Int,
  pageCount: Int,
  startIndex: Int,
  totalSize: Int,
  links: Seq[Link],
  results: Seq[A]) extends Result[A]

case class ErrorResult(status: HttpStatus) extends Result[Nothing]
