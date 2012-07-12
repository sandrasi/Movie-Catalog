package com.github.sandrasi.moviecatalog.service.rest

import com.github.sandrasi.moviecatalog.repository.Repository
import com.github.sandrasi.moviecatalog.domain.entities.core.Movie
import com.github.sandrasi.moviecatalog.service.dtos.DtoSupport._
import com.github.sandrasi.moviecatalog.service.dtos.{MotionPictureDto, MovieDto}

trait MovieCatalogResourceSupport extends RestSupport { outer =>

  protected def movieCatalogRepository: Repository

  private final val IndexResource = new RestResource[Nothing] {

    override def path = "/"
    override def description = template("index-resource")

    override protected def get: Result[Nothing] = Result.empty(Link(rel = "movies", href = MoviesResource.url()))
  }

  private final val MoviesResource = new RestResource[MotionPictureDto] {

    override def path = "/movies"
    override def description = template("movies-resource")

    override protected def get = QueryResult(
      pageNumber = 1,
      pageSize = 0,
      pageCount = 1,
      startIndex = 1,
      totalSize = 0,
      results = movieCatalogRepository.query(classOf[Movie]).map(toMotionPictureDto(_)).toSeq
    )
  }
}
