package com.codiply.barrio.helpers

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object ConfigHelper {
  def get(argsConfig: ArgsConfig): Config = {
    val loadedConfig: Config = ConfigFactory.load()

    val nodeRole = if (argsConfig.seedOnlyNode) NodeRoles.seedOnlyNode else NodeRoles.fullNode

    val akkaSystem = loadedConfig.getString(ConfigKey.akkaSystem)
    val seedsValue = loadedConfig.getString(ConfigKey.akkaSeeds)
      .split(",")
      .map(_.trim)
      .map((seed) => s""""akka.tcp://${akkaSystem}@${seed}"""")
      .mkString("[", ",", "]")

    val programmaticConfig: Config = ConfigFactory.parseString(s"""
      akka {
        cluster {
          roles=[$nodeRole]
          seed-nodes=$seedsValue
        }
      }""")

    programmaticConfig.withFallback(loadedConfig)
  }
}
