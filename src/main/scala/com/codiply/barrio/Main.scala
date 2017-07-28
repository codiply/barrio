package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
import helpers.PointLoader
import helpers.ArgsConfig
import helpers.ArgsParser
import neighbors.NeighborhoodCluster
import neighbors.Point
import web.WebServer

object Main extends App {
  import neighbors.Point.DistanceMetric

  val config: ArgsConfig = ArgsParser.parse(args)

  // TODO: get it from the environment variable
  val actorSystem = ActorSystem("barrio")

  val pointsLoader = () => PointLoader.fromFile(config.file)

  val metricSpace = DistanceMetric.euclidean

  val neighborhood = new NeighborhoodCluster(actorSystem, pointsLoader, metricSpace)

  val webServer = new WebServer(neighborhood)
  // TODO: get the port from the environment
  val webServerPort = 18001
  webServer.startServer("0.0.0.0", webServerPort, ServerSettings(ConfigFactory.load), Some(actorSystem))
}
