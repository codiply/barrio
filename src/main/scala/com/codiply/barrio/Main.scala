package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
import input.PointLoader
import neighbors.NeighborhoodCluster
import neighbors.Point
import web.WebServer

object Main extends App {
  import neighbors.Point.DistanceMetric
  
  val config = ArgsParser.parse(args)
  
  val actorSystem = ActorSystem("barrio")
  
  val pointsLoader = () => PointLoader.fromFile(config.file)
  
  val distance = DistanceMetric.euclidean
  
  val neighborhood = new NeighborhoodCluster(actorSystem, pointsLoader, distance)
  
  val webServer = new WebServer(neighborhood)
  webServer.startServer("0.0.0.0", 18001, ServerSettings(ConfigFactory.load), Some(actorSystem))
}