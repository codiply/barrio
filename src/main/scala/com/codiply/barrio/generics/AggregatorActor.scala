package com.codiply.barrio.generics

import scala.concurrent.duration._
import scala.reflect.ClassTag

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Cancellable

object AggregatorActorProtocol {
  object DoSendAggregate
}

class AggregatorActor[TAggregate, TResponseIn:  ClassTag, TResponseOut](
    responseRecipient: ActorRef,
    initialAggregateValue: TAggregate,
    folder: (TAggregate, TResponseIn) => TAggregate,
    mapper: TAggregate => TResponseOut,
    expectedNumberOfResponses: Int,
    timeout: FiniteDuration) extends Actor {
  import AggregatorActorProtocol._
  import context.dispatcher

  var currentAggregateValue: TAggregate = initialAggregateValue
  var outstandingIncomingResponses = expectedNumberOfResponses

  var timeoutCancellable: Option[Cancellable] = None

  if (expectedNumberOfResponses <= 0) {
    sendAggregate()
  } else {
    timeoutCancellable = Some(context.system.scheduler.scheduleOnce(timeout, self, DoSendAggregate))
  }

  def receive: Receive = {
    case incomingResponse: TResponseIn =>
      currentAggregateValue = folder(currentAggregateValue, incomingResponse)
      outstandingIncomingResponses -= 1
      if (outstandingIncomingResponses <= 0) {
        timeoutCancellable.foreach { _.cancel() }
        sendAggregate()
      }
    case DoSendAggregate => sendAggregate()
  }

  private def sendAggregate() = {
    val response = mapper(currentAggregateValue)
    responseRecipient ! response
    context.stop(self)
  }
}
