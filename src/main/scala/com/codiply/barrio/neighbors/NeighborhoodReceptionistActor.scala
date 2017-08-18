package com.codiply.barrio.neighbors

import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Address
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.Props

import com.codiply.barrio.generic.AggregatorActorProtocol.TerminateAggregationEarly
import com.codiply.barrio.generic.OneOffForwardingActor
import com.codiply.barrio.generic.OneOffForwardingActor.OneOffForward
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.helpers.Constants
import com.codiply.barrio.neighbors.aggregators.LocationIndexAggregatorActor
import com.codiply.barrio.neighbors.aggregators.NeighborAggregatorActor
import com.codiply.barrio.neighbors.aggregators.NodeStatsAggregatorActor
import com.codiply.barrio.neighbors.LocationIndexActorProtocol.GetLocationRequest
import com.codiply.barrio.neighbors.LocationIndexActorProtocol.GetLocationResponse

object NeighborhoodReceptionistActor {
  def props(locationIndexActorRouter: ActorRef, nodeActorRouter: ActorRef): Props =
    Props(new NeighborhoodReceptionistActor(locationIndexActorRouter, nodeActorRouter))
}

class NeighborhoodReceptionistActor(
    locationIndexActorRouter: ActorRef,
    nodeActorRouter: ActorRef) extends Actor with ActorLogging {
  import ActorProtocol._

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember], classOf[ReachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  var nodeSet = Set[Address]()
  var nodeCount = 0

  val receive: Receive = receiveRequests orElse receiveClusterEvents

  def receiveRequests: Receive = {
    case request @ GetNeighborsRequestByLocation(location, k, distanceThreshold, _, _, timeoutMilliseconds) => {
      val originalSender = sender
      val neighborAggregator = context.actorOf(NeighborAggregatorActor.props(
          k, originalSender, nodeCount, timeoutMilliseconds.milliseconds))
      val newRequest = request.copy(timeoutMilliseconds = Constants.slightlyReduceTimeout(request.timeoutMilliseconds))
      nodeActorRouter.tell(newRequest, neighborAggregator)
    }
    case request: GetNeighborsRequestByLocationId => {
      val originalSender = sender
      val myself = self
      val slightlyReducedTimeout = Constants.slightlyReduceTimeout(request.timeoutMilliseconds)

      // Prepare the aggregator for the neighbors.
      val neighborAggregator = context.actorOf(NeighborAggregatorActor.props(
          request.k, originalSender, nodeCount, request.timeoutMilliseconds.milliseconds))

      val forwardingLogic = (getLocationResponse: GetLocationResponse) =>
        getLocationResponse.location match {
          case Some(location) => OneOffForward(
              from = neighborAggregator,
              to = nodeActorRouter,
              message = GetNeighborsRequestByLocation(
                location, request.k, request.distanceThreshold,
                includeData = request.includeData,
                includeLocation = request.includeLocation,
                slightlyReducedTimeout))
          case None => OneOffForward(from = myself, to = neighborAggregator, message = TerminateAggregationEarly)
      }

      val forwardingActor = context.actorOf(OneOffForwardingActor.props(forwardingLogic, None, slightlyReducedTimeout.milliseconds))

      val locationIndexAggregator = context.actorOf(LocationIndexAggregatorActor.props(
          forwardingActor, nodeCount, slightlyReducedTimeout.milliseconds))
      locationIndexActorRouter.tell(GetLocationRequest(request.locationId), locationIndexAggregator)
    }
    case request @ GetClusterStatsRequest(timeoutMilliseconds, doGarbageCollect) => {
      val originalSender = sender
      val aggregator = context.actorOf(NodeStatsAggregatorActor.props(
          originalSender, nodeCount, timeoutMilliseconds.milliseconds))
      nodeActorRouter.tell(GetNodeStatsRequest(Constants.slightlyReduceTimeout(timeoutMilliseconds), doGarbageCollect), aggregator)
    }
  }

  def receiveClusterEvents: Receive = {
    case event @ MemberUp(member) =>
      this.nodeSet += member.address
      this.updateNodeCount()
    case event @ MemberWeaklyUp(member) =>
      this.nodeSet += member.address
      this.updateNodeCount()
    case event @ ReachableMember(member) =>
      this.nodeSet += member.address
      this.updateNodeCount()
    case event @ UnreachableMember(member) =>
      this.nodeSet -= member.address
      this.updateNodeCount()
    case event @ MemberLeft(member) =>
      this.nodeSet -= member.address
      this.updateNodeCount()
    case event @ MemberExited(member) =>
      this.nodeSet -= member.address
      this.updateNodeCount()
    case event @ MemberRemoved(member, previousStatus) =>
      this.nodeSet -= member.address
      this.updateNodeCount()
  }

  private def updateNodeCount() = {
    this.nodeCount = this.nodeSet.size
    log.info("Node count is now: {}", this.nodeCount)
  }
}
