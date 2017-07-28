package com.codiply.barrio.neighbors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.pattern.ask
import akka.routing.BroadcastGroup
import akka.util.Timeout
import Point._

class NeighborhoodCluster (
    actorSystem: ActorSystem,
    pointsLoader: () => Iterable[Point],
    metric: DistanceMetric) extends NeighborProvider {
  import ActorProtocol._
  import forests.NeighborhoodForestActor

  val points = pointsLoader().toList

  import actorSystem.dispatcher

  // TODO: Take the number of trees from configuration
  val nodeActor = actorSystem.actorOf(NeighborhoodForestActor.props(points, metric, 3), "neighborhood-node")

  val nodeActorRouter = actorSystem.actorOf(
      ClusterRouterGroup(
          BroadcastGroup(List("/user/neighborhood-node")),
          ClusterRouterGroupSettings(
              totalInstances = Int.MaxValue,
              routeesPaths = List("/user/neighborhood-node"),
              allowLocalRoutees = true,
              useRole = None)).props(), name = "neighborhood-node-router")

  val receptionistActor = actorSystem.actorOf(
      NeighborhoodReceptionistActor.props(nodeActorRouter, metric), "receptionist")

  def getNeighbors(coordinates: List[Double], k: Int, distanceThreshold: Double): Future[List[Point]] = {
    val timeout: FiniteDuration = 5.seconds
    implicit val askTimeout = Timeout(2 * timeout)

    (receptionistActor ? GetNeighborsRequest(coordinates, k , distanceThreshold, timeout)).mapTo[GetNeighborsResponse].map(_.neighbors)
  }

  def getStats(): Future[ClusterStats] = {
    val timeout: FiniteDuration = 3.minutes
    implicit val askTimeout = Timeout(2 * timeout)
    (receptionistActor ? GetClusterStatsRequest(timeout)).mapTo[GetClusterStatsResponse].map(_.stats)
  }
}
