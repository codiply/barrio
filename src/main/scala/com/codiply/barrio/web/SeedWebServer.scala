package com.codiply.barrio.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{HttpApp, Route}

class SeedWebServer() extends HttpApp {

  override def routes: Route =
    path("") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>This is a Barrio seed node!</h1>"))
      }
    }
}
