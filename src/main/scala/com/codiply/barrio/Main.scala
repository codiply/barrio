package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings

import com.codiply.barrio.helpers.ArgsParser
import com.codiply.barrio.helpers.ConfigHelper
import com.codiply.barrio.helpers.ConfigKey
import com.codiply.barrio.helpers.PointLoader
import com.codiply.barrio.helpers.Random
import com.codiply.barrio.neighbors.NeighborhoodCluster
import com.codiply.barrio.neighbors.NeighborhoodConfig
import com.codiply.barrio.web.SeedWebServer
import com.codiply.barrio.web.WebServer

object Main extends App {
  ArgsParser.parse(args) match {
    case Some(argsConfig) =>
      val config = ConfigHelper.get(argsConfig)
      val actorSystem = ActorSystem(config.getString(ConfigKey.akkaSystem), config)

      val webServer = if (!argsConfig.seedOnlyNode) {
        val pointsLoader =
          if (argsConfig.isUrl) {
            () => PointLoader.fromCsvUrl(argsConfig.file, argsConfig.dimensions,
              separator = argsConfig.separator, coordinateSeparator = argsConfig.coordinateSeparator)
          } else {
            () => PointLoader.fromCsvFile(argsConfig.file, argsConfig.dimensions,
              separator = argsConfig.separator, coordinateSeparator = argsConfig.coordinateSeparator)
          }

        val random = argsConfig.randomSeed match {
          case Some(seed) => Random(seed)
          case None => Random()
        }

        val neighborhoodConfig = NeighborhoodConfig(argsConfig, config)
        val neighborhood = new NeighborhoodCluster(actorSystem, pointsLoader, neighborhoodConfig, random)

        new WebServer(neighborhood)
      } else {
        new SeedWebServer()
      }
      val webServerHost = "0.0.0.0"
      val webServerPort = config.getInt(ConfigKey.webApiPort)

      webServer.startServer(webServerHost, webServerPort, ServerSettings(config), Some(actorSystem))
    case None => ()
  }
}
