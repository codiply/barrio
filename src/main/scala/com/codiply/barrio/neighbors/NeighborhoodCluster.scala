package com.codiply.barrio.neighbors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.pattern.ask
import akka.routing.BroadcastGroup
import akka.util.Timeout

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.RealDistance

class NeighborhoodCluster (
    actorSystem: ActorSystem,
    pointsLoader: () => Iterable[Point],
    dimensions: Int,
    metric: Metric) extends NeighborProvider {
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

  def getNeighbors(coordinates: List[Double], k: Int, distanceThreshold: RealDistance): Future[List[Point]] = {
    if (coordinates.length == dimensions) {
      val timeout: FiniteDuration = 5.seconds
      implicit val askTimeout = Timeout(2 * timeout)

      metric.toEasyDistance(distanceThreshold) match {
        case Some(easyDistanceThreshold) =>
          (receptionistActor ? GetNeighborsRequest(
            coordinates, k , easyDistanceThreshold, timeout)).mapTo[GetNeighborsResponse].map(_.neighbors)
        case None => Future(List[Point]())
      }
    } else {
      Future(List[Point]())
    }
  }

  def getStats(): Future[ClusterStats] = {
    val timeout: FiniteDuration = 3.minutes
    implicit val askTimeout = Timeout(2 * timeout)
    (receptionistActor ? GetClusterStatsRequest(timeout)).mapTo[GetClusterStatsResponse].map(_.stats)
  }
}
