package com.codiply.barrio.test

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object TestKitConfig {
  val config: Config = ConfigFactory.parseString("""
    akka {
      loglevel = "WARNING"
    }
    """)
}
