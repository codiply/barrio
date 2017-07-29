package com.codiply.barrio.neighbors

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.actor.Props
import com.codiply.barrio.generic.AggregatorActor

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.ActorProtocol._

object NodeStatsAggregatorActor {
  def props(
      responseRecipient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration): Props = {
        val initialValue = Nil
        val folder = (allNodeStats: List[NodeStats], response: GetNodeStatsResponse) => response.stats :: allNodeStats
        val mapper = (allNodeStats: List[NodeStats]) => GetClusterStatsResponse(ClusterStats(allNodeStats))
        Props(new AggregatorActor(responseRecipient, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      }
}
