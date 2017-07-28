package com.codiply.barrio.helpers

import scala.io.Source
import com.codiply.barrio.neighbors.Point

object PointLoader {
  def fromFile(fileName: String): Iterable[Point] = {
    Source.fromFile(fileName).getLines.toIterable
      .filter { !_.isEmpty() }
      .map { _.split(",").map(_.trim) }
      .map ( pieces => {
        val id = pieces(0)
        val coordinates = pieces.drop(1).map(_.toDouble).toList
        Point(id, coordinates)
      })
  }
}
