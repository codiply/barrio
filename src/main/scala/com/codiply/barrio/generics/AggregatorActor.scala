package com.codiply.barrio.generics

import scala.concurrent.duration._
import scala.reflect.ClassTag

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.ActorRef

object AggregatorActorProtocol {
  object DoSendAggregate
}

class AggregatorActor[TAggregate, TResponse:  ClassTag](
    responseRecipient: ActorRef,
    initialValue: TAggregate,
    folder: (TAggregate, TResponse) => TAggregate,
    mapper: TAggregate => TResponse,
    expectedNumberOfResponses: Int,
    timeout: FiniteDuration) extends Actor {
  import AggregatorActorProtocol._
  import context.dispatcher
  
  var aggregate: TAggregate = initialValue
  var outstandingResponses = expectedNumberOfResponses
 
  val timeoutCancellable = context.system.scheduler.scheduleOnce(timeout, self, DoSendAggregate)
  
  def receive: Receive = {
    case response: TResponse =>
      this.aggregate = folder(this.aggregate, response)
      this.outstandingResponses -= 1
      if (this.outstandingResponses <= 0) {
        timeoutCancellable.cancel()
        self ! DoSendAggregate
      }
    case DoSendAggregate =>
      val response = mapper(this.aggregate)
      responseRecipient ! response
      context.stop(self)
  }
}
