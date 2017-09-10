package com.codiply.barrio.web

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes

import com.codiply.barrio.neighbors.Errors._

object ErrorMapper {
  case class Error(status: StatusCode, message: String)

  def map(error: NeighborsRequestError): Error = {
    error match {
      case InvalidDistanceThreshold =>
        Error(StatusCodes.BadRequest, "Invalid distance threshold")
      case e: InvalidDimensions =>
        Error(StatusCodes.BadRequest, s"Invalid dimensions. Expected ${e.expected} but got ${e.actual}.")
      case BothLocationAndLocationIdDefined =>
        Error(StatusCodes.BadRequest, "Both location and locationId defined in request")
      case UnkownLocation =>
        Error(StatusCodes.BadRequest, "Location not found")
    }
  }
}
