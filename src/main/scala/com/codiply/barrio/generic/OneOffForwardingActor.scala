package com.codiply.barrio.generic

import scala.concurrent.duration._
import scala.reflect.ClassTag

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props

import OneOffForwardingActor.OneOffForward

object OneOffForwardingActor {
  def props[TResponseIn: ClassTag](
    mapper: TResponseIn => OneOffForward,
    timeoutForward: Option[OneOffForward],
    timeout: FiniteDuration): Props =
      Props(new OneOffForwardingActor(mapper, timeoutForward, timeout))

  case class OneOffForward(
      from: ActorRef,
      to: ActorRef,
      message: AnyRef)
}

object OneOffForwardingActorProtocol {
  object ForwardTimeoutMessage
}

class OneOffForwardingActor[TResponseIn: ClassTag](
    forwardMapper: TResponseIn => OneOffForward,
    forwardOnTimeout: Option[OneOffForward],
    timeout: FiniteDuration) extends Actor {
  import OneOffForwardingActorProtocol._
  import context.dispatcher

  val timeoutCancellable: Cancellable =
    context.system.scheduler.scheduleOnce(timeout, self, ForwardTimeoutMessage)

  def receive: Receive = {
    case incomingResponse: TResponseIn =>
      timeoutCancellable.cancel()
      doForward(forwardMapper(incomingResponse))
      context.stop(self)
    case ForwardTimeoutMessage =>
      forwardOnTimeout.foreach(doForward)
      context.stop(self)
  }

  private def doForward(forward: OneOffForward) = {
    forward.to.tell(forward.message, forward.from)
  }
}
