package com.github.sandrasi.moviecatalog.service.rest

import scala.collection.mutable.{Map => MutableMap}
import org.fusesource.scalate.Template
import com.github.sandrasi.moviecatalog.repository.Repository
import com.github.sandrasi.moviecatalog.service.dtos.DtoSupport._
import com.github.sandrasi.moviecatalog.service.dtos.MotionPictureDto
import com.github.sandrasi.moviecatalog.domain.Movie

trait MovieCatalogResourceSupport extends RestSupport { outer =>

  protected def movieCatalogRepository: Repository

  private final val Resources = MutableMap[String, RestResource[_] with GetSupport[_]]()

  private final val IndexResource = get[Nothing]("/", "index-resource") {
    Result.empty(Link(rel = "movies", href = MoviesResource.getUrl()))
  }

  private final val MoviesResource = get[MotionPictureDto]("/movies", "movies-resource") {
    QueryResult(
      pageNumber = 1,
      pageSize = 0,
      pageCount = 1,
      startIndex = 1,
      totalSize = 0,
      results = movieCatalogRepository.query(classOf[Movie]).map(toMotionPictureDto(_)).toSeq
    )
  }

  private def get[A](rscPath: String, rscDesc: String)(rscGet: => Result[A]): RestResource[A] with GetSupport[A] = {
    val resource = new RestResource[A] with GetSupport[A] {
      override def path: String = rscPath
      override def description: Template = rscDesc
      override protected def get = rscGet
    }

    Resources += rscPath -> resource
    resource
  }

  get("/explorer") {
    redirect("/explorer/")
  }

  get("/explorer/*") {
    val path = "/" + params("splat")
    if (Resources.contains(path)) describeResource(Resources(path)) else renderResponse(doNotFound())
  }
}
