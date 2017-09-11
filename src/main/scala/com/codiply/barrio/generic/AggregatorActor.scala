package com.codiply.barrio.generic

import scala.concurrent.duration._
import scala.reflect.ClassTag

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props

case class AggregatorMapperContext(
    timeoutReached: Boolean,
    earlyTerminationConditionMet: Boolean,
    earlyTerminationRequested: Boolean)

object AggregatorActor {
  def props[TAggregate, TResponseIn: ClassTag, TResponseOut](
    responseRecipient: ActorRef,
    initialValue: TAggregate,
    folder: (TAggregate, TResponseIn) => TAggregate,
    mapper: (TAggregate, AggregatorMapperContext) => TResponseOut,
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
    mapper: (TAggregate, AggregatorMapperContext) => TResponseOut,
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
    sendAggregate(timeoutReached = false, earlyTerminationConditionMet = false)
  } else {
    timeoutCancellable = Some(context.system.scheduler.scheduleOnce(timeout, self, DoSendAggregate))
  }

  val mustTerminateEarly = earlyTerminationCondition.getOrElse((aggregate: TAggregate) => false)

  def receive: Receive = {
    case incomingResponse: TResponseIn =>
      currentAggregateValue = folder(currentAggregateValue, incomingResponse)
      outstandingIncomingResponses -= 1
      val earlyTerminationConditionMet = mustTerminateEarly(currentAggregateValue)
      if (earlyTerminationConditionMet || outstandingIncomingResponses <= 0) {
        terminateAggregation(conditionMet = earlyTerminationConditionMet)
      }
    case DoSendAggregate => sendAggregate(timeoutReached = true)
    case TerminateAggregationEarly => terminateAggregation(requested = true)
    case CancelAggregation => cancelAggregation()
  }

  private def cancelAggregation() = {
    timeoutCancellable.foreach { _.cancel() }
    context.stop(self)
  }

  private def terminateAggregation(conditionMet: Boolean = false, requested: Boolean = false) = {
    timeoutCancellable.foreach { _.cancel() }
    sendAggregate(earlyTerminationConditionMet = conditionMet, earlyTerminationRequested = requested)
  }

  private def sendAggregate(
      timeoutReached: Boolean = false,
      earlyTerminationConditionMet: Boolean = false,
      earlyTerminationRequested: Boolean = false) = {
    val response = mapper(currentAggregateValue,
        AggregatorMapperContext(
            timeoutReached = timeoutReached,
            earlyTerminationConditionMet = earlyTerminationConditionMet,
            earlyTerminationRequested = earlyTerminationRequested))
    responseRecipient ! response
    context.stop(self)
  }
}
