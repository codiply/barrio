package com.codiply.barrio.nn

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.pattern.ask
import akka.routing.BroadcastGroup
import akka.util.Timeout

class NaiveNeighborhood(
    actorSystem: ActorSystem,
    points: Iterable[Point],
    distance: (List[Double], List[Double]) => Double) extends Neighborhood {
  import NaiveNeighborhoodActor.{GetNeighborsRequest, GetNeighborsResponse}
  import NeighborsQueryActor._
  
  val allPoints = points.toList
  
  val neighborhoodActor = actorSystem.actorOf(NaiveNeighborhoodActor.props(allPoints, distance), name="neighborhoodActor")
  
  val router = actorSystem.actorOf(
      ClusterRouterGroup(
          BroadcastGroup(List("/user/neighborhoodActor*")), 
          ClusterRouterGroupSettings(
              totalInstances = 100, 
              routeesPaths = List("/user/neighborhoodActor*"),
              allowLocalRoutees = true, 
              useRole = None)).props(), name = "neighborhoodRouter")
  
  import actorSystem.dispatcher
  implicit val timeout = Timeout(5 seconds)
  
  def getNeighbors(coordinates: List[Double], k: Int): Future[List[Point]] = {
    val queryActor = actorSystem.actorOf(NeighborsQueryActor.props())
    (queryActor ? NeighborsQuery(router, coordinates, k, distance)).mapTo[GetNeighborsResponse].map(_.neighbors)
  }
}

object NeighborsQueryActor {
  case class NeighborsQuery(
      router: ActorRef, 
      coordinates: List[Double], 
      k: Int,
      distance: (List[Double], List[Double]) => Double)
  
  def props() = Props(new NeighborsQueryActor)
}

class NeighborsQueryActor extends Actor {
  import NeighborsQueryActor._
  import NaiveNeighborhoodActor._
  
  def receive = waitingQuery
  
  def waitingQuery: PartialFunction[Any, Unit] = {
    case NeighborsQuery(router, coordinates, k, distance) => {
      val aggregator = context.actorOf(NeighborsAggregator.props(coordinates, k, distance, 3, self))
      router ! GetNeighborsRequest(aggregator, coordinates, k)
      val querySender = sender()
      context.become(waitingResponse(querySender))
    }
  }
  
  def waitingResponse(querySender: ActorRef): PartialFunction[Any, Unit] = {
    case GetNeighborsResponse(neighbors) => {
      querySender ! GetNeighborsResponse(neighbors)
      context.stop(self)
    }
  }
}

object NeighborsAggregator {
  def props(
      coordinates: List[Double], 
      k: Int, 
      distance: (List[Double], List[Double]) => Double,
      responsesToExpect: Int,
      sendResultTo: ActorRef) = Props(new NeighborsAggregator(coordinates, k, distance, responsesToExpect, sendResultTo))
}

class NeighborsAggregator(
    coordinates: List[Double], 
    k: Int, 
    distance: (List[Double], List[Double]) => Double,
    responsesToExpect: Int,
    sendResultTo: ActorRef) extends Actor {  
  import NaiveNeighborhoodActor._
  
  def receive = waitingResponses(responsesToExpect, List.empty)
  
  def waitingResponses(outstandingResponses: Int, neighbors: List[Point]): PartialFunction[Any, Unit] = {
    case GetNeighborsResponse(additionalNeighbors) => {
      val newNeighbors = (neighbors ++ additionalNeighbors)
        .sortBy(x => distance(x.coordinates, coordinates))
        .take(k)
      if (outstandingResponses > 1) { 
        context.become(waitingResponses(outstandingResponses - 1, newNeighbors))
      } else {
        sendResultTo ! GetNeighborsResponse(newNeighbors)
        context.stop(self)
      }
    }
  }
}

object NaiveNeighborhoodActor {
  case class GetNeighborsRequest(aggregator: ActorRef, coordinates: List[Double], k: Int)
  case class GetNeighborsResponse(neighbors: List[Point])
  
  def props(points: List[Point], distance: (List[Double], List[Double]) => Double) = 
    Props(new NaiveNeighborhoodActor(points, distance))
}

class NaiveNeighborhoodActor(
    points: List[Point],
    distance: (List[Double], List[Double]) => Double) extends Actor {
  import NaiveNeighborhoodActor._
  
  def receive = {
    case GetNeighborsRequest(aggregator, coordinates, k) => {
      val neighbors = points.sortBy(p => distance(p.coordinates, coordinates)).take(k)
      aggregator ! GetNeighborsResponse(neighbors)
    }
  }
}