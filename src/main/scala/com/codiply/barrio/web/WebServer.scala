package com.codiply.barrio.web

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import com.codiply.barrio.neighbors.NeighborProvider
import com.codiply.barrio.neighbors.ClusterStats
import com.codiply.barrio.neighbors.NodeStats
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
          val response = ClusterStatsJson(stats.nodeStats.map { s: NodeStats => NodeStatsJson(
              freeMemoryMB = s.freeMemoryMB,
              totalMemoryMB = s.totalMemoryMB,
              maxMemoryMB = s.maxMemoryMB,
              usedMemoryMB = s.usedMemoryMB,
              treeStats = s.treeStats.map { x => TreeStatsJson(minDepth = x.minDepth, maxDepth = x.maxDepth) }) } ).toJson
          complete(HttpEntity(ContentTypes.`application/json`, response.toString))
        }
      }
    } ~ 
    path("neighbors") {
      post {
        decodeRequest {
          entity(as[NeighborsRequest]) { request => {
              onSuccess(neighborhood.getNeighbors(request.coordinates, request.k)) { neighbors =>
                val response = NeighborsResponse(neighbors.map(p => Neighbor(p.id, p.coordinates))).toJson
                complete(HttpEntity(ContentTypes.`application/json`, response.toString))
              }
            }
          }        
        }
      }
    }
}