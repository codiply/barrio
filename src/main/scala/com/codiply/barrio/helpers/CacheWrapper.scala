package com.codiply.barrio.helpers

import scala.concurrent.duration._

import com.github.blemale.scaffeine.Cache
import com.github.blemale.scaffeine.Scaffeine

trait CacheWrapper[K, V] {
  def getIfPresent(key: K): Option[V]
  def put(key: K, value: V): Unit
}

class ScaffeineWrapper[K, V](
    expireAfterAccess: Option[Duration],
    maximumSize: Option[Int]) extends CacheWrapper[K, V] {
  var cacheBuilder = Scaffeine().recordStats()

  expireAfterAccess.foreach(duration =>
    cacheBuilder = cacheBuilder.expireAfterAccess(duration)
  )
  maximumSize.foreach(size =>
    cacheBuilder = cacheBuilder.maximumSize(size)
  )

  val cache = cacheBuilder.build[K, V]()

  override def getIfPresent(key: K): Option[V] =
    cache.getIfPresent(key)

  override def put(key: K, value: V): Unit =
    cache.put(key, value)
}
