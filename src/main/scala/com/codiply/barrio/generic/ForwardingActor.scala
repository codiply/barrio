package com.codiply.barrio.generic

import scala.concurrent.duration._
import scala.reflect.ClassTag

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props
import ForwardingActor.Forward

object ForwardingActor {
  def props[TResponseIn: ClassTag](
    mapper: TResponseIn => Forward,
    timeoutForward: Option[Forward],
    timeout: FiniteDuration): Props =
      Props(new ForwardingActor(mapper, timeoutForward, timeout))

  case class Forward(
      from: ActorRef,
      to: ActorRef,
      message: AnyRef)
}

object ForwardingActorProtocol {
  object ForwardTimeoutMessage
}

class ForwardingActor[TResponseIn: ClassTag](
    forwardMapper: TResponseIn => Forward,
    forwardOnTimeout: Option[Forward],
    timeout: FiniteDuration) extends Actor {
  import ForwardingActorProtocol._
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

  private def doForward(forward: Forward) = {
    forward.to.tell(forward.message, forward.from)
  }
}
