package com.codiply.barrio.neighbors.forests

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.helpers.RandomProvider
import com.codiply.barrio.helpers.TimeHelper
import com.codiply.barrio.helpers.TimeStamp
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.NeighborhoodConfig


object NeighborhoodForestActor {
  def props(
    name: String,
    points: List[Point],
    config: NeighborhoodConfig,
    random: RandomProvider): Props =
      Props(new NeighborhoodForestActor(name, points, config, random))
}

class NeighborhoodForestActor(
    name: String,
    points: List[Point],
    config: NeighborhoodConfig,
    random: RandomProvider) extends Actor with ActorLogging {
  import ActorProtocol._
  import com.codiply.barrio.neighbors.MemoryStats
  import com.codiply.barrio.neighbors.NearestNeighborsContainer
  import com.codiply.barrio.neighbors.TreeStats
  import com.codiply.barrio.neighbors.NodeStats

  import context.dispatcher

  val statsActor = context.actorOf(NeihborhoodForestStatsActor.props(), "stats-actor")

  val trees = (1 to config.treesPerNode).map(i => {
    val name = "tree-" + i
    context.actorOf(NeighborhoodTreeActor.props(name, config, random.createNew(), 0, statsActor), name)
  }).toList

  trees.foreach { _ ! InitialiseTree(points) }

  var initialisedTreesCount = 0
  var initialisedTrees: List[ActorRef] = Nil

  def receive: Receive = {
    case msg @ TreeInitialised(rootTreeName) => {
      if (trees.contains(sender)) {
        initialisedTreesCount += 1
        initialisedTrees = sender +: initialisedTrees
        statsActor ! msg
      }
    }
    case request: GetNeighborsRequestByLocation => {
      if (request.location.length == config.dimensions) {
        val originalSender = sender

        val timeoutMilliseconds = TimeHelper.timeoutFromNowMilliseconds(request.timeoutOn)
        val effectiveTimeoutMilliseconds = config.getEffectiveTimeoutMilliseconds(Some(timeoutMilliseconds))
        val effectiveTimeoutOn = TimeStamp.fromMillisFromNow(effectiveTimeoutMilliseconds)

        val searchActor = context.actorOf(NeighborhoodForestSearchActor.props(
            originalSender, initialisedTrees, request.location, request.k, request.distanceThreshold,
            includeData = request.includeData, includeLocation = request.includeLocation,
            timeoutOn = TimeHelper.slightlyReduceTimeout(effectiveTimeoutOn)))
      } else {
        sender ! NearestNeighborsContainer.empty(request.k)
      }
    }
    case GetNodeStatsRequest(timeoutMilliseconds, doGarbageCollect) =>
      val originalSender = sender
      sendNodeStats(timeoutMilliseconds, originalSender, doGarbageCollect)
  }

  private def sendNodeStats(timeoutMilliseconds: Long, recipient: ActorRef, doGarbageCollect: Boolean): Unit = {
    implicit val askTimeout = Timeout(timeoutMilliseconds.milliseconds)

    val runtime = Runtime.getRuntime

    if (doGarbageCollect) {
      runtime.gc()
    }

    val mb = 1024 * 1024
    val freeMemoryMB = runtime.freeMemory.toDouble / mb;
    val totalMemoryMB = runtime.totalMemory.toDouble / mb;
    val maxMemoryMB = runtime.maxMemory.toDouble /mb;

    val treeStatsResponse = (statsActor ? GetNeighborhoodTreeStatsRequest).mapTo[GetNeighborhoodTreeStatsResponse]

    treeStatsResponse onSuccess { case GetNeighborhoodTreeStatsResponse(treeStats) =>
      recipient ! GetNodeStatsResponse(name, NodeStats(
          initialised = !treeStats.values.exists(!_.initialised),
          version = config.version,
          dimensions = config.dimensions,
          memory = MemoryStats(
            freeMemoryMB = freeMemoryMB,
            totalMemoryMB = totalMemoryMB,
            maxMemoryMB = maxMemoryMB,
            usedMemoryMB = totalMemoryMB - freeMemoryMB),
          trees = treeStats
        ))
    }
  }
}
