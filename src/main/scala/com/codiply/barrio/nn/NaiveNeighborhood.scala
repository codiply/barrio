package com.codiply.barrio.nn

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.{ Actor, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

class NaiveNeighborhood(
    points: Iterable[Point],
    distance: (List[Double], List[Double]) => Double) extends Neighborhood {
  import NaiveNeighborhoodActor.{GetNeighborsRequest, GetNeighborsResponse}
  
  val allPoints = points.toList
  
  val actorSystem = ActorSystem("Barrio")
  val neighborhoodActor = actorSystem.actorOf(NaiveNeighborhoodActor.props(allPoints, distance))
  
  // Needed
  import actorSystem.dispatcher
  implicit val timeout = Timeout(5 seconds)
  
  def getNeighbors(coordinates: List[Double], k: Int): Future[List[Point]] = 
    (neighborhoodActor ? GetNeighborsRequest(coordinates, k)).mapTo[GetNeighborsResponse].map(_.neighbors)
}

object NaiveNeighborhoodActor {
  case class GetNeighborsRequest(coordinates: List[Double], k: Int)
  case class GetNeighborsResponse(neighbors: List[Point])
  
  def props(points: List[Point], distance: (List[Double], List[Double]) => Double) = 
    Props(new NaiveNeighborhoodActor(points, distance))
}

class NaiveNeighborhoodActor(
    points: List[Point],
    distance: (List[Double], List[Double]) => Double) extends Actor {
  import NaiveNeighborhoodActor._
  
  def receive = {
    case GetNeighborsRequest(coordinates, k) => {
      val neighbors = points.sortBy(p => distance(p.coordinates, coordinates)).take(k)
      sender() ! GetNeighborsResponse(neighbors)
    }
  }
}