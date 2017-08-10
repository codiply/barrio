package com.codiply.barrio.helpers

import scala.io.Source

import com.codiply.barrio.geometry.Point

object PointLoader {
  def fromCsvFile(fileName: String, dimensions: Int, separator: String): Iterable[Point] = {
    Source.fromFile(fileName)("UTF-8").getLines.toIterable
      .filter { !_.isEmpty() }
      .map { _.split(separator).map(_.trim) }
      .flatMap( pieces => {
        if (pieces.length == dimensions + 1) {
          try {
            val id = pieces(0)
            val location = pieces.drop(1).map(_.toDouble).toList
            Some(Point(id, location))
          } catch {
            case e: Exception =>
              // TODO: log error
              None
          }
        } else {
          None
        }
      })
  }
}
