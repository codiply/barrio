package com.codiply.barrio.neighbors

import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import Point._

object NeighborAggregatorActor {
  def props(
      coordinates: Coordinates,
      kNeighbors: Int,
      distance: DistanceMetric,
      replyTo: ActorRef,
      expectedNumberOfResponses: Int,
      timeout: FiniteDuration) = Props(new NeighborAggregatorActor(
          coordinates, kNeighbors, distance, replyTo, expectedNumberOfResponses, timeout))
}

object NeighborAggregatorActorProtocol {
  object DoSendAggregate
}

class NeighborAggregatorActor(
    coordinates: Coordinates,
    kNeighbors: Int,
    distance: DistanceMetric,
    originalQuerySender: ActorRef,
    expectedNumberOfResponses: Int,
    timeout: FiniteDuration) extends Actor {
  import NeighborhoodPatchActorProtocol._
  import NeighborAggregatorActorProtocol._
  import context.dispatcher
  
  var nearestNeighbors: List[Point] = Nil
  var outstandingResponses = expectedNumberOfResponses
 
  context.system.scheduler.scheduleOnce(timeout, self, DoSendAggregate)
  
  def receive: Receive = {
    case GetNeighborsResponse(neighbors) =>
      this.nearestNeighbors = (nearestNeighbors ++ neighbors).sortBy(p => 
        distance(coordinates, p.coordinates)).take(kNeighbors)
      this.outstandingResponses -= 1
      if (this.outstandingResponses <= 0) self ! DoSendAggregate
    case DoSendAggregate => 
      originalQuerySender ! GetNeighborsResponse(nearestNeighbors)
      context.stop(self)
  }
}
