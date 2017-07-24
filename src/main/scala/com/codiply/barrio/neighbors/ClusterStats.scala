package com.codiply.barrio.neighbors


final case class NodeStats(
    freeMemoryMB: Double,
    totalMemoryMB: Double,
    maxMemoryMB: Double,
    usedMemoryMB: Double)
    
final case class ClusterStats(nodeStats: List[NodeStats])
