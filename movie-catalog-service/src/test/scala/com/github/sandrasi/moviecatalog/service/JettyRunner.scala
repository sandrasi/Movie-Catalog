package com.github.sandrasi.moviecatalog.service

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object JettyRunner {

  private final val Server = new Server(8080)

  def main(args: Array[String]) {
    startJetty()
  }

  private def startJetty() {
    val webAppContext = new WebAppContext()
    webAppContext.setResourceBase("src/main/webapp")
    webAppContext.setContextPath("/")
    Server.setHandler(webAppContext)
    Server.setStopAtShutdown(true)
    Server.start()
  }
}
