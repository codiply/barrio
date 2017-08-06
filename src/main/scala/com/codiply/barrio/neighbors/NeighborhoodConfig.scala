package com.codiply.barrio.neighbors

import com.typesafe.config.Config

import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.helpers.ArgsConfig
import com.codiply.barrio.helpers.ConfigKey
import com.codiply.barrio.helpers.VersionHelper

object NeighborhoodConfig {
  object Defaults {
    val maxRequestTimeoutMilliseconds = 60000
    val minRequestTimeoutMilliseconds = 10
    val defaultRequestTimeoutMilliseconds = 10000
  }

  def apply(argsConfig: ArgsConfig, config: Config): NeighborhoodConfig = {
    val maxRequestTimeoutMilliseconds =
      Math.max(Defaults.minRequestTimeoutMilliseconds,
          getPositiveInt(config, ConfigKey.maxRequestTimeoutMilliseconds, Defaults.maxRequestTimeoutMilliseconds))
    val defaultRequestTimeoutMilliseconds =
      Math.min(maxRequestTimeoutMilliseconds,
          getPositiveInt(config, ConfigKey.defaultRequestTimeoutMilliseconds, Defaults.defaultRequestTimeoutMilliseconds))
    NeighborhoodConfig(
        defaultRequestTimeoutMilliseconds = defaultRequestTimeoutMilliseconds,
        dimensions = argsConfig.dimensions,
        maxPointsPerLeaf = argsConfig.maxPointsPerLeaf,
        maxRequestTimeoutMilliseconds = maxRequestTimeoutMilliseconds,
        minRequestTimeoutMilliseconds = Defaults.minRequestTimeoutMilliseconds,
        // TODO: set via command line argument
        metric = Metric.euclidean,
        nodeName = config.getString(ConfigKey.hostname),
        treesPerNode = argsConfig.treesPerNode,
        version = VersionHelper.version)
  }

  private def getPositiveInt(config: Config, key: String, default: Int): Int = {
    try {
      val value = config.getInt(key)
      if (value > 0) value else default
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
    minRequestTimeoutMilliseconds: Int,
    metric: Metric,
    nodeName: String,
    treesPerNode: Int,
    version: String) {
  def getEffectiveTimeoutMilliseconds(requestTimeoutMilliseconds: Option[Int]): Int = {
    requestTimeoutMilliseconds match {
      case None => defaultRequestTimeoutMilliseconds
      case Some(timeout) =>
        Math.max(minRequestTimeoutMilliseconds, Math.min(timeout, maxRequestTimeoutMilliseconds))
    }
  }
}
