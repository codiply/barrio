package com.codiply.barrio.neighbors.caching

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorLogging
import akka.actor.Props

import com.codiply.barrio.neighbors.ActorProtocol.GetNeighborsResponse
import com.codiply.barrio.neighbors.caching.Types.NeighborsCache

object NeighborsCacheReaderActor {
  def props(cache: NeighborsCache): Props =
    Props(new NeighborsCacheReaderActor(cache))
}

class NeighborsCacheReaderActor(
    cache: NeighborsCache) extends Actor with ActorLogging {
  import com.codiply.barrio.neighbors.caching.NeighborsCacheActorProtocol.GetCachedNeighborsRequest
  import com.codiply.barrio.neighbors.caching.NeighborsCacheActorProtocol.GetCachedNeighborsResponse

  def receive: Receive = {
    case GetCachedNeighborsRequest(locationId, k) => {
      cache.getIfPresent(locationId) match {
        case Some(cachedResponse) => {
          (cachedResponse.neighbors.length, cachedResponse.distanceThreshold) match {
            case (_, thresholdCached) if thresholdCached < cachedResponse.distanceThreshold =>
              sender ! GetCachedNeighborsResponse(None)
              log.info(s"Cannot reuse cached neighbors for location ${locationId} because the distance threshold is lower.")
            case (kCached, _) if kCached >= k =>
              val response = if (kCached == k) {
                cachedResponse
              } else {
                cachedResponse.copy(neighbors = cachedResponse.neighbors.take(k))
              }
              sender ! GetCachedNeighborsResponse(Some(response))
              log.info(s"Cached response found for location ${locationId}.")
            case _ =>
              sender ! GetCachedNeighborsResponse(None)
              log.info(s"Cached response without enough neighbours found for location ${locationId}.")
          }
        }
        case None =>
          sender ! GetCachedNeighborsResponse(None)
          log.info(s"Cached response not found for location ${locationId}.")
      }
    }
  }
}
