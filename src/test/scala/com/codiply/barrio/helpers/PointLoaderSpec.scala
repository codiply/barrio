package com.codiply.barrio.tests.helpers

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.helpers.PointLoader

class PointLoaderSpec extends FlatSpec with MockFactory {
  val separator = "@"
  val coordinateSeparator = ","

  val coordinatesParserNotToBeCalled = (input: String) => {
    assert(false, s"the coordinates parser was used")
    Some(Coordinates())
  }

  "PointLoader.createCoordinatesParser returns a parser that" should
    "return None for an empty string" in {
    val dimensions = 2
    val input = ""
    val parse = PointLoader.createCoordinatesParser(dimensions, coordinateSeparator)
    assert(parse(input) == None)
  }
  it should "return None if less dimensions present" in {
    val dimensions = 2
    val input = "1.0"
    val parse = PointLoader.createCoordinatesParser(dimensions, coordinateSeparator)
    assert(parse(input) == None)
  }
  it should "return None if more dimensions present" in {
    val dimensions = 2
    val input = "1.0, 2.0, 3.0"
    val parse = PointLoader.createCoordinatesParser(dimensions, coordinateSeparator)
    assert(parse(input) == None)
  }
  it should "return the expected result if the right number of dimensions is present" in {
    val dimensions = 2
    val input = "1.0, 2.0"
    val parse = PointLoader.createCoordinatesParser(dimensions, coordinateSeparator)
    val expected = Some(Coordinates(1.0, 2.0))
    assert(parse(input) == expected)
  }

  "PointLoader.createCsvLineParser returns a parser that" should
    "return None for an empty string" in {
    val input = ""

    val parse = PointLoader.createCsvLineParser(separator, coordinatesParserNotToBeCalled)
    assert(parse(input) == None)
  }
  it should "return None for input with a single part" in {
    val input = "some-id"

    val parse = PointLoader.createCsvLineParser(separator, coordinatesParserNotToBeCalled)
    assert(parse(input) == None)
  }
  it should "return None for input with more than 3 parts" in {
    val input = (1 to 4).mkString(separator)

    val parse = PointLoader.createCsvLineParser(separator, coordinatesParserNotToBeCalled)
    assert(parse(input) == None)
  }
  it should "return the expected results for input with id and coordinates" in {
    val id = "some-id"
    val coordinates = Coordinates(1.0,2.0,3.0)
    val coordinatesString = coordinates.mkString(coordinateSeparator)
    val input = s"$id$separator$coordinatesString"

    val mockCoordinatesParser = mockFunction[String, Option[Coordinates]]
    mockCoordinatesParser.expects(coordinatesString).returning(Some(coordinates)).once()

    val parse = PointLoader.createCsvLineParser(separator, mockCoordinatesParser)
    val output = parse(input)

    assert(output.isDefined)
    assert(output.get.id == id)
    assert(output.get.location == coordinates)
    assert(output.get.data == "")
  }
  it should "return the expected results for input with id, coordinates and additional data" in {
    val id = "some-id"
    val coordinates = Coordinates(1.0,2.0,3.0)
    val coordinatesString = coordinates.mkString(coordinateSeparator)
    val data = "some-data"
    val input = s"$id$separator$coordinatesString$separator$data"

    val mockCoordinatesParser = mockFunction[String, Option[Coordinates]]
    mockCoordinatesParser.expects(coordinatesString).returning(Some(coordinates)).once()

    val parse = PointLoader.createCsvLineParser(separator, mockCoordinatesParser)
    val output = parse(input)

    assert(output.isDefined)
    assert(output.get.id == id)
    assert(output.get.location == coordinates)
    assert(output.get.data == data)
  }
  it should "trim whitespace before parsing" in {
    val id = "some-id"
    val coordinates = Coordinates(1.0,2.0,3.0)
    val coordinatesString = coordinates.mkString(coordinateSeparator)
    val data = "some-data"
    val input = s"  $id   $separator $coordinatesString$separator $data  "

    val mockCoordinatesParser = mockFunction[String, Option[Coordinates]]
    mockCoordinatesParser.expects(coordinatesString).returning(Some(coordinates)).once()

    val parse = PointLoader.createCsvLineParser(separator, mockCoordinatesParser)
    val output = parse(input)

    assert(output.isDefined)
    assert(output.get.id == id)
    assert(output.get.location == coordinates)
    assert(output.get.data == data)
  }
}
