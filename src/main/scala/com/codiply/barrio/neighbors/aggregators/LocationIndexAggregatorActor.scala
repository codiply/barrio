package com.codiply.barrio.neighbors.aggregators

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.actor.Props

import com.codiply.barrio.generic.AggregatorActor
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.neighbors.LocationIndexActorProtocol.GetLocationResponse

object LocationIndexAggregatorActor {
  def props(
      responseRecipient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration): Props = {
        val initialValue = None
        val folder = (aggregate: Option[Coordinates], newResponse: GetLocationResponse) =>
          if (aggregate.isDefined) {
            aggregate
          } else {
            newResponse.location
          }
        val mapper = (aggregate: Option[Coordinates]) => GetLocationResponse(aggregate)
        val earlyTerminationCondition = Some((aggregate: Option[Coordinates]) => aggregate.isDefined)
        AggregatorActor.props(
            responseRecipient, initialValue, folder, mapper, earlyTerminationCondition, expectedNumberOfResponses, timeout)
      }
}
