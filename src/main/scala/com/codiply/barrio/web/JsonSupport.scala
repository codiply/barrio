package com.codiply.barrio.web

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object JsonSupport {
  import com.codiply.barrio.neighbors.ClusterStats
  import com.codiply.barrio.neighbors.MemoryStats
  import com.codiply.barrio.neighbors.NodeStats
  import com.codiply.barrio.neighbors.TreeStats

  final case class NeighborJson(id: String, coordinates: List[Double])
  final case class NeighborsRequestJson(k: Int, coordinates: List[Double], distanceThreshold: Double)
  final case class NeighborsResponseJson(neighbors: List[NeighborJson])

  object Mapping {
    def mapMemoryStats(stats: MemoryStats): MemoryStatsJson =
      MemoryStatsJson(
        freeMemoryMB = stats.freeMemoryMB,
        totalMemoryMB = stats.totalMemoryMB,
        maxMemoryMB = stats.maxMemoryMB,
        usedMemoryMB = stats.usedMemoryMB)

    def mapTreeStats(stats: TreeStats): TreeStatsJson =
      TreeStatsJson(
        leafs = stats.leafs,
        points = stats.points,
        minLeafPoints = stats.minLeafPoints,
        meanLeafPoints = stats.meanLeafPoints,
        maxLeafPoints = stats.maxLeafPoints,
        minDepth = stats.minDepth,
        meanDepth = stats.meanDepth,
        maxDepth = stats.maxDepth)

    def mapNodeStats(stats: NodeStats): NodeStatsJson =
      NodeStatsJson(
        memory = mapMemoryStats(stats.memory),
        trees = stats.trees.mapValues(mapTreeStats))

    def mapClusterStats(stats: ClusterStats): ClusterStatsJson =
      ClusterStatsJson(nodes = stats.nodes.mapValues(mapNodeStats))
  }

  final case class MemoryStatsJson(
    freeMemoryMB: Double,
    totalMemoryMB: Double,
    maxMemoryMB: Double,
    usedMemoryMB: Double)

  final case class TreeStatsJson(
    leafs: Int,
    points: Int,
    minLeafPoints: Int,
    meanLeafPoints: Double,
    maxLeafPoints: Int,
    minDepth: Int,
    meanDepth: Double,
    maxDepth: Int)

  final case class NodeStatsJson(
    memory: MemoryStatsJson,
    trees: Map[String, TreeStatsJson])

  final case class ClusterStatsJson(nodes: Map[String, NodeStatsJson])
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import JsonSupport._

  implicit val neighborFormat = jsonFormat2(NeighborJson)
  implicit val neighborsRequestFormat = jsonFormat3(NeighborsRequestJson)
  implicit val neighborsResponseFormat = jsonFormat1(NeighborsResponseJson)
  implicit val memoryStatsFormat = jsonFormat4(MemoryStatsJson)
  implicit val treeStatsFormat = jsonFormat8(TreeStatsJson)
  implicit val nodeStatsFormat = jsonFormat2(NodeStatsJson)
  implicit val clusterStatsFormat = jsonFormat1(ClusterStatsJson)
}
