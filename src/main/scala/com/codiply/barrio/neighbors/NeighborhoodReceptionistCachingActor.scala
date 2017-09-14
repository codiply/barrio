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

object NeighborhoodReceptionistCachingActor {
  def props(
      locationIndexActorRouter: ActorRef,
      nodeActorRouter: ActorRef,
      cacheConfig: CacheConfig): Props =
    Props(new NeighborhoodReceptionistCachingActor(
        locationIndexActorRouter,
        nodeActorRouter,
        cacheConfig))
}

class NeighborhoodReceptionistCachingActor(
    locationIndexActorRouter: ActorRef,
    nodeActorRouter: ActorRef,
    cacheConfig: CacheConfig) extends Actor with ActorLogging {
  import ActorProtocol._

  import context.dispatcher

  val receptionist = context.actorOf(
      NeighborhoodReceptionistActor.props(locationIndexActorRouter, nodeActorRouter), "receptionist")

  val cache: Cache[GetNeighborsRequestByLocationId, GetNeighborsResponse] =
    Scaffeine().recordStats()
    .expireAfterAccess(cacheConfig.expirationAfterAccessSeconds.seconds)
    .maximumSize(cacheConfig.maximumSize)
    .build[GetNeighborsRequestByLocationId, GetNeighborsResponse]()

  def receive: Receive = {
    case request: GetNeighborsRequestByLocationId => {
      val originalSender = sender
      cache.getIfPresent(request) match {
        case Some(response) => sender ! response
        case None => {
          implicit val askTimeout = Timeout((Constants.slightlyIncreaseTimeout(request.timeoutMilliseconds)).milliseconds)
          (receptionist ? request).mapTo[GetNeighborsResponse].foreach(response => {
            cache.put(request, response)
            originalSender ! response
          })
        }
      }
    }
    case anyOtherRequest => {
      receptionist.forward(anyOtherRequest)
    }
  }
}
