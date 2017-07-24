package com.codiply.barrio.web

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
  
final case class NeighborsRequest(k: Int, coordinates: List[Double])
final case class Neighbor(id: String, coordinates: List[Double])
final case class NeighborsResponse(neighbors: List[Neighbor])

final case class TreeStatsJson(
    minDepth: Int,
    maxDepth: Int)

final case class NodeStatsJson(
    freeMemoryMB: Double,
    totalMemoryMB: Double,
    maxMemoryMB: Double,
    usedMemoryMB: Double,
    treeStats: List[TreeStatsJson])
    
final case class ClusterStatsJson(nodeStats: List[NodeStatsJson])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val neighborsRequestFormat = jsonFormat2(NeighborsRequest)
  implicit val pointFormat = jsonFormat2(Neighbor)
  implicit val neighborsResponseFormat = jsonFormat1(NeighborsResponse)
  implicit val treeStatsJson = jsonFormat2(TreeStatsJson)
  implicit val nodeStatsJson = jsonFormat5(NodeStatsJson)
  implicit val clusterStatsJson = jsonFormat1(ClusterStatsJson)
}
