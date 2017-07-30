package com.codiply.barrio.neighbors

final case class MemoryStats(
  freeMemoryMB: Double,
  totalMemoryMB: Double,
  maxMemoryMB: Double,
  usedMemoryMB: Double)

final case class TreeStats(
  leafs: Int,
  points: Int,
  minLeafPoints: Int,
  meanLeafPoints: Double,
  maxLeafPoints: Int,
  minDepth: Int,
  meanDepth: Double,
  maxDepth: Int)

final case class NodeStats(
  memory: MemoryStats,
  trees: Map[String, TreeStats])

final case class ClusterStats(nodes: Map[String, NodeStats])
