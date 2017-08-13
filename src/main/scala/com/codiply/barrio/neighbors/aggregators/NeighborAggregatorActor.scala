package com.codiply.barrio.neighbors.aggregators

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.actor.Props

import com.codiply.barrio.generic.AggregatorActor
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.NearestNeighborsContainer

object NeighborAggregatorActor {
  def props(
      kNeighbors: Int,
      responseRecipient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration): Props = {
        val initialValue = NearestNeighborsContainer.empty(kNeighbors)
        val folder = (aggregateContainer: NearestNeighborsContainer, newContainer: NearestNeighborsContainer) =>
          aggregateContainer.merge(newContainer)
        val mapper = (aggregateContainer: NearestNeighborsContainer) =>
          GetNeighborsResponse(aggregateContainer.orderedDistinctNeighbors)
        AggregatorActor.props(responseRecipient, initialValue, folder, mapper, None, expectedNumberOfResponses, timeout)
      }
}
