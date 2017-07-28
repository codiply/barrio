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
import Point._

object NeighborhoodReceptionistActor {
  def props(nodeActorRouter: ActorRef, metric: DistanceMetric): Props =
    Props(new NeighborhoodReceptionistActor(nodeActorRouter, metric))
}

class NeighborhoodReceptionistActor(
    nodeActorRouter: ActorRef,
    metric: DistanceMetric) extends Actor with ActorLogging {
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
    case request @ GetNeighborsRequest(coordinates, k, distanceThreshold, aggregatorTimeout) => {
      val originalSender = sender
      val aggregator = context.actorOf(NeighborAggregatorActor.props(
          coordinates, k, metric, originalSender, nodeCount, 2 * aggregatorTimeout))
      nodeActorRouter.tell(request, aggregator)
    }
    case request @ GetClusterStatsRequest(aggregatorTimeout) => {
      val originalSender = sender
      val aggregator = context.actorOf(NodeStatsAggregatorActor.props(
          originalSender, nodeCount, 2 * aggregatorTimeout))
      nodeActorRouter.tell(GetNodeStatsRequest(aggregatorTimeout), aggregator)
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
