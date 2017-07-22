package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
import configuration._
import input.PointLoader
import neighbors.NeighborhoodCluster
import neighbors.Point
import web.WebServer
import com.codiply.barrio.configuration.ArgsParser

object Main extends App {
  import neighbors.Point.DistanceMetric
  
  val config: ArgsConfig = ArgsParser.parse(args)
  
  // TODO: get it from the environment variable
  val actorSystem = ActorSystem("barrio")
  
  val pointsLoader = () => PointLoader.fromFile(config.file)
  
  val distance = DistanceMetric.euclidean
  
  val neighborhood = new NeighborhoodCluster(actorSystem, config.algo, pointsLoader, distance)
  
  val webServer = new WebServer(neighborhood)
  // TODO: get the port from the environment
  webServer.startServer("0.0.0.0", 18001, ServerSettings(ConfigFactory.load), Some(actorSystem))
}