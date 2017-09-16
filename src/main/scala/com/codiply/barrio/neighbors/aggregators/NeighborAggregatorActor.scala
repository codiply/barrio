package com.codiply.barrio.neighbors.aggregators

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.actor.Props

import com.codiply.barrio.generic.AggregatorActor
import com.codiply.barrio.generic.AggregatorMapperContext
import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.forests.NeighborhoodForestSearchActorProtocol.NeighborsForestSearchResponse
import com.codiply.barrio.neighbors.NearestNeighborsContainer

object NeighborAggregatorActor {
  def props(
      kNeighbors: Int,
      responseRecipient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration,
      distanceThreshold: EasyDistance): Props = {
        val initialValue = NeighborsForestSearchResponse(false, NearestNeighborsContainer.empty(kNeighbors))
        val folder = (aggregateResponse: NeighborsForestSearchResponse, newResponse: NeighborsForestSearchResponse) =>
          NeighborsForestSearchResponse(
              aggregateResponse.timeoutReached || newResponse.timeoutReached,
              aggregateResponse.neighborsContainer.merge(newResponse.neighborsContainer))
        val mapper = (aggregateResponse: NeighborsForestSearchResponse, mapperContext: AggregatorMapperContext) =>
          GetNeighborsResponse(
              timeoutReached = aggregateResponse.timeoutReached || mapperContext.timeoutReached,
              distanceThreshold = distanceThreshold,
              aggregateResponse.neighborsContainer.orderedDistinctNeighbors)
        AggregatorActor.props(responseRecipient, initialValue, folder, mapper, None, expectedNumberOfResponses, timeout)
      }
}
