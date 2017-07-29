package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory

import com.codiply.barrio.helpers.ArgsConfig
import com.codiply.barrio.helpers.ArgsParser
import com.codiply.barrio.helpers.PointLoader
import com.codiply.barrio.neighbors.NeighborhoodCluster
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.web.WebServer

object Main extends App {
  val config: ArgsConfig = ArgsParser.parse(args)

  // TODO: get it from the environment variable
  val actorSystem = ActorSystem("barrio")

  val pointsLoader = () => PointLoader.fromFile(config.file)

  val metric = Metric.euclidean

  val neighborhood = new NeighborhoodCluster(actorSystem, pointsLoader, metric)

  val webServer = new WebServer(neighborhood)
  // TODO: get the port from the environment
  val webServerPort = 18001
  webServer.startServer("0.0.0.0", webServerPort, ServerSettings(ConfigFactory.load), Some(actorSystem))
}
