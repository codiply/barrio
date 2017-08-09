package com.codiply.barrio.tests.geometry

import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.TestActors
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.codiply.barrio.generic.AggregatorActor
import com.codiply.barrio.generic.AggregatorActorProtocol.DoSendAggregate
import com.codiply.barrio.test.TestKitConfig

class AggregatorActorSpec extends TestKit(ActorSystem("AggregatorActorSpec", TestKitConfig.config))
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An AggregatorActor" must {
    val initialValue = "i"
    val folder = (aggregate: String, message: String) => aggregate + " then " + message
    val mapper = (aggregate: String) => aggregate.toUpperCase()
    val timeoutMilliseconds = 300
    val timeout = timeoutMilliseconds.millisecond
    val justBeforeTimeout = (timeoutMilliseconds * 0.9).millisecond
    val justAfterTimeout = (timeoutMilliseconds * 1.1).millisecond

    "send no aggregate before the timeout when it receives no responses" in {
      val testProbe = TestProbe()

      val expectedNumberOfResponses = 3
      val expectedResponse = "I"

      val aggregator = system.actorOf(AggregatorActor.props(
          testProbe.ref, initialValue, folder, mapper, expectedNumberOfResponses, timeout))

      testProbe.expectNoMsg(justBeforeTimeout)
    }
    "send the aggregate after the timeout when it receives no responses" in {
      val testProbe = TestProbe()

      val expectedNumberOfResponses = 3
      val expectedResponse = "I"

      val aggregator = system.actorOf(AggregatorActor.props(
          testProbe.ref, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      testProbe.watch(aggregator)

      testProbe.expectMsg(justAfterTimeout, expectedResponse)
      testProbe.expectTerminated(aggregator)
    }
    "send no aggregate before the timeout when not all expected responses are received" in {
      val testProbe = TestProbe()

      val expectedNumberOfResponses = 3

      val message1 = "a"
      val message2 = "b"
      val expectedResponse = "I THEN A THEN B"

      val aggregator = system.actorOf(AggregatorActor.props(
          testProbe.ref, initialValue, folder, mapper, expectedNumberOfResponses, timeout))

      aggregator ! message1
      aggregator ! message2

      testProbe.expectNoMsg(justBeforeTimeout)
    }
    "send the aggregate after the timeout when not all expected responses are received" in {
      val testProbe = TestProbe()

      val expectedNumberOfResponses = 3

      val message1 = "a"
      val message2 = "b"
      val expectedResponse = "I THEN A THEN B"

      val aggregator = system.actorOf(AggregatorActor.props(
          testProbe.ref, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      testProbe.watch(aggregator)

      aggregator ! message1
      aggregator ! message2

      testProbe.expectMsg(justAfterTimeout, expectedResponse)
      testProbe.expectTerminated(aggregator)
    }
    "send back the aggregate when the expected number of responses is received" in {
      val testProbe = TestProbe()

      val expectedNumberOfResponses = 2

      val message1 = "a"
      val message2 = "b"
      val message3 = "c"
      val expectedResponse = "I THEN A THEN B"

      val aggregator = system.actorOf(AggregatorActor.props(
          testProbe.ref, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      testProbe.watch(aggregator)

      aggregator ! message1
      aggregator ! message2
      aggregator ! message3

      testProbe.expectMsg(justBeforeTimeout, expectedResponse)
      testProbe.expectTerminated(aggregator)
    }
    "send back the aggregate when it receives a DoSendAggregateMessage" in {
      val testProbe = TestProbe()

      val expectedNumberOfResponses = 3
      val expectedResponse = "I"

      val aggregator = system.actorOf(AggregatorActor.props(
          testProbe.ref, initialValue, folder, mapper, expectedNumberOfResponses, timeout))
      testProbe.watch(aggregator)

      aggregator ! DoSendAggregate

      testProbe.expectMsg(justBeforeTimeout, expectedResponse)
      testProbe.expectTerminated(aggregator)
    }
  }
}
