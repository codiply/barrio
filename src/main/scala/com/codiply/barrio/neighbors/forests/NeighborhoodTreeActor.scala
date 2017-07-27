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
  def props(
      rootTreeName: String,
      points: List[Point], 
      distance: DistanceMetric,
      thisRootDepth: Int,
      statsActor: ActorRef) = 
    Props(new NeighborhoodTreeActor(rootTreeName, points, distance, thisRootDepth, statsActor))
}

class NeighborhoodTreeActor(
    rootTreeName: String,
    points: List[Point],
    distance: DistanceMetric,
    thisRootDepth: Int,
    statsActor: ActorRef) extends Actor with ActorLogging {
  import ActorProtocol._
  import NeighborhoodForestSearchActorProtocol._
  import NeighborhoodTreeActor._

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
      
      def createChildActor(pts: List[Point]) =
        context.actorOf(props(rootTreeName, pts, distance, thisRootDepth + 1, statsActor))
      
      val childLeftActorRef = createChildActor(pointsLeft)
      val childRightActorRef = createChildActor(pointsRight)
      
      val childLeft = Child(centroidLeft, childLeftActorRef)
      val childRight = Child(centroidRight, childRightActorRef)
      
      Some(Children(left = childLeft, right = childRight))
    }
    else None
    
    
  val isLeaf = !children.isDefined
  
  if (isLeaf) {
    signalTreeInitialised()
    sendStats()
  } else {
    context.become(receive orElse receiveNode)
  }
  
  def receive: Receive = receiveCommon
  
  def receiveCommon: Receive = {
    case request: NeighborsSearchTreeRequest => {
      children match {
        case Some(Children(Child(centroidLeft, treeLeft), Child(centroidRight, treeRight))) => {
          val closerToLeft = distance(centroidLeft, request.coordinates) < distance(centroidRight, request.coordinates)
          val selectedSubTree = if (closerToLeft) treeLeft else treeRight
          selectedSubTree.forward(request)
        }
        case None => {
          val nearestNeighborsContainer = 
            NearestNeighborsContainer.apply(points, request.k, p => distance(p.coordinates, request.coordinates))
          sender ! NeighborsSearchLeafResponse(nearestNeighborsContainer)
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
  
  def sendStats() = {
    statsActor ! NeighborhoodTreeLeafStats(
        treeName = rootTreeName, 
        depth = thisRootDepth, 
        pointCount = points.length)
  }
}
