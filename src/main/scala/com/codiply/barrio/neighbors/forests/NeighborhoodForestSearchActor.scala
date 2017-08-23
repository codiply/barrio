package com.codiply.barrio.neighbors.forests

import scala.concurrent.duration._
import scala.collection.mutable.PriorityQueue
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.NearestNeighborsContainer

object NeighborhoodForestSearchActor {
  def props(
      responseRecipient: ActorRef,
      treesToSearch: List[ActorRef],
      location: List[Double],
      k: Int,
      distanceThreshold: EasyDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Int): Props = Props(new NeighborhoodForestSearchActor(
          responseRecipient, treesToSearch, location, k, distanceThreshold,
          includeData = includeData, includeLocation = includeLocation, timeoutMilliseconds))
}

object NeighborhoodForestSearchActorProtocol {
  final object DoSendResponse
  final case class NeighborsSearchTreeRequest(
      location: List[Double], k: Int, includeData: Boolean, includeLocation: Boolean, distanceThreshold: EasyDistance)
  final case class NeighborsSearchLeafResponse(container: NearestNeighborsContainer)
  final case class CandidateSubTree(root: ActorRef, minDistance: EasyDistance)
  final case class EnqueueCandidate(candidate: CandidateSubTree)
}

class NeighborhoodForestSearchActor(
      responseRecipient: ActorRef,
      treesToSearch: List[ActorRef],
      location: List[Double],
      k: Int,
      distanceThreshold: EasyDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Int) extends Actor with ActorLogging {
  import context.dispatcher
  import com.codiply.barrio.neighbors.ActorProtocol._
  import NeighborhoodForestSearchActorProtocol._
  import scala.math.Ordering.Implicits._

  val timeoutCancellable = context.system.scheduler.scheduleOnce(timeoutMilliseconds.milliseconds, self, DoSendResponse)

  var prioritisedSubTrees = PriorityQueue[CandidateSubTree]()(Ordering[Double].on[CandidateSubTree](-_.minDistance.value))
  treesToSearch.map { CandidateSubTree(_, EasyDistance(0.0)) }.foreach { prioritisedSubTrees.enqueue(_) }

  var nearestNeighborsContainer = NearestNeighborsContainer.empty(k)

  var currentDistanceThreshold = distanceThreshold

  // Initiate the search
  sendNextSearchTreeRequest()

  def receive: Receive = {
    case request: NeighborsSearchLeafResponse => {
      nearestNeighborsContainer = nearestNeighborsContainer.merge(request.container)

      nearestNeighborsContainer.distanceUpperBound.foreach {
        upperBound => {
          pruneQueue(upperBound)
          updateDistanceThreshold(upperBound)
        }
      }

      sendNextSearchTreeRequest()
    }
    case EnqueueCandidate(candidate) => {
      prioritisedSubTrees.enqueue(candidate)
    }
    case DoSendResponse => sendResponse()
  }

  private def pruneQueue(distanceUpperBound: EasyDistance): Unit =
    prioritisedSubTrees = prioritisedSubTrees.filter(tree => tree.minDistance <= distanceUpperBound)

  private def updateDistanceThreshold(distanceUpperBound: EasyDistance): Unit =
    currentDistanceThreshold = EasyDistance.min(currentDistanceThreshold, distanceUpperBound)

  private def sendNextSearchTreeRequest(): Unit = {
    if (prioritisedSubTrees.isEmpty) {
      sendResponse()
    }
    else {
      val subTree = prioritisedSubTrees.dequeue()
      subTree.root ! NeighborsSearchTreeRequest(
          location, k, includeData = includeData, includeLocation = includeLocation, currentDistanceThreshold)
    }
  }

  private def sendResponse(): Unit = {
    timeoutCancellable.cancel()
    responseRecipient ! nearestNeighborsContainer
    context.stop(self)
  }
}
