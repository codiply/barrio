package com.codiply.barrio.helpers

import scala.io.Source

import awscala.s3.S3
import awscala.Region

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.helpers.DataSourceType._

object PointLoader {
  def getLoader(config: ArgsConfig): () => Iterable[Point] = {
    config.dataSourceType match {
      case WebDataSource =>
        () =>
          fromCsvUrl(config.file, config.dimensions, encoding = config.encoding,
            separator = config.separator, coordinateSeparator = config.coordinateSeparator)
      case LocalDataSource =>
        () =>
          fromCsvFile(config.file, config.dimensions,encoding = config.encoding,
            separator = config.separator, coordinateSeparator = config.coordinateSeparator)
      case S3DataSource =>
        () =>
          config.s3Bucket match {
            case Some(bucket) => fromCsvOnS3(bucket = bucket, key = config.file, dimensions = config.dimensions,
              encoding = config.encoding, separator = config.separator, coordinateSeparator = config.coordinateSeparator)
            case None => Set.empty
          }
    }
  }

  def fromCsvFile(
      fileName: String,
      dimensions: Int,
      encoding: String,
      separator: String,
      coordinateSeparator: String): Seq[Point] = {
    fromCsvLines(Source.fromFile(fileName)(encoding).getLines.toSeq,
        dimensions, separator = separator, coordinateSeparator = coordinateSeparator)
  }

  def fromCsvUrl(
      url: String,
      dimensions: Int,
      encoding: String,
      separator: String,
      coordinateSeparator: String): Seq[Point] = {
    fromCsvLines(Source.fromURL(url)(encoding).getLines.toSeq,
        dimensions, separator = separator, coordinateSeparator = coordinateSeparator)
  }

  def fromCsvOnS3(
      bucket: String,
      key: String,
      dimensions: Int,
      encoding: String,
      separator: String,
      coordinateSeparator: String): Seq[Point] = {
    implicit val s3 = S3.at(Region.Ireland)
    val s3Object = s3.getObject(bucket, key)
    val lines = Source.fromInputStream(s3Object.getObjectContent())(encoding).getLines.toSeq
    fromCsvLines(lines, dimensions, separator = separator, coordinateSeparator = coordinateSeparator)
  }

  def fromCsvLines(
      lines: Seq[String],
      dimensions: Int,
      separator: String,
      coordinateSeparator: String): Seq[Point] = {
    val parseCoordinates = createCoordinatesParser(dimensions, coordinateSeparator)
    val parseCsvLine = createCsvLineParser(separator, parseCoordinates)
    lines.filter { !_.isEmpty() }.flatMap(parseCsvLine(_))
  }

  def createCsvLineParser(
      separator: String,
      coordinatesParser: String => Option[Coordinates]): String => Option[Point] = {
    (line: String) => {
      val pieces = line.split(separator).map(_.trim)
      if (pieces.length == 2 || pieces.length == 3) {
        val id = pieces(0)
        val data = if (pieces.length == 3) pieces(2) else ""
        coordinatesParser(pieces(1)).map { Point(id, _, data) }
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
          Some(Coordinates(pieces.map(_.toDouble): _*))
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
