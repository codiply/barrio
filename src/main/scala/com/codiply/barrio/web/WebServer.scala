package com.codiply.barrio.web

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import spray.json._

import com.codiply.barrio.geometry.RealDistance
import com.codiply.barrio.neighbors.ClusterHealth
import com.codiply.barrio.neighbors.ClusterStats
import com.codiply.barrio.neighbors.NeighborProvider
import com.codiply.barrio.neighbors.NodeStats
import com.codiply.barrio.web.JsonSupport._

class WebServer(neighborhood: NeighborProvider) extends HttpApp with JsonSupport {
  override def routes: Route =
    path("") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello from Barrio!</h1>"))
      }
    } ~
    path("health") {
      get {
        onSuccess(neighborhood.getHealth()) { case health: ClusterHealth =>
          val response = Mapping.mapClusterHealth(health).toJson
          complete(HttpEntity(ContentTypes.`application/json`, response.toString))
        }
      }
    } ~
    path("neighbors") {
      post {
        decodeRequest {
          entity(as[NeighborsRequestJson]) { request =>
            onSuccess(neighborhood.getNeighbors(
                request.location, request.k, RealDistance(request.distanceThreshold), request.timeout)) { neighbors =>
              val response = NeighborsResponseJson(
                  count = neighbors.length,
                  neighbors = neighbors.map(p => NeighborJson(p.id, p.location))).toJson
              complete(HttpEntity(ContentTypes.`application/json`, response.toString))
            }
          }
        }
      }
    } ~
    path("stats") {
      get {
        parameters('gc.as[Boolean] ? false) { (doGarbageCollect) =>
          onSuccess(neighborhood.getStats(doGarbageCollect)) { case stats: ClusterStats =>
            val response = Mapping.mapClusterStats(stats).toJson
            complete(HttpEntity(ContentTypes.`application/json`, response.toString))
          }
        }
      }
    }
}
