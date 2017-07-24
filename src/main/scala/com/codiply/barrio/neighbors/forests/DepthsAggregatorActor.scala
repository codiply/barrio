package com.codiply.barrio.neighbors.forests

import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.actor.Props
import com.codiply.barrio.generics.AggregatorActor
import NeighborhoodTreeActorProtocol._

object DepthsAggregatorActor {
  def props(
      responseRecipient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration) = {
        val initialValue = Nil
        val folder = (depths: List[Int], response: GetDepthsResponse) => depths ++ response.depths
        val mapper = (depths: List[Int]) => GetDepthsResponse(depths.map(_ + 1))
        Props(new AggregatorActor(responseRecipient, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      }
}