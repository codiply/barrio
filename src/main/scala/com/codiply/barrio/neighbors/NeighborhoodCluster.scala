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
    config: NeighborhoodConfig) extends NeighborProvider {
  import ActorProtocol._
  import forests.NeighborhoodForestActor

  val points = pointsLoader().toList
  val metric = config.metric

  import actorSystem.dispatcher

  val nodeActor = actorSystem.actorOf(
      NeighborhoodForestActor.props(config.nodeName, points, config),
      "neighborhood-node-" + config.nodeName)

  val nodeActorRouter = actorSystem.actorOf(
      ClusterRouterGroup(
          BroadcastGroup(List("/user/neighborhood-node-*")),
          ClusterRouterGroupSettings(
              totalInstances = Int.MaxValue,
              routeesPaths = List("/user/neighborhood-node-*"),
              allowLocalRoutees = true,
              useRole = None)).props(), name = "router-to-neighborhood-nodes")

  val receptionistActor = actorSystem.actorOf(
      NeighborhoodReceptionistActor.props(nodeActorRouter), "receptionist")

  def getNeighbors(coordinates: List[Double], k: Int, distanceThreshold: RealDistance): Future[List[Point]] = {
    if (coordinates.length == config.dimensions) {
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
