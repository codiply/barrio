package com.codiply.barrio.neighbors

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.actor.Props

import com.codiply.barrio.generic.AggregatorActor
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.neighbors.ActorProtocol._

object NeighborAggregatorActor {
  def props(
      coordinates: Coordinates,
      kNeighbors: Int,
      metric: Metric,
      responseRecipient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration): Props = {
        val initialValue = Nil
        val folder = (neighbors: List[Point], response: GetNeighborsResponse) =>
          (neighbors ++ response.neighbors).groupBy(_.id).map(_._2.head).toList.sortBy(p =>
            metric.easyDistance(coordinates, p.location).value).take(kNeighbors)
        val mapper = (neighbors: List[Point]) => GetNeighborsResponse(neighbors)
        Props(new AggregatorActor(responseRecipient, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      }
}
