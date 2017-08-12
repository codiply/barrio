package com.codiply.barrio.neighbors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.pattern.ask
import akka.routing.BroadcastGroup
import akka.util.Timeout

import com.codiply.barrio.helpers.Constants
import com.codiply.barrio.helpers.RandomProvider
import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.geometry.RealDistance

class NeighborhoodCluster (
    actorSystem: ActorSystem,
    pointsLoader: () => Iterable[Point],
    config: NeighborhoodConfig,
    random: RandomProvider) extends NeighborProvider {
  import ActorProtocol.GetClusterStatsRequest
  import ActorProtocol.GetClusterStatsResponse
  import ActorProtocol.GetNeighborsRequest
  import ActorProtocol.GetNeighborsResponse
  import com.codiply.barrio.neighbors.forests.NeighborhoodForestActor

  val points = pointsLoader().toList
  val metric = config.metric

  val idToCoordinates: Map[String, Coordinates] = points.map(p => (p.id, p.location)).toMap

  import actorSystem.dispatcher

  val nodeActor = actorSystem.actorOf(
      NeighborhoodForestActor.props(config.nodeName, points, config, random),
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

  def getNeighbors(
    location: Option[List[Double]],
    locationId: Option[String],
    k: Int,
    distanceThreshold: RealDistance,
    includeData: Boolean,
    includeLocation: Boolean,
    timeoutMilliseconds: Option[Int]): Future[Vector[Neighbor]] = {
    (location, locationId) match {
      case (Some(location), None) => getNeighborsByLocation(
        location, k, distanceThreshold, includeData = includeData, includeLocation = includeLocation, timeoutMilliseconds)
      case (None, Some(locationId)) => getNeighborsById(
        locationId, k, distanceThreshold, includeData = includeData, includeLocation = includeLocation, timeoutMilliseconds)
      case _ => Future(Vector[Neighbor]())
    }
  }

  private def getNeighborsByLocation(
      location: List[Double],
      k: Int,
      distanceThreshold: RealDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Option[Int]): Future[Vector[Neighbor]] = {
    if (location.length == config.dimensions) {
      val effectiveTimeoutMilliseconds = config.getEffectiveTimeoutMilliseconds(timeoutMilliseconds)
      implicit val askTimeout = Timeout((Constants.slightlyIncreaseTimeout(effectiveTimeoutMilliseconds)).milliseconds)

      metric.toEasyDistance(distanceThreshold) match {
        case Some(easyDistanceThreshold) =>
          (receptionistActor ? GetNeighborsRequest(
            location, k , easyDistanceThreshold, includeData = includeData,
            includeLocation = includeLocation, effectiveTimeoutMilliseconds)).mapTo[GetNeighborsResponse].map(response =>
              response.neighbors.flatMap(neighbor =>
                config.metric.toRealDistance(neighbor.distance).map(realDistance =>
                  Neighbor(neighbor.id, realDistance, neighbor.data, neighbor.location)))
            )
        case None => Future(Vector[Neighbor]())
      }
    } else {
      Future(Vector[Neighbor]())
    }
  }

  private def getNeighborsById(
      locationId: String,
      k: Int,
      distanceThreshold: RealDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Option[Int]): Future[Vector[Neighbor]] = {
    idToCoordinates.get(locationId) match {
      case None => Future(Vector[Neighbor]())
      case Some(location) =>
        getNeighborsByLocation(location, k, distanceThreshold,
            includeData = includeData, includeLocation = includeLocation, timeoutMilliseconds)
    }
  }

  def getHealth(): Future[ClusterHealth] = {
    getStats(doGarbageCollect = false).map(ClusterHealthAnalyzer.analyse(_))
  }

  def getStats(doGarbageCollect: Boolean): Future[ClusterStats] = {
    val timeoutMilliseconds = Constants.statsTimeoutMilliseconds
    implicit val askTimeout = Timeout(Constants.slightlyIncreaseTimeout(timeoutMilliseconds).milliseconds)
    val response = (receptionistActor ? GetClusterStatsRequest(timeoutMilliseconds, doGarbageCollect))
    response.mapTo[GetClusterStatsResponse].map(_.stats)
  }
}
