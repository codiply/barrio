package com.codiply.barrio.web

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import com.codiply.barrio.nn.Point
  
final case class NeighborsRequest(k: Int, coordinates: List[Double])
final case class Neighbor(id: String, coordinates: List[Double])
final case class NeighborsResponse(neighbors: List[Neighbor])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val neighborsRequestFormat = jsonFormat2(NeighborsRequest)
  implicit val pointFormat = jsonFormat2(Neighbor)
  implicit val neighborsResponseFormat = jsonFormat1(NeighborsResponse)
}
