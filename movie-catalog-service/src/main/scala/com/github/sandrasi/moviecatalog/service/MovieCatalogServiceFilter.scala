package com.github.sandrasi.moviecatalog.service

import org.scalatra.ScalatraFilter
import org.neo4j.kernel.EmbeddedGraphDatabase
import com.github.sandrasi.moviecatalog.repository.neo4j.Neo4jRepository
import com.github.sandrasi.moviecatalog.service.rest.MovieCatalogResourceSupport

class MovieCatalogServiceFilter extends ScalatraFilter with MovieCatalogResourceSupport {

  override protected val movieCatalogRepository = new Neo4jRepository(new EmbeddedGraphDatabase("foo.db"))

  override protected def contextPath = servletContext.getContextPath
}
