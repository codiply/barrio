package com.codiply.barrio.neighbors

object Errors {
  sealed abstract class NeighborsRequestError

  case object InvalidDistanceThreshold extends NeighborsRequestError
  case class InvalidDimensions(actual: Int, expected: Int) extends NeighborsRequestError
  case object UnkownLocation extends NeighborsRequestError
  case object BothLocationAndLocationIdDefined extends NeighborsRequestError
}
