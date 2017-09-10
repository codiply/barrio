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
        initialised = false,
        pointsPerLeaf = LongQuantityStatsContainer.empty,
        depth = LongQuantityStatsContainer.empty)
}

final case class TreeStatsContainer(
    initialised: Boolean,
    pointsPerLeaf: LongQuantityStatsContainer,
    depth: LongQuantityStatsContainer) {
  def add(stats: LeafStats): TreeStatsContainer =
    this.copy(
        pointsPerLeaf = pointsPerLeaf.add(stats.pointCount),
        depth = depth.add(stats.depth))

  def setInitialised: TreeStatsContainer =
    this.copy(initialised = true)

  def toStats(): TreeStats =
    TreeStats(
        initialised = initialised,
        leafs = pointsPerLeaf.count,
        points = pointsPerLeaf.sum,
        pointsPerLeaf = pointsPerLeaf.toStats,
        depth = depth.toStats)
}

final case class TreeStats(
  initialised: Boolean,
  leafs: BigInt,
  points: BigInt,
  pointsPerLeaf: LongQuantityStats,
  depth: LongQuantityStats)

final case class NodeStats(
  initialised: Boolean,
  version: String,
  dimensions: Int,
  memory: MemoryStats,
  trees: Map[String, TreeStats])

final case class ClusterStats(initialised: Boolean, nodes: Map[String, NodeStats])
