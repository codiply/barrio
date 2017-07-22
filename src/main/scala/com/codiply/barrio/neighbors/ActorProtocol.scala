package com.codiply.barrio.neighbors

import Point._

object ActorProtocol {
  final case class GetNeighborsRequest(coordinates: List[Double], k: Int)
  final case class GetNeighborsResponse(neighbors: List[Point])
}