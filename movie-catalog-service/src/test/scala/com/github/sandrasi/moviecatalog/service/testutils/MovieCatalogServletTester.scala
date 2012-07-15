package com.github.sandrasi.moviecatalog.service.testutils

import org.eclipse.jetty.testing.ServletTester
import org.eclipse.jetty.util.resource.Resource

class MovieCatalogServletTester extends ServletTester {

  def setBaseResource(baseResource: Resource) {
    getContext.setBaseResource(baseResource)
  }
}
