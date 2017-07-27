package com.codiply.barrio.neighbors.forests

import scala.concurrent.duration._
import scala.collection.mutable.PriorityQueue
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import com.codiply.barrio.neighbors.Point

object NeighborhoodForestSearchActor {
  def props(
      responseRecipient: ActorRef, 
      treesToSearch: List[ActorRef],
      coordinates: List[Double], 
      k: Int, 
      distanceThreshold: Double,
      timeout: FiniteDuration) = Props(new NeighborhoodForestSearchActor(
          responseRecipient, treesToSearch, coordinates, k, distanceThreshold, timeout))
}

object NeighborhoodForestSearchActorProtocol {
  final object DoSendResponse
  final case class NeighborsSearchTreeRequest(coordinates: List[Double], k: Int, distanceThreshold: Double)
  final case class NeighborsSearchLeafResponse(container: NearestNeighborsContainer)
  final case class CandidateSubTree(root: ActorRef, minEasyDistance: Double)
  final case class EnqueueCandidate(candidate: CandidateSubTree)
}

class NeighborhoodForestSearchActor(
      responseRecipient: ActorRef, 
      treesToSearch: List[ActorRef],
      coordinates: List[Double], 
      k: Int,
      easyDistanceThreshold: Double,
      timeout: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher
  import com.codiply.barrio.neighbors.ActorProtocol._
  import NeighborhoodForestSearchActorProtocol._
  import scala.math.Ordering.Implicits._
  
  val timeoutCancellable = context.system.scheduler.scheduleOnce(timeout, self, DoSendResponse)
  
  val prioritisedSubTrees = PriorityQueue[CandidateSubTree]()(Ordering[Double].on[CandidateSubTree](-_.minEasyDistance))
  treesToSearch.map { CandidateSubTree(_, 0.0) }.foreach { prioritisedSubTrees.enqueue(_) }
  
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
  
  def pruneQueue() = {
    nearestNeighborsContainer.distanceUpperBound.foreach {
      upperBound => prioritisedSubTrees.filter(tree => tree.minEasyDistance <= upperBound)
    }
  }
  
  def sendNextSearchTreeRequest() = {
    if (prioritisedSubTrees.isEmpty)
      sendResponse()
    else {
      val subTree = prioritisedSubTrees.dequeue()
      subTree.root ! NeighborsSearchTreeRequest(coordinates, k, easyDistanceThreshold)
    }
  }
  
  def sendResponse() = {
    timeoutCancellable.cancel()
    val neighbors = nearestNeighborsContainer.orderedDistinctNeighbors.map(_.point).toList
    responseRecipient ! GetNeighborsResponse(neighbors)
    context.stop(self)
  }
}