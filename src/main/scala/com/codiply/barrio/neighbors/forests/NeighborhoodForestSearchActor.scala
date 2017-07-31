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
      timeout: FiniteDuration): Props = Props(new NeighborhoodForestSearchActor(
          responseRecipient, treesToSearch, location, k, distanceThreshold, timeout))
}

object NeighborhoodForestSearchActorProtocol {
  final object DoSendResponse
  final case class NeighborsSearchTreeRequest(location: List[Double], k: Int, distanceThreshold: EasyDistance)
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
      timeout: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher
  import com.codiply.barrio.neighbors.ActorProtocol._
  import NeighborhoodForestSearchActorProtocol._
  import scala.math.Ordering.Implicits._

  val timeoutCancellable = context.system.scheduler.scheduleOnce(timeout, self, DoSendResponse)

  val prioritisedSubTrees = PriorityQueue[CandidateSubTree]()(Ordering[Double].on[CandidateSubTree](-_.minDistance.value))
  treesToSearch.map { CandidateSubTree(_, EasyDistance(0.0)) }.foreach { prioritisedSubTrees.enqueue(_) }

  var nearestNeighborsContainer = NearestNeighborsContainer.empty(k)

  // Initiate the search
  sendNextSearchTreeRequest()

  def receive: Receive = {
    case request: NeighborsSearchLeafResponse => {
      nearestNeighborsContainer = nearestNeighborsContainer.merge(request.container)
      pruneQueue()
      sendNextSearchTreeRequest()
    }
    case EnqueueCandidate(candidate) => {
      prioritisedSubTrees.enqueue(candidate)
    }
    case DoSendResponse => sendResponse()
  }

  private def pruneQueue() = {
    nearestNeighborsContainer.distanceUpperBound.foreach {
      upperBound => prioritisedSubTrees.filter(tree => tree.minDistance.lessEqualThan(upperBound))
    }
  }

  private def sendNextSearchTreeRequest() = {
    if (prioritisedSubTrees.isEmpty) {
      sendResponse()
    }
    else {
      val subTree = prioritisedSubTrees.dequeue()
      subTree.root ! NeighborsSearchTreeRequest(location, k, distanceThreshold)
    }
  }

  private def sendResponse() = {
    timeoutCancellable.cancel()
    responseRecipient ! nearestNeighborsContainer
    context.stop(self)
  }
}
