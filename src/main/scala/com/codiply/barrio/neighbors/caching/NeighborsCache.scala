package com.codiply.barrio.neighbors.caching

import com.codiply.barrio.helpers.CacheWrapper
import com.codiply.barrio.neighbors.ActorProtocol.GetNeighborsResponse

object Types {
  type NeighborsCache = CacheWrapper[String, GetNeighborsResponse]
}
