package com.codiply.barrio.neighbors

import com.codiply.barrio.helpers.LongQuantityStats
import com.codiply.barrio.helpers.LongQuantityStatsContainer

final case class MemoryStats(
  freeMemoryMB: Double,
  totalMemoryMB: Double,
  maxMemoryMB: Double,
  usedMemoryMB: Double)

final case class LeafStats(
    depth: Int,
    pointCount: Int)

object TreeStatsContainer {
  val empty: TreeStatsContainer =
    TreeStatsContainer(
        pointsPerLeaf = LongQuantityStatsContainer.empty,
        depth = LongQuantityStatsContainer.empty)
}

final case class TreeStatsContainer(
    pointsPerLeaf: LongQuantityStatsContainer,
    depth: LongQuantityStatsContainer) {
  def add(stats: LeafStats): TreeStatsContainer =
    TreeStatsContainer(
        pointsPerLeaf = pointsPerLeaf.add(stats.pointCount),
        depth = depth.add(stats.depth))

  def toStats(): TreeStats =
    TreeStats(
        leafs = pointsPerLeaf.count,
        points = pointsPerLeaf.sum,
        pointsPerLeaf = pointsPerLeaf.toStats,
        depth = depth.toStats)
}

final case class TreeStats(
  leafs: BigInt,
  points: BigInt,
  pointsPerLeaf: LongQuantityStats,
  depth: LongQuantityStats)

final case class NodeStats(
  memory: MemoryStats,
  trees: Map[String, TreeStats])

final case class ClusterStats(nodes: Map[String, NodeStats])
