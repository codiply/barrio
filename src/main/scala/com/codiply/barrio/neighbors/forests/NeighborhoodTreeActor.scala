package com.codiply.barrio.neighbors.forests

import scala.util.Random
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.Point
import com.codiply.barrio.neighbors.Point._

object NeighborhoodTreeActor {
  def props(points: List[Point], distance: DistanceMetric) = 
    Props(new NeighborhoodTreeActor(points, distance))
}

class NeighborhoodTreeActor(
    points: List[Point],
    distance: DistanceMetric) extends Actor {
  import NeighborhoodTreeActor._
  
  case class Child(centroid: Coordinates, actorRef: ActorRef)
  case class Children(left: Child, right: Child)
  
  val children =
    // TODO: make the threshold of 100 points configurable
    if (points.take(101).length == 101) {
      val centroids = Random.shuffle(points).take(2).toArray
      val centroidLeft = centroids(0).coordinates
      val centroidRight = centroids(1).coordinates
      
      val belongsToLeft = (p: Point) => 
        distance(centroidLeft, p.coordinates) < distance(centroidRight, p.coordinates)
      val (pointsLeft, pointsRight) = points.partition(belongsToLeft)
      
      val childLeftActorRef = context.actorOf(props(pointsLeft, distance))
      val childRightActorRef = context.actorOf(props(pointsRight, distance))
      
      val childLeft = Child(centroidLeft, childLeftActorRef)
      val childRight = Child(centroidRight, childRightActorRef)
      
      Some(Children(left = childLeft, right = childRight))
    }
    else None
  
  def receive: Receive = {
    case request @ GetNeighborsRequest(coordinates, k) => {
      children match {
        case Some(Children(Child(centroidLeft, treeLeft), Child(centroidRight, treeRight))) => {
          val closerToLeft = distance(centroidLeft, coordinates) < distance(centroidRight, coordinates)
          val selectedSubTree = if (closerToLeft) treeLeft else treeRight
          selectedSubTree.forward(request)
        }
        case None => {
          val neighbors = points.sortBy(p => distance(coordinates, p.coordinates)).take(k)
          sender ! GetNeighborsResponse(neighbors)
        }
      }
    }
  }
}
