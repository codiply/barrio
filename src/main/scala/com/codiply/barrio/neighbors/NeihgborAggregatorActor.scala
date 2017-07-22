package com.codiply.barrio.neighbors

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.actor.Props
import com.codiply.barrio.generics.AggregatorActor
import NeighborhoodPatchActorProtocol._
import Point._

object NeighborAggregatorActor {
  def props(
      coordinates: Coordinates,
      kNeighbors: Int,
      distance: DistanceMetric,
      responseRecepient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration) = {
        val initialValue = Nil
        val folder = (neighbors: List[Point], response: GetNeighborsResponse) => 
          (neighbors ++ response.neighbors).groupBy(_.id).map(_._2.head).toList.sortBy(p => 
            distance(coordinates, p.coordinates)).take(kNeighbors)
        val mapper = (neighbors: List[Point]) => GetNeighborsResponse(neighbors)
        Props(new AggregatorActor(responseRecepient, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      }
}