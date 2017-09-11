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

import com.codiply.barrio.helpers.Constants

object NeighborhoodReceptionistCachingActor {
  def props(locationIndexActorRouter: ActorRef, nodeActorRouter: ActorRef): Props =
    Props(new NeighborhoodReceptionistCachingActor(locationIndexActorRouter, nodeActorRouter))
}

class NeighborhoodReceptionistCachingActor(
    locationIndexActorRouter: ActorRef,
    nodeActorRouter: ActorRef) extends Actor with ActorLogging {
  import ActorProtocol._

  import context.dispatcher

  val receptionist = context.actorOf(
      NeighborhoodReceptionistActor.props(locationIndexActorRouter, nodeActorRouter), "receptionist")

  // TODO: Replace with an actual Cache
  var cache = Map[GetNeighborsRequestByLocationId, GetNeighborsResponse]()

  def receive: Receive = {
    case request: GetNeighborsRequestByLocationId => {
      val originalSender = sender
      cache.get(request) match {
        case Some(response) => sender ! response
        case None => {
          implicit val askTimeout = Timeout((Constants.slightlyIncreaseTimeout(request.timeoutMilliseconds)).milliseconds)
          (receptionist ? request).mapTo[GetNeighborsResponse].foreach(response => {
            cache = cache + (request -> response)
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
