package com.codiply.barrio.neighbors

import com.typesafe.config.Config

import com.codiply.barrio.helpers.ArgsConfig
import com.codiply.barrio.helpers.ConfigKey

object NeighborhoodConfig {
  def apply(argsConfig: ArgsConfig, config: Config): NeighborhoodConfig =
    NeighborhoodConfig(
        nodeName = config.getString(ConfigKey.hostname),
        dimensions = argsConfig.dimensions,
        treesPerNode = argsConfig.treesPerNode)
}

case class NeighborhoodConfig(
    nodeName: String,
    dimensions: Int,
    treesPerNode: Int)
