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
  case class Children(left: Child, right: Child)
  case class NodeSettings(
      children: Children, isCloserToLeft: Coordinates => Boolean, distanceToPartitioningPlane: Coordinates => EasyDistance)

  var initialisedChildrenCount = 0

  val metric = config.metric
  val centroidSelector = CentroidSelectionAlgorithm.randomFurthest

  def receive: Receive = receiveInitial

  def receiveInitial: Receive = {
    case InitialiseTree(points: List[Point]) => {
      val nodeSettings =
        if (points.take(config.maxPointsPerLeaf + 1).length > config.maxPointsPerLeaf) {
          centroidSelector.select(random, points, metric).map { case (centroidLeft, centroidRight) => {
            val isCloserToLeft = (location: Coordinates) =>
              metric.easyDistance(centroidLeft, location).lessThan(
                  metric.easyDistance(centroidRight, location))
              val (pointsLeft, pointsRight) = points.partition { (p: Point) => isCloserToLeft(p.location) }

            def createChildActor() =
              context.actorOf(props(rootTreeName, config, random.createNew(), thisRootDepth + 1, statsActor))

            val childLeftActorRef = createChildActor()
            childLeftActorRef ! InitialiseTree(pointsLeft)
            val childRightActorRef = createChildActor()
            childRightActorRef ! InitialiseTree(pointsRight)

            val childLeft = Child(centroidLeft, childLeftActorRef)
            val childRight = Child(centroidRight, childRightActorRef)

            // In the unlikely event that the two centroids have the same coordinates,
            // give zero distance to the boundary so that both leafs are inspected.
            val distanceToBoundary = metric.easyDistanceToPlane(
                PartitioningPlane(centroidLeft, centroidRight)).getOrElse((x: Coordinates) => EasyDistance(0.0))

            NodeSettings(Children(left = childLeft, right = childRight), isCloserToLeft, distanceToBoundary)
          }
        }
      } else {
        None
      }

      nodeSettings match {
        case Some(settings) => {
          context.become(receiveNode(settings))
        }
        case None => {
          signalTreeInitialised()
          sendStats(points)
          context.become(receiveLeaf(points))
        }
      }
    }
  }

  def receiveNode(nodeSettings: NodeSettings): Receive = {
    case request: NeighborsSearchTreeRequest => {
      nodeSettings match {
        case NodeSettings(
            Children(
                Child(centroidLeft, treeLeft),
                Child(centroidRight, treeRight)),
                closerToLeft,
                distanceToPartitioningPlane) => {
          val closerToLeft =
            metric.easyDistance(centroidLeft, request.location).lessThan(
                metric.easyDistance(centroidRight, request.location))
          val selectedSubTree = if (closerToLeft) treeLeft else treeRight
          val otherSubTree = if (closerToLeft) treeRight else treeLeft
          val dist = distanceToPartitioningPlane(request.location)
          if (dist.lessEqualThan(request.distanceThreshold)) {
            sender ! EnqueueCandidate(CandidateSubTree(otherSubTree, dist))
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
