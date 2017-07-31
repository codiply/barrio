package com.codiply.barrio.helpers

import scala.util.{Random => ScalaRandom}

import java.time.Instant

trait RandomProvider {
  def nextInt(): Int
  def nextLong(): Long
  def nextFloat(): Float
  def nextDouble(): Double

  def getNew(): RandomProvider
}

object Random {
  def apply(): Random = new Random()
  def apply(seed: Int): Random = new Random(seed)
}

class Random(seed: Int) extends RandomProvider {
  def this() {
    this(Instant.now.getNano)
  }

  val rand = new ScalaRandom(seed)

  def nextInt(): Int = rand.nextInt()
  def nextLong(): Long = rand.nextLong()
  def nextFloat(): Float = rand.nextFloat()
  def nextDouble(): Double = rand.nextDouble()

  def getNew(): RandomProvider = {
    new Random(nextInt())
  }
}
