package com.codiply.barrio.neighbors.caching

import com.codiply.barrio.neighbors.ActorProtocol.GetNeighborsResponse

object NeighborsCacheActorProtocol {
  case class CacheNeighborsRequest(locationId: String, response: GetNeighborsResponse)
  case class GetCachedNeighborsRequest(locationId: String, k: Int)
  case class GetCachedNeighborsResponse(response: Option[GetNeighborsResponse])
}
