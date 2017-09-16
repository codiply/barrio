package com.codiply.barrio.neighbors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.github.blemale.scaffeine.Cache
import com.github.blemale.scaffeine.Scaffeine

import com.codiply.barrio.helpers.Constants
import com.codiply.barrio.neighbors.caching.Types.NeighborsCache

object NeighborhoodReceptionistCachingActor {
  def props(
      locationIndexActorRouter: ActorRef,
      nodeActorRouter: ActorRef,
      neighborsCacheReader: ActorRef,
      neighborsCacheWriter: ActorRef): Props =
    Props(new NeighborhoodReceptionistCachingActor(
        locationIndexActorRouter, nodeActorRouter, neighborsCacheReader, neighborsCacheWriter))
}

class NeighborhoodReceptionistCachingActor(
    locationIndexActorRouter: ActorRef,
    nodeActorRouter: ActorRef,
    neighborsCacheReader: ActorRef,
    neighborsCacheWriter: ActorRef) extends Actor with ActorLogging {
  import ActorProtocol._
  import com.codiply.barrio.neighbors.caching.NeighborsCacheActorProtocol.GetCachedNeighborsRequest
  import com.codiply.barrio.neighbors.caching.NeighborsCacheActorProtocol.GetCachedNeighborsResponse
  import com.codiply.barrio.neighbors.caching.NeighborsCacheActorProtocol.CacheNeighborsRequest

  import context.dispatcher

  val receptionist = context.actorOf(
      NeighborhoodReceptionistActor.props(locationIndexActorRouter, nodeActorRouter), "receptionist")

  def receive: Receive = {
    case request: GetNeighborsRequestByLocationId if !request.includeData && !request.includeLocation => {
      val originalSender = sender
      implicit val askTimeout = Timeout((Constants.slightlyIncreaseTimeout(request.timeoutMilliseconds)).milliseconds)
      (neighborsCacheReader ? GetCachedNeighborsRequest(request.locationId, request.k)).mapTo[GetCachedNeighborsResponse].foreach(cacheResponse => {
        cacheResponse.response match {
          case Some(response) => originalSender ! response
          case None => {
            (receptionist ? request).mapTo[GetNeighborsResponse].foreach(receptionistResponse => {
              originalSender ! receptionistResponse
              if (!receptionistResponse.timeoutReached) {
                neighborsCacheWriter ! CacheNeighborsRequest(request.locationId, receptionistResponse)
              }
            })
          }
        }
      })
    }
    case anyOtherRequest => {
      receptionist.forward(anyOtherRequest)
    }
  }
}
