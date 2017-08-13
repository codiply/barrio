package com.codiply.barrio.neighbors.aggregators

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.actor.Props
import com.codiply.barrio.generic.AggregatorActor

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.ClusterStats
import com.codiply.barrio.neighbors.NodeStats

object NodeStatsAggregatorActor {
  def props(
      responseRecipient: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration): Props = {
        val initialValue = Map.empty[String, NodeStats]
        val folder = (allNodeStats: Map[String, NodeStats], response: GetNodeStatsResponse) =>
          allNodeStats + (response.nodeName -> response.stats)
        val mapper = (allNodeStats: Map[String, NodeStats]) => GetClusterStatsResponse(ClusterStats(allNodeStats))
        AggregatorActor.props(responseRecipient, initialValue, folder, mapper, None, expectedNumberOfResponses, timeout)
      }
}
