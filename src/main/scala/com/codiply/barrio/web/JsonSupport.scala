package com.codiply.barrio.web

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object JsonSupport {
  import com.codiply.barrio.helpers.LongQuantityStats
  import com.codiply.barrio.neighbors.ClusterHealth
  import com.codiply.barrio.neighbors.ClusterStats
  import com.codiply.barrio.neighbors.MemoryStats
  import com.codiply.barrio.neighbors.NodeStats
  import com.codiply.barrio.neighbors.TreeStats

  final case class NeighborJson(id: String, distance: Double, data: Option[String], location: Option[List[Double]])
  final case class NeighborsRequestJson(
      k: Int,
      location: Option[List[Double]],
      locationId: Option[String],
      distanceThreshold: Option[Double],
      includeData: Option[Boolean],
      includeLocation: Option[Boolean],
      timeout: Option[Int])
  final case class NeighborsResponseJson(count: Int, neighbors: Vector[NeighborJson])

  object Mapping {
    def mapMemoryStats(stats: MemoryStats): MemoryStatsJson =
      MemoryStatsJson(
        freeMemoryMB = stats.freeMemoryMB,
        totalMemoryMB = stats.totalMemoryMB,
        maxMemoryMB = stats.maxMemoryMB,
        usedMemoryMB = stats.usedMemoryMB)

    def mapIntegerQuantityStats(stats: LongQuantityStats): LongQuantityStatsJson =
      LongQuantityStatsJson(
          min = stats.min,
          max = stats.max,
          mean = stats.mean,
          standardError = stats.standardError)

    def mapTreeStats(stats: TreeStats): TreeStatsJson =
      TreeStatsJson(
        leafs = stats.leafs,
        points = stats.points,
        pointsPerLeaf = mapIntegerQuantityStats(stats.pointsPerLeaf),
        depth = mapIntegerQuantityStats(stats.depth))

    def mapNodeStats(stats: NodeStats): NodeStatsJson =
      NodeStatsJson(
        version = stats.version,
        dimensions = stats.dimensions,
        memory = mapMemoryStats(stats.memory),
        trees = stats.trees.mapValues(mapTreeStats))

    def mapClusterStats(stats: ClusterStats): ClusterStatsJson =
      ClusterStatsJson(nodes = stats.nodes.mapValues(mapNodeStats))

    def mapClusterHealth(health: ClusterHealth): ClusterHealthJson =
      ClusterHealthJson(errors = health.errors)
  }

  final case class MemoryStatsJson(
    freeMemoryMB: Double,
    totalMemoryMB: Double,
    maxMemoryMB: Double,
    usedMemoryMB: Double)

  final case class LongQuantityStatsJson(
    min: Long,
    max: Long,
    mean: Double,
    standardError: Double)

  final case class TreeStatsJson(
    leafs: BigInt,
    points: BigInt,
    pointsPerLeaf: LongQuantityStatsJson,
    depth: LongQuantityStatsJson)

  final case class NodeStatsJson(
    version: String,
    dimensions: Int,
    memory: MemoryStatsJson,
    trees: Map[String, TreeStatsJson])

  final case class ClusterStatsJson(nodes: Map[String, NodeStatsJson])

  final case class ClusterHealthJson(errors: List[String])
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  import JsonSupport._

  implicit val neighborFormat = jsonFormat4(NeighborJson)
  implicit val neighborsRequestFormat = jsonFormat7(NeighborsRequestJson)
  implicit val neighborsResponseFormat = jsonFormat2(NeighborsResponseJson)
  implicit val memoryStatsFormat = jsonFormat4(MemoryStatsJson)
  implicit val longQuantityStatsFormat = jsonFormat4(LongQuantityStatsJson)
  implicit val treeStatsFormat = jsonFormat4(TreeStatsJson)
  implicit val nodeStatsFormat = jsonFormat4(NodeStatsJson)
  implicit val clusterStatsFormat = jsonFormat1(ClusterStatsJson)
  implicit val clusterHealthFormat = jsonFormat1(ClusterHealthJson)
}
