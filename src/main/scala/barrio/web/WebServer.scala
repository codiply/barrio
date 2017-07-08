package com.codiply.barrio.web

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import com.codiply.barrio.nn.Neighborhood
import spray.json._

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
          entity(as[NeighborsRequest]) { request => {
              val neighbors = neighborhood.getNeighbors(request.coordinates, request.k)
              val response = NeighborsResponse(neighbors.map(p => Neighbor(p.id, p.coordinates))).toJson
              complete(HttpEntity(ContentTypes.`application/json`, response.toString))
            }
          }        
        }
      }
    }
}