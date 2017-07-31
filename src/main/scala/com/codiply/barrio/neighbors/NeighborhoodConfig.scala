package com.codiply.barrio.neighbors

import com.typesafe.config.Config

import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.helpers.ArgsConfig
import com.codiply.barrio.helpers.ConfigKey

object NeighborhoodConfig {
  def apply(argsConfig: ArgsConfig, config: Config): NeighborhoodConfig =
    NeighborhoodConfig(
        nodeName = config.getString(ConfigKey.hostname),
        // TODO: set via command line argument
        metric = Metric.euclidean,
        dimensions = argsConfig.dimensions,
        treesPerNode = argsConfig.treesPerNode,
        maxPointsPerLeaf = argsConfig.maxPointsPerLeaf)
}

case class NeighborhoodConfig(
    nodeName: String,
    metric: Metric,
    dimensions: Int,
    treesPerNode: Int,
    maxPointsPerLeaf: Int)
