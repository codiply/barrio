package com.codiply.barrio.neighbors

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.pattern.ask
import akka.routing.BroadcastGroup
import akka.util.Timeout
import com.codiply.barrio.configuration.SearchAlgorithmEnum
import Point._

class NeighborhoodCluster (
    actorSystem: ActorSystem,
    searchAlgorithm: SearchAlgorithmEnum.Value,
    pointsLoader: () => Iterable[Point],
    distance: DistanceMetric) extends NeighborProvider {
  import ActorProtocol._
  import linear.NeighborhoodPatchActor
  import forests.NeighborhoodForestActor
  
  val points = pointsLoader().toList
  
  val timeout: FiniteDuration = 5 seconds 
  
  implicit val askTimeout = Timeout(timeout)
  import actorSystem.dispatcher
  
  val nodeActorProps = searchAlgorithm match {
    case SearchAlgorithmEnum.Linear => NeighborhoodPatchActor.props(points, distance)
    // TODO: Take the number of trees from configuration
    case SearchAlgorithmEnum.Forest => NeighborhoodForestActor.props(points, distance, 3, timeout)
  }
      
  val nodeActor = actorSystem.actorOf(nodeActorProps, "neighborhood-node")
  
  val nodeActorRouter = actorSystem.actorOf(
      ClusterRouterGroup(
          BroadcastGroup(List("/user/neighborhood-node")), 
          ClusterRouterGroupSettings(
              totalInstances = 1000000, 
              routeesPaths = List("/user/neighborhood-node"),
              allowLocalRoutees = true, 
              useRole = None)).props(), name = "neighborhood-node-router")
  
  val receptionistActor = actorSystem.actorOf(
      NeighborhoodReceptionistActor.props(nodeActorRouter, distance, timeout), "receptionist") 
      
  def getNeighbors(coordinates: List[Double], k: Int): Future[List[Point]] = {
    (receptionistActor ? GetNeighborsRequest(coordinates, k)).mapTo[GetNeighborsResponse].map(_.neighbors)
  }
}