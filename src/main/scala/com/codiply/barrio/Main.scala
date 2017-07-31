package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import com.codiply.barrio.helpers.ArgsConfig
import com.codiply.barrio.helpers.ArgsParser
import com.codiply.barrio.helpers.ConfigKey
import com.codiply.barrio.helpers.PointLoader
import com.codiply.barrio.helpers.Random
import com.codiply.barrio.neighbors.NeighborhoodCluster
import com.codiply.barrio.neighbors.NeighborhoodConfig
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.web.WebServer

object Main extends App {
  ArgsParser.parse(args) match {
    case Some(argsConfig) =>
      val config: Config = ConfigFactory.load()

      val actorSystem = ActorSystem(config.getString(ConfigKey.akkaSystem))

      val pointsLoader = () => PointLoader.fromCsvFile(argsConfig.file, argsConfig.dimensions)

      val random = argsConfig.randomSeed match {
        case Some(seed) => Random(seed)
        case None => Random()
      }

      val neighborhoodConfig = NeighborhoodConfig(argsConfig, config)
      val neighborhood = new NeighborhoodCluster(actorSystem, pointsLoader, neighborhoodConfig, random)

      val webServer = new WebServer(neighborhood)

      val webServerHost = "0.0.0.0"
      val webServerPort = config.getInt(ConfigKey.webApiPort)

      webServer.startServer(webServerHost, webServerPort, ServerSettings(config), Some(actorSystem))
    case None => ()
  }
}
