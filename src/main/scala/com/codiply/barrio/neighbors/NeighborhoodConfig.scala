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
    val cacheExpirationAfterAccessSeconds = 1200
    val cacheMaximumSize = 10000
  }

  def apply(argsConfig: ArgsConfig, config: Config): NeighborhoodConfig = {
    val maxRequestTimeoutMilliseconds =
      Math.max(Defaults.minRequestTimeoutMilliseconds,
          getPositiveInt(config, ConfigKey.maxRequestTimeoutMilliseconds, Defaults.maxRequestTimeoutMilliseconds))
    val defaultRequestTimeoutMilliseconds =
      Math.min(maxRequestTimeoutMilliseconds,
          getPositiveInt(config, ConfigKey.defaultRequestTimeoutMilliseconds, Defaults.defaultRequestTimeoutMilliseconds))
    NeighborhoodConfig(
        cache = argsConfig.cache,
        cacheConfig = CacheConfig(
            expirationAfterAccessSeconds =
              getPositiveInt(config, ConfigKey.cacheExpireAfterAccessSeconds, Defaults.cacheExpirationAfterAccessSeconds),
            maximumSize =
              getPositiveInt(config, ConfigKey.cacheMaximumSize, Defaults.cacheMaximumSize)),
        defaultRequestTimeoutMilliseconds = defaultRequestTimeoutMilliseconds,
        dimensions = argsConfig.dimensions,
        maxPointsPerLeaf = argsConfig.maxPointsPerLeaf,
        maxRequestTimeoutMilliseconds = maxRequestTimeoutMilliseconds,
        minRequestTimeoutMilliseconds = Defaults.minRequestTimeoutMilliseconds,
        metric = Metric.allMetrics.getOrElse(argsConfig.metric, Metric.euclidean),
        nodeName = config.getString(ConfigKey.hostname),
        treesPerNode = argsConfig.treesPerNode,
        version = VersionHelper.version)
  }

  private def getPositiveInt(config: Config, key: String, default: Int): Int = {
    getPositiveIntOption(config, key) match {
      case Some(value) => value
      case None => default
    }
  }

  private def getPositiveIntOption(config: Config, key: String): Option[Int] = {
    try {
      val value = config.getInt(key)
      if (value > 0) Some(value) else None
    } catch {
      case e: Exception => None
    }
  }
}

case class NeighborhoodConfig(
    cache: Boolean,
    cacheConfig: CacheConfig,
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

case class CacheConfig(expirationAfterAccessSeconds: Int, maximumSize: Int)
