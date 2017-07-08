package com.codiply.barrio.web

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

final case class NeighborsQuery(k: Int, coordinates: List[Double])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val neighborQueryFormat = jsonFormat2(NeighborsQuery)
}