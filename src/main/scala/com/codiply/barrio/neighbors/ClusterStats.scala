package com.codiply.barrio.neighbors

final case class TreeStats(
    minDepth: Int,
    maxDepth: Int)

final case class NodeStats(
    freeMemoryMB: Double,
    totalMemoryMB: Double,
    maxMemoryMB: Double,
    usedMemoryMB: Double,
    treeStats: List[TreeStats])
    
final case class ClusterStats(nodeStats: List[NodeStats])
