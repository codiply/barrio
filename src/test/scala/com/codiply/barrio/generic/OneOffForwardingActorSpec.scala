package com.codiply.barrio.tests.geometry

import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.TestActors
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.codiply.barrio.generic.OneOffForwardingActor
import com.codiply.barrio.generic.OneOffForwardingActor.OneOffForward
import com.codiply.barrio.test.TestKitConfig

class OneOffForwardingActorSpec extends TestKit(ActorSystem("OneOffForwardingActorSpec", TestKitConfig.config))
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A OneOffForwardingActor" must {
    val timeoutMilliseconds = 300
    val timeout = timeoutMilliseconds.millisecond
    val justBeforeTimeout = (timeoutMilliseconds * 0.9).millisecond
    val justAfterTimeout = (timeoutMilliseconds * 1.1).millisecond
    val zeroTimeout = 0.milliseconds

    "stops itself on timeout" in {
      val senderProbe = TestProbe()
      val receiverProbe = TestProbe()
      val watcherProbe = TestProbe()

      val mapper = (incoming: String) =>
        OneOffForward(from = senderProbe.ref, to = receiverProbe.ref, message = incoming)
      val forwardOnTimeout = None

      val forwarder = system.actorOf(OneOffForwardingActor.props(
          mapper, forwardOnTimeout, timeout))
      watcherProbe.watch(forwarder)


      watcherProbe.expectTerminated(forwarder, justAfterTimeout)
    }
    "forwards nothing before the timeout if it receives nothing" in {
      val senderProbe = TestProbe()
      val receiverProbe = TestProbe()
      val timeoutReceiverProbe = TestProbe()
      val watcherProbe = TestProbe()

      val timeoutMessage = "timeout-message"

      val mapper = (incoming: String) =>
        OneOffForward(from = senderProbe.ref, to = receiverProbe.ref, message = incoming.toUpperCase)
      val forwardOnTimeout = Some(OneOffForward(
        from = senderProbe.ref, to = timeoutReceiverProbe.ref, timeoutMessage))

      val forwarder = system.actorOf(OneOffForwardingActor.props(
          mapper, forwardOnTimeout, timeout))
      watcherProbe.watch(forwarder)

      senderProbe.expectNoMsg(justBeforeTimeout)
      receiverProbe.expectNoMsg(zeroTimeout)
      timeoutReceiverProbe.expectNoMsg(zeroTimeout)
      watcherProbe.expectNoMsg(zeroTimeout)
    }
    "forwards expected message on timeout" in {
      val senderProbe = TestProbe()
      val receiverProbe = TestProbe()
      val timeoutReceiverProbe = TestProbe()
      val watcherProbe = TestProbe()

      val timeoutMessage = "timeout-message"

      val mapper = (incoming: String) =>
        OneOffForward(from = senderProbe.ref, to = receiverProbe.ref, message = incoming.toUpperCase)
      val forwardOnTimeout = Some(OneOffForward(
        from = senderProbe.ref, to = timeoutReceiverProbe.ref, timeoutMessage))

      val forwarder = system.actorOf(OneOffForwardingActor.props(
          mapper, forwardOnTimeout, timeout))
      watcherProbe.watch(forwarder)

      senderProbe.expectNoMsg(justAfterTimeout)
      receiverProbe.expectNoMsg(zeroTimeout)
      timeoutReceiverProbe.expectMsg(zeroTimeout, timeoutMessage)
      watcherProbe.expectTerminated(forwarder, zeroTimeout)
    }
    "forwards the message when there is a single receiver" in {
      val incomingMessage = "message"
      val expectedForwardedMessage = "MESSAGE"

      val senderProbe = TestProbe()
      val receiverProbe = TestProbe()
      val watcherProbe = TestProbe()

      val mapper = (incoming: String) =>
        OneOffForward(from = senderProbe.ref, to = receiverProbe.ref, message = incoming.toUpperCase)
      val forwardOnTimeout = None

      val forwarder = system.actorOf(OneOffForwardingActor.props(
          mapper, forwardOnTimeout, timeout))
      watcherProbe.watch(forwarder)

      forwarder ! incomingMessage

      watcherProbe.expectTerminated(forwarder, justBeforeTimeout)
      receiverProbe.expectMsg(zeroTimeout, expectedForwardedMessage)
      senderProbe.expectNoMsg(zeroTimeout)
    }
  }
}
