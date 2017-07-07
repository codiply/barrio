package com.codiply.barrio

object Main extends App {
  val webServer = new WebServer
  webServer.startServer("localhost", 18001)
}