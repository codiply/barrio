package com.codiply.barrio.neighbors

import com.typesafe.config.Config

import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.helpers.ArgsConfig
import com.codiply.barrio.helpers.ConfigKey

object NeighborhoodConfig {
  object Defaults {
    val maxRequestTimeoutMilliseconds = 60000
    val defaultRequestTimeoutMilliseconds = 10000
  }

  def apply(argsConfig: ArgsConfig, config: Config): NeighborhoodConfig =
    NeighborhoodConfig(
        defaultRequestTimeoutMilliseconds =
          getInt(config, ConfigKey.maxRequestTimeoutMilliseconds, Defaults.defaultRequestTimeoutMilliseconds),
        dimensions = argsConfig.dimensions,
        maxPointsPerLeaf = argsConfig.maxPointsPerLeaf,
        maxRequestTimeoutMilliseconds =
          getInt(config, ConfigKey.maxRequestTimeoutMilliseconds, Defaults.maxRequestTimeoutMilliseconds),
        // TODO: set via command line argument
        metric = Metric.euclidean,
        nodeName = config.getString(ConfigKey.hostname),
        treesPerNode = argsConfig.treesPerNode)

  def getInt(config: Config, key: String, default: Int): Int = {
    try {
      config.getInt(key)
    } catch {
      case e: Exception => default
    }
  }
}

case class NeighborhoodConfig(
    defaultRequestTimeoutMilliseconds: Int,
    dimensions: Int,
    maxPointsPerLeaf: Int,
    maxRequestTimeoutMilliseconds: Int,
    metric: Metric,
    nodeName: String,
    treesPerNode: Int)
