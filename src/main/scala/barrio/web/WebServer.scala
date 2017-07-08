package com.codiply.barrio.web

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import com.codiply.barrio.nn.Neighborhood

class WebServer(neighborhood: Neighborhood) extends HttpApp with JsonSupport {  
  override def routes: Route =
    path("") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello from Barrio!</h1>"))
      }
    } ~ 
    path("neighbors") {
      post {
        decodeRequest {
          entity(as[NeighborsQuery]) { q => {
              val neighbors = neighborhood.getNeighbors(q.coordinates, q.k)
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, neighbors.toString()))
            }
          }        
        }
      }
    }
}