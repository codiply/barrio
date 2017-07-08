package com.codiply.barrio

import input.PointLoader
import nn.{ NaiveNeighborhood, Point }
import web.WebServer

object Main extends App {
  val config = ArgsParser.parse(args)
  
  val points = PointLoader.fromFile(config.file)
  
  val distance = (coordinates1: List[Double], coordinates2: List[Double]) =>
    coordinates1.zip(coordinates2).map(x => {
      val diff = x._1 - x._2
      diff * diff
    }).sum
  
  val neighborhood = new NaiveNeighborhood(points, distance)
  
  val webServer = new WebServer(neighborhood)
  webServer.startServer("0.0.0.0", 18001)
}