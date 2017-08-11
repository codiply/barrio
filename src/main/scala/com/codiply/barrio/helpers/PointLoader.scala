package com.codiply.barrio.helpers

import scala.io.Source

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates

object PointLoader {
  def fromCsvFile(
      fileName: String,
      dimensions: Int,
      idSeparator: String,
      coordinateSeparator: String): Seq[Point] = {
    fromCsvLines(Source.fromFile(fileName)("UTF-8").getLines.toSeq,
        dimensions, idSeparator = idSeparator, coordinateSeparator = coordinateSeparator)
  }

  def fromCsvLines(
      lines: Seq[String],
      dimensions: Int,
      idSeparator: String,
      coordinateSeparator: String): Seq[Point] = {
    val parseCoordinates = createCoordinatesParser(dimensions, coordinateSeparator)
    val parseCsvLine = createCsvLineParser(idSeparator, parseCoordinates)
    lines.filter { !_.isEmpty() }.flatMap(parseCsvLine(_))
  }

  def createCsvLineParser(
      idSeparator: String,
      coordinatesParser: String => Option[Coordinates]): String => Option[Point] = {
    (line: String) => {
      val pieces = line.split(idSeparator).map(_.trim)
      if (pieces.length == 2) {
        val id = pieces(0)
        coordinatesParser(pieces(1)).map { Point(id, _) }
      } else {
        None
      }
    }
  }

  def createCoordinatesParser(dimensions: Int, separator: String): String => Option[Coordinates] = {
    (input: String) => {
      val pieces = input.split(separator)
      if (pieces.size == dimensions) {
        try {
          Some(pieces.map(_.toDouble).toList)
        } catch {
          case e: Exception =>
            // TODO: log error
            None
        }
      } else {
        None
      }
    }
  }
}
