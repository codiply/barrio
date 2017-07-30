package com.codiply.barrio

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory

import com.codiply.barrio.helpers.ArgsConfig
import com.codiply.barrio.helpers.ArgsParser
import com.codiply.barrio.helpers.PointLoader
import com.codiply.barrio.neighbors.NeighborhoodCluster
import com.codiply.barrio.neighbors.NeighborhoodConfig
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.web.WebServer

object Main extends App {
  ArgsParser.parse(args) match {
    case Some(argsConfig) =>
      val config = ConfigFactory.load()

      val actorSystem = ActorSystem(config.getString("barrio.akka-system"))

      val pointsLoader = () => PointLoader.fromCsvFile(argsConfig.file, argsConfig.dimensions)

      val metric = Metric.euclidean

      val neighborhood = new NeighborhoodCluster(actorSystem, pointsLoader, NeighborhoodConfig(argsConfig), metric)

      val webServer = new WebServer(neighborhood)

      val webServerHost = "0.0.0.0"
      val webServerPort = config.getInt("barrio.web-api-port")

      webServer.startServer(webServerHost, webServerPort, ServerSettings(config), Some(actorSystem))
    case None => ()
  }
}
