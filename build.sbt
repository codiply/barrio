name := "Barrio"
organization := "com.codiply"
version := "0.0.1"

scalaVersion := "2.11.11"

libraryDependencies ++= { 
  val akkaVersion = "2.4.19" 
  val akkaHttpVersion = "10.0.9"
  
  Seq(
    "com.github.scopt" %% "scopt" % "3.6.0",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  )
}

assemblyOutputPath in assembly := file("target/barrio/barrio.jar")
mainClass in assembly := Some("com.codiply.barrio.Main")