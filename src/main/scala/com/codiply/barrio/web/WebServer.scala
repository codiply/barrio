package com.codiply.barrio.web

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import com.codiply.barrio.neighbors.NeighborProvider
import com.codiply.barrio.neighbors.ClusterStats
import com.codiply.barrio.neighbors.NodeStats
import JsonSupport._
import spray.json._

class WebServer(neighborhood: NeighborProvider) extends HttpApp with JsonSupport {
  override def routes: Route =
    path("") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello from Barrio!</h1>"))
      }
    } ~
    path("stats") {
      get {
        onSuccess(neighborhood.getStats()) { case stats: ClusterStats =>
          val response = Mapping.mapClusterStats(stats).toJson
          complete(HttpEntity(ContentTypes.`application/json`, response.toString))
        }
      }
    } ~
    path("neighbors") {
      post {
        decodeRequest {
          entity(as[NeighborsRequestJson]) { request =>
            onSuccess(neighborhood.getNeighbors(request.coordinates, request.k, request.distanceThreshold)) { neighbors =>
              val response = NeighborsResponseJson(neighbors.map(p => NeighborJson(p.id, p.coordinates))).toJson
              complete(HttpEntity(ContentTypes.`application/json`, response.toString))
            }
          }
        }
      }
    }
}
