package com.codiply.barrio.generic

import scala.concurrent.duration._
import scala.reflect.ClassTag

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props

object AggregatorActor {
  def props[TAggregate, TResponseIn: ClassTag, TResponseOut](
    responseRecipient: ActorRef,
    initialValue: TAggregate,
    folder: (TAggregate, TResponseIn) => TAggregate,
    mapper: TAggregate => TResponseOut,
    earlyTerminationCondition: Option[TAggregate => Boolean],
    expectedNumberOfResponses: Int,
    timeout: FiniteDuration
  ): Props = Props(new AggregatorActor(
      responseRecipient, initialValue, folder, mapper, earlyTerminationCondition, expectedNumberOfResponses, timeout))
}

object AggregatorActorProtocol {
  object TerminateAggregationEarly
  object CancelAggregation
}

class AggregatorActor[TAggregate, TResponseIn: ClassTag, TResponseOut](
    responseRecipient: ActorRef,
    initialValue: TAggregate,
    folder: (TAggregate, TResponseIn) => TAggregate,
    mapper: TAggregate => TResponseOut,
    earlyTerminationCondition: Option[TAggregate => Boolean],
    expectedNumberOfResponses: Int,
    timeout: FiniteDuration) extends Actor {
  object DoSendAggregate

  import AggregatorActorProtocol._
  import context.dispatcher

  var currentAggregateValue: TAggregate = initialValue
  var outstandingIncomingResponses = expectedNumberOfResponses

  var timeoutCancellable: Option[Cancellable] = None

  if (expectedNumberOfResponses <= 0) {
    sendAggregate()
  } else {
    timeoutCancellable = Some(context.system.scheduler.scheduleOnce(timeout, self, DoSendAggregate))
  }

  val mustTerminateEarly = earlyTerminationCondition.getOrElse((aggregate: TAggregate) => false)

  def receive: Receive = {
    case incomingResponse: TResponseIn =>
      currentAggregateValue = folder(currentAggregateValue, incomingResponse)
      outstandingIncomingResponses -= 1
      if (mustTerminateEarly(currentAggregateValue) || outstandingIncomingResponses <= 0) {
        terminateAggregationEarly()
      }
    case DoSendAggregate => sendAggregate()
    case TerminateAggregationEarly => terminateAggregationEarly()
    case CancelAggregation => cancelAggregation()
  }

  private def cancelAggregation() = {
    timeoutCancellable.foreach { _.cancel() }
    context.stop(self)
  }

  private def terminateAggregationEarly() = {
    timeoutCancellable.foreach { _.cancel() }
    sendAggregate()
  }

  private def sendAggregate() = {
    val response = mapper(currentAggregateValue)
    responseRecipient ! response
    context.stop(self)
  }
}
