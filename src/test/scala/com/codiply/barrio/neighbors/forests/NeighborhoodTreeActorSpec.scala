package com.codiply.barrio.tests.neighbors.forests

import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.TestActors
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.helpers.Random
import com.codiply.barrio.neighbors.NeighborhoodConfig
import com.codiply.barrio.neighbors.forests.NeighborhoodTreeActor
import com.codiply.barrio.neighbors.forests.ActorProtocol.InitialiseTree
import com.codiply.barrio.neighbors.forests.ActorProtocol.NeighborhoodTreeLeafStats
import com.codiply.barrio.neighbors.forests.ActorProtocol.TreeInitialised
import com.codiply.barrio.test.TestKitConfig

class NeighborhoodTreeActorSpec extends TestKit(ActorSystem("NeighborhoodTreeActorSpec", TestKitConfig.config))
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A NeighborhoodTreeActor" must {
    val timeout = 200.milliseconds

    val seed = 123

    val nodeName = "this-is-the-node-name"
    val rootTreeName = "this-is-the-tree-name"
    val dimensions = 2
    val treesPerNode = 1
    val treeStartingDepth = 11

    val point0 = Point("point0", Coordinates(0.0, 0.0), "data-0")
    val point1 = Point("point1", Coordinates(1.0, 0.0), "data-1")
    val point2 = Point("point2", Coordinates(1.0, 1.0), "data-2")
    val point3 = Point("point3", Coordinates(0.0, 1.0), "data-3")

    def createConfig(maxPointsPerLeaf: Int): NeighborhoodConfig = NeighborhoodConfig(
      defaultRequestTimeoutMilliseconds = NeighborhoodConfig.Defaults.defaultRequestTimeoutMilliseconds,
      dimensions = dimensions,
      maxPointsPerLeaf = maxPointsPerLeaf,
      maxRequestTimeoutMilliseconds = NeighborhoodConfig.Defaults.maxRequestTimeoutMilliseconds,
      minRequestTimeoutMilliseconds = NeighborhoodConfig.Defaults.minRequestTimeoutMilliseconds,
      metric = Metric.euclidean,
      nodeName = nodeName,
      treesPerNode = treesPerNode,
      version = "")

    "send the expected messages when it is initialised with no points" in {
      val statsProbe = TestProbe()
      val parentProbe = TestProbe()

      val maxPointsPerLeaf = 1
      val config = createConfig(maxPointsPerLeaf)
      val random = Random(seed)

      val expectedDepth = treeStartingDepth

      val points = List.empty[Point]
      val treeActor = parentProbe.childActorOf(
          NeighborhoodTreeActor.props(rootTreeName, config, random, treeStartingDepth, statsProbe.ref))
      treeActor ! InitialiseTree(points)

      val stats = statsProbe.expectMsgType[NeighborhoodTreeLeafStats](timeout)
      assert(stats.treeName == rootTreeName, "check tree name")
      assert(stats.stats.depth == expectedDepth, "check depth")
      assert(stats.stats.pointCount == points.length, "check point count")

      parentProbe.expectMsg(timeout, TreeInitialised(rootTreeName))
    }
    "send the expected messages when it is initialised with one point" in {
      val statsProbe = TestProbe()
      val parentProbe = TestProbe()

      val seed = 123
      val maxPointsPerLeaf = 1
      val config = createConfig(maxPointsPerLeaf)
      val random = Random(seed)

      val expectedDepth = treeStartingDepth
      val expectedPointCount = 1

      val points = List(point0)
      val treeActor = parentProbe.childActorOf(
          NeighborhoodTreeActor.props(rootTreeName, config, random, treeStartingDepth, statsProbe.ref))
      treeActor ! InitialiseTree(points)

      val stats = statsProbe.expectMsgType[NeighborhoodTreeLeafStats](timeout)
      assert(stats.treeName == rootTreeName, "check tree name")
      assert(stats.stats.depth == expectedDepth, "check depth")
      assert(stats.stats.pointCount == points.length, "check point count")

      parentProbe.expectMsg(timeout, TreeInitialised(rootTreeName))
    }
    "send the expected messages when it is initialised with two points (max points per leaf 1)" in {
      val statsProbe = TestProbe()
      val parentProbe = TestProbe()

      val seed = 123
      val maxPointsPerLeaf = 1
      val config = createConfig(maxPointsPerLeaf)
      val random = Random(seed)

      val expectedDepth = treeStartingDepth + 1
      val expectedPointCount = 1

      val points = List(point0, point1)
      val treeActor = parentProbe.childActorOf(
          NeighborhoodTreeActor.props(rootTreeName, config, random, treeStartingDepth, statsProbe.ref))
      treeActor ! InitialiseTree(points)

      val expectedNumberOfLeafs = 2
      val statsMessages = statsProbe.receiveN(expectedNumberOfLeafs).map {
        _.asInstanceOf[NeighborhoodTreeLeafStats] }.toArray

      (0 until expectedNumberOfLeafs).foreach { (i: Int) => {
          assert(statsMessages(i).treeName == rootTreeName, s"check tree name (message $i)")
          assert(statsMessages(i).stats.depth == expectedDepth, s"check depth (message $i)")
          assert(statsMessages(i).stats.pointCount == points.length / expectedNumberOfLeafs, s"check point count (message $i)")
        }
      }

      parentProbe.expectMsg(timeout, TreeInitialised(rootTreeName))
    }
  }
}
