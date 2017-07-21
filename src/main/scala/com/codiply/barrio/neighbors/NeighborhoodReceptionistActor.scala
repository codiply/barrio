package com.codiply.barrio.neighbors

import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Props
import Point._

object NeighborhoodReceptionistActor {
  def props(nodeActorRouter: ActorRef, distance: DistanceMetric, aggregatorTimeout: FiniteDuration) = 
    Props(new NeighborhoodReceptionistActor(nodeActorRouter, distance, aggregatorTimeout))
}

class NeighborhoodReceptionistActor(
    nodeActorRouter: ActorRef,
    distance: DistanceMetric,
    aggregatorTimeout: FiniteDuration) extends Actor {
  import NeighborhoodPatchActorProtocol._
  
  var nodeCount = 3
  
  def receive: Receive = {
    case request @ GetNeighborsRequest(coordinates, k) => {
      val originalSender = sender
      val aggregator = context.actorOf(NeighborAggregatorActor.props(
          coordinates, k, distance, originalSender, nodeCount, aggregatorTimeout))
      nodeActorRouter.tell(request, aggregator) 
    }
  }
}