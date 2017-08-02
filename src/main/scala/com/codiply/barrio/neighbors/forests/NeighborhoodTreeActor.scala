package com.codiply.barrio.neighbors.forests

import scala.util.Random
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Actor.Receive
import akka.actor.Props

import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.helpers.RandomProvider
import com.codiply.barrio.neighbors.NeighborhoodConfig

object NeighborhoodTreeActor {
  def props(
      rootTreeName: String,
      config: NeighborhoodConfig,
      random: RandomProvider,
      thisRootDepth: Int,
      statsActor: ActorRef): Props =
    Props(new NeighborhoodTreeActor(rootTreeName, config, random, thisRootDepth, statsActor))
}

class NeighborhoodTreeActor(
    rootTreeName: String,
    config: NeighborhoodConfig,
    random: RandomProvider,
    thisRootDepth: Int,
    statsActor: ActorRef) extends Actor with ActorLogging {
  import com.codiply.barrio.geometry.EasyDistance
  import com.codiply.barrio.geometry.PartitioningPlane
  import com.codiply.barrio.geometry.Point.Coordinates
  import com.codiply.barrio.neighbors.ActorProtocol._
  import com.codiply.barrio.neighbors.forests.ActorProtocol._
  import com.codiply.barrio.neighbors.forests.CentroidSelectionAlgorithm
  import com.codiply.barrio.neighbors.forests.NeighborhoodForestSearchActorProtocol._
  import com.codiply.barrio.neighbors.forests.NeighborhoodTreeActor._
  import com.codiply.barrio.neighbors.NearestNeighborsContainer

  case class Child(centroid: Coordinates, actorRef: ActorRef)
  case class NodeChildren(left: Child, right: Child)

  var initialisedChildrenCount = 0

  val metric = config.metric
  val centroidSelector = CentroidSelectionAlgorithm.randomFurthest

  def receive: Receive = receiveInitial

  def receiveInitial: Receive = {
    case InitialiseTree(points: List[Point]) => {
      val nodeChildren =
        if (points.take(config.maxPointsPerLeaf + 1).length > config.maxPointsPerLeaf) {
          centroidSelector.select(random, points, metric).map { case (centroidLeft, centroidRight) => {
            val isCloserToLeft = closerToLeft(centroidLeft, centroidRight)
            val (pointsLeft, pointsRight) = points.partition { (p: Point) => isCloserToLeft(p.location) }

            def createChildActor() =
              context.actorOf(props(rootTreeName, config, random.createNew(), thisRootDepth + 1, statsActor))

            val childLeftActorRef = createChildActor()
            childLeftActorRef ! InitialiseTree(pointsLeft)
            val childRightActorRef = createChildActor()
            childRightActorRef ! InitialiseTree(pointsRight)

            val childLeft = Child(centroidLeft, childLeftActorRef)
            val childRight = Child(centroidRight, childRightActorRef)

            NodeChildren(left = childLeft, right = childRight)
          }
        }
      } else {
        None
      }

      nodeChildren match {
        case Some(children) => {
          context.become(receiveNode(children))
        }
        case None => {
          signalTreeInitialised()
          sendStats(points)
          context.become(receiveLeaf(points))
        }
      }
    }
  }

  def receiveNode(nodeChildren: NodeChildren): Receive = {
    case request: NeighborsSearchTreeRequest => {
      nodeChildren match {
        case NodeChildren(
                Child(centroidLeft, treeLeft),
                Child(centroidRight, treeRight)) => {
          val isCloserToLeft = closerToLeft(centroidLeft, centroidRight)(request.location)
          val selectedSubTree = if (isCloserToLeft) treeLeft else treeRight
          val otherSubTree = if (isCloserToLeft) treeRight else treeLeft
          // In the unlikely event that the two centroids have the same coordinates,
          // give zero distance to the boundary so that both leafs are inspected.
          val distanceToBoundary = metric.easyDistanceToPlane(
              PartitioningPlane(centroidLeft, centroidRight)).map { _(request.location) }.getOrElse(EasyDistance(0.0))

          if (distanceToBoundary.lessEqualThan(request.distanceThreshold)) {
            sender ! EnqueueCandidate(CandidateSubTree(otherSubTree, distanceToBoundary))
          }
          selectedSubTree.forward(request)
        }
      }
    }
    case TreeInitialised => {
      initialisedChildrenCount += 1
      if (initialisedChildrenCount == 2) signalTreeInitialised()
    }
  }

  def receiveLeaf(points: List[Point]): Receive = {
    case request: NeighborsSearchTreeRequest => {
      val nearestNeighborsContainer =
        NearestNeighborsContainer(points, request.k, p => metric.easyDistance(p.location, request.location))
      sender ! NeighborsSearchLeafResponse(nearestNeighborsContainer)
    }
  }

  private def closerToLeft(centroidLeft: Coordinates, centroidRight: Coordinates) =
    (location: Coordinates) =>
      metric.easyDistance(centroidLeft, location).lessThan(metric.easyDistance(centroidRight, location))

  private def signalTreeInitialised() = {
    context.parent ! TreeInitialised
  }

  private def sendStats(points: List[Point]) = {
    statsActor ! NeighborhoodTreeLeafStats(
        treeName = rootTreeName,
        depth = thisRootDepth,
        pointCount = points.length)
  }
}
