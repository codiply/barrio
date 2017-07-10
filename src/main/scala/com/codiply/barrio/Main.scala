package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
import input.PointLoader
import nn.{ NaiveNeighborhood, Point }
import web.WebServer

object Main extends App {
  val config = ArgsParser.parse(args)
  
  val actorSystem = ActorSystem("barrio")
  
  val points = PointLoader.fromFile(config.file)
  
  val distance = (coordinates1: List[Double], coordinates2: List[Double]) =>
    coordinates1.zip(coordinates2).map(x => {
      val diff = x._1 - x._2
      diff * diff
    }).sum
  
  val neighborhood = new NaiveNeighborhood(actorSystem, points, distance)
  
  val webServer = new WebServer(neighborhood)
  webServer.startServer("0.0.0.0", 18001, ServerSettings(ConfigFactory.load), Some(actorSystem))
}