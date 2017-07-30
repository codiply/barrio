package com.codiply.barrio.neighbors

import com.codiply.barrio.helpers.ArgsConfig

object NeighborhoodConfig {
  def apply(argsConfig: ArgsConfig): NeighborhoodConfig =
    NeighborhoodConfig(
        dimensions = argsConfig.dimensions,
        treesPerNode = argsConfig.treesPerNode)
}

case class NeighborhoodConfig(
    dimensions: Int,
    treesPerNode: Int)
