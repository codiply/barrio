package com.codiply.barrio

object Main extends App {
  var config = ArgsParser.parse(args)
  
  println(config)
  
//  val webServer = new WebServer
//  webServer.startServer("localhost", 18001)
}