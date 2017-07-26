package com.codiply.barrio.neighbors.forests

import scala.util.Random
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Actor.Receive
import akka.actor.Props
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.Point
import com.codiply.barrio.neighbors.Point._

object NeighborhoodTreeActor {
  def props(points: List[Point], distance: DistanceMetric) = 
    Props(new NeighborhoodTreeActor(points, distance))
}

object NeighborhoodTreeActorProtocol {
  final case class GetDepthsRequest(aggregatorTimeout: FiniteDuration)
  final case class GetDepthsResponse(depths: List[Int])
}

class NeighborhoodTreeActor(
    points: List[Point],
    distance: DistanceMetric) extends Actor with ActorLogging {
  import ActorProtocol._
  import NeighborhoodTreeActor._
  import NeighborhoodTreeActorProtocol._

  case class Child(centroid: Coordinates, actorRef: ActorRef)
  case class Children(left: Child, right: Child)
  
  var initialisedChildrenCount = 0
  
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
    
    
  val isLeaf = !children.isDefined
  
  if (isLeaf) {
    signalTreeInitialised()
  } else {
    context.become(receive orElse receiveNode)
  }
  
  def receive: Receive = receiveCommon
  
  def receiveCommon: Receive = {
    case request @ GetNeighborsRequest(coordinates, k, timeout) => {
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
    case request @ GetDepthsRequest(timeout) => {
      children match {
        case Some(ch) => {
          val originalSender = sender
          val aggregator = context.actorOf(DepthsAggregatorActor.props(
             originalSender, 2, timeout))
          ch.left.actorRef.tell(request, aggregator)
          ch.right.actorRef.tell(request, aggregator)
        }
        case None => {
          sender ! GetDepthsResponse(List(0))
        }
      }
    }
  }
  
  def receiveNode: Receive = {
    case TreeInitialised => {
      initialisedChildrenCount += 1
      if (initialisedChildrenCount == 2) signalTreeInitialised()
    }
  }
  
  def signalTreeInitialised() = {
    context.parent ! TreeInitialised
  }
}
