package com.codiply.barrio.neighbors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.pattern.ask
import akka.routing.BroadcastGroup
import akka.util.Timeout

import com.codiply.barrio.helpers.Constants
import com.codiply.barrio.helpers.NodeRoles
import com.codiply.barrio.helpers.RandomProvider
import com.codiply.barrio.helpers.ScaffeineWrapper
import com.codiply.barrio.helpers.TimeStamp
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.geometry.RealDistance
import com.codiply.barrio.neighbors.caching.Types.NeighborsCache
import com.codiply.barrio.neighbors.Errors._

object NeighborhoodCluster {
  val locationIndexActorNamePrefix = "location-index-"
  val nodeActorNamePrefix = "neighborhood-node-"
}

class NeighborhoodCluster (
    actorSystem: ActorSystem,
    pointsLoader: () => Iterable[Point],
    config: NeighborhoodConfig,
    random: RandomProvider) extends NeighborProvider {
  import ActorProtocol.GetClusterStatsRequest
  import ActorProtocol.GetClusterStatsResponse
  import ActorProtocol.GetNeighborsRequest
  import ActorProtocol.GetNeighborsRequestByLocationId
  import ActorProtocol.GetNeighborsRequestByLocation
  import ActorProtocol.GetNeighborsResponse
  import NeighborhoodCluster._
  import com.codiply.barrio.neighbors.forests.NeighborhoodForestActor
  import com.codiply.barrio.neighbors.caching.NeighborsCacheReaderActor
  import com.codiply.barrio.neighbors.caching.NeighborsCacheWriterActor

  val points = pointsLoader().toList
  val metric = config.metric

  val locationIndexActor = actorSystem.actorOf(
      LocationIndexActor.props(points), locationIndexActorNamePrefix + config.nodeName)

  import actorSystem.dispatcher

  val nodeActor = actorSystem.actorOf(
      NeighborhoodForestActor.props(config.nodeName, points, config, random),
      nodeActorNamePrefix + config.nodeName)

  val locationIndexActorRouter = createBroadcastRouter(actorSystem, locationIndexActorNamePrefix)
  val nodeActorRouter = createBroadcastRouter(actorSystem, nodeActorNamePrefix)

  val receptionistActor =
      if (config.cache) {
        val cache: NeighborsCache = new ScaffeineWrapper[String, GetNeighborsResponse](
            Some(config.cacheConfig.expirationAfterAccessSeconds.seconds),
            Some(config.cacheConfig.maximumSize))
        val neighborsCacheReader = actorSystem.actorOf(
            NeighborsCacheReaderActor.props(cache), "neighbors-cache-reader")
        val neighborsCacheWriter = actorSystem.actorOf(
            NeighborsCacheWriterActor.props(cache), "neighbors-cache-writer")
        actorSystem.actorOf(
          NeighborhoodReceptionistCachingActor.props(
              locationIndexActorRouter, nodeActorRouter, neighborsCacheReader, neighborsCacheWriter),
              "receptionist-cache")
      } else {
        actorSystem.actorOf(
          NeighborhoodReceptionistActor.props(locationIndexActorRouter, nodeActorRouter), "receptionist")
      }

  def getNeighbors(
    location: Option[Seq[Double]],
    locationId: Option[String],
    k: Int,
    distanceThreshold: Option[RealDistance],
    includeData: Boolean,
    includeLocation: Boolean,
    timeoutMilliseconds: Option[Long]): Future[Either[NeighborsRequestError, NeighborsResponse]] = {
    val effectiveTimeoutMilliseconds = config.getEffectiveTimeoutMilliseconds(timeoutMilliseconds)
    val effectiveDistanceThreshold = distanceThreshold.getOrElse(RealDistance.zero)
    (location, locationId) match {
      case (Some(location), None) => getNeighborsByLocation(
        location, k, effectiveDistanceThreshold,
        includeData = includeData, includeLocation = includeLocation, effectiveTimeoutMilliseconds)
      case (None, Some(locationId)) => getNeighborsByLocationId(
        locationId, k, effectiveDistanceThreshold,
        includeData = includeData, includeLocation = includeLocation, effectiveTimeoutMilliseconds)
      case _ => Future(Left(BothLocationAndLocationIdDefined))
    }
  }

  private def createBroadcastRouter(actorSystem: ActorSystem, nodeNamePrefix: String): ActorRef = {
    actorSystem.actorOf(
      ClusterRouterGroup(
          BroadcastGroup(List("/user/" + nodeNamePrefix + "*")),
          ClusterRouterGroupSettings(
              totalInstances = Int.MaxValue,
              routeesPaths = List("/user/" + nodeNamePrefix + "*"),
              allowLocalRoutees = true,
              useRole = Some(NodeRoles.fullNode))).props(), name = "router-to-" + nodeNamePrefix)
  }

  private def getNeighborsByLocation(
      location: Seq[Double],
      k: Int,
      distanceThreshold: RealDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Long): Future[Either[NeighborsRequestError, NeighborsResponse]] = {
    if (location.length == config.dimensions) {
      metric.toEasyDistance(distanceThreshold) match {
        case Some(easyDistanceThreshold) => {
          val timeoutOn = TimeStamp.fromMillisFromNow(timeoutMilliseconds)
          val request = GetNeighborsRequestByLocation(
            Coordinates(location: _*), k , easyDistanceThreshold, includeData = includeData,
            includeLocation = includeLocation, timeoutOn)
          getNeighborsWithRequest(request, timeoutMilliseconds).map(response => Right(response))
        }
        case None => Future(Left(InvalidDistanceThreshold))
      }
    } else {
      Future(Left(InvalidDimensions(actual = location.length, expected = config.dimensions)))
    }
  }

  private def getNeighborsByLocationId(
      locationId: String,
      k: Int,
      distanceThreshold: RealDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Long): Future[Either[NeighborsRequestError, NeighborsResponse]] = {
    metric.toEasyDistance(distanceThreshold) match {
      case Some(easyDistanceThreshold) => {
        val timeoutOn = TimeStamp.fromMillisFromNow(timeoutMilliseconds)
        val request = GetNeighborsRequestByLocationId(
          locationId, k , easyDistanceThreshold, includeData = includeData,
          includeLocation = includeLocation, timeoutOn)
        getNeighborsWithRequest(request, timeoutMilliseconds).map(response => Right(response))
      }
      case None => Future(Left(InvalidDistanceThreshold))
    }
  }

  private def getNeighborsWithRequest(
      request: GetNeighborsRequest,
      timeoutMilliseconds: Long): Future[NeighborsResponse] = {
    implicit val askTimeout = Timeout((Constants.considerablyIncreaseTimeout(timeoutMilliseconds, 2)).milliseconds)
    (receptionistActor ? request).mapTo[GetNeighborsResponse].map(response =>
      NeighborsResponse(
        timeoutReached = response.timeoutReached,
        neighbors = response.neighbors.flatMap(neighbor =>
        config.metric.toRealDistance(neighbor.distance).map(realDistance =>
          Neighbor(neighbor.id, realDistance, neighbor.data, neighbor.location)))))
  }

  def getHealth(): Future[ClusterHealth] = {
    getStats(doGarbageCollect = false).map(ClusterHealthAnalyzer.analyse(_))
  }

  def getStats(doGarbageCollect: Boolean): Future[ClusterStats] = {
    val timeoutMilliseconds = Constants.statsTimeoutMilliseconds
    implicit val askTimeout = Timeout(Constants.considerablyIncreaseTimeout(timeoutMilliseconds).milliseconds)
    val response = (receptionistActor ? GetClusterStatsRequest(timeoutMilliseconds, doGarbageCollect))
    response.mapTo[GetClusterStatsResponse].map(_.stats)
  }
}
