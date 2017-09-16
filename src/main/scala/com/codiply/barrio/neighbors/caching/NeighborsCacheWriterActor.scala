package com.codiply.barrio.neighbors.caching

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorLogging
import akka.actor.Props

import com.codiply.barrio.helpers.CacheWrapper
import com.codiply.barrio.neighbors.ActorProtocol.GetNeighborsResponse
import com.codiply.barrio.neighbors.caching.Types.NeighborsCache

object NeighborsCacheWriterActor {
  def props(cache: NeighborsCache): Props =
    Props(new NeighborsCacheWriterActor(cache))
}

class NeighborsCacheWriterActor(
    cache: NeighborsCache) extends Actor with ActorLogging {
  import com.codiply.barrio.neighbors.caching.NeighborsCacheActorProtocol.CacheNeighborsRequest

  def receive: Receive = {
    case msg: CacheNeighborsRequest if msg.response.timeoutReached =>
      log.warning(s"Attempted to cache a response that had timed out.")
    case msg: CacheNeighborsRequest if msg.response.neighbors.length == 0 =>
      log.warning(s"Attempted to cache a response with no neighbors")
    case msg: CacheNeighborsRequest =>
      cache.put(msg.locationId, msg.response)
      log.info(s"Caching response for location ${msg.locationId}.")
  }
}
