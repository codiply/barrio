package com.codiply.barrio.tests.neighbors

import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.LocationIndexActor
import com.codiply.barrio.neighbors.LocationIndexActorProtocol.GetLocationRequest
import com.codiply.barrio.neighbors.LocationIndexActorProtocol.GetLocationResponse
import com.codiply.barrio.test.TestKitConfig

class LocationIndexActorSpec extends TestKit(ActorSystem("LocationIndexActorSpec", TestKitConfig.config))
  with WordSpecLike with ImplicitSender with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A LocationIndexActor" must {
    val timeout = 200.milliseconds

    val point1 = Point("point1", List(1.0, 2.0, 3.0), "data-1")
    val point2 = Point("point2", List(2.0, 3.0, 4.0), "data-2")
    val point3 = Point("point3", List(3.0, 4.0, 5.0), "data-3")

    val points = List(point1, point2, point3)

    "send the expected response when list of points is empty" in {
      val locationIndexActor = system.actorOf(LocationIndexActor.props(List.empty))

      locationIndexActor ! GetLocationRequest(point1.id)

      expectMsg(timeout, GetLocationResponse(None))
    }
    "send the expected response when id is found" in {
      val locationIndexActor = system.actorOf(LocationIndexActor.props(points))

      locationIndexActor ! GetLocationRequest(point1.id)

      expectMsg(timeout, GetLocationResponse(Some(point1.location)))
    }
    "send the expected response when id is not found" in {
      val locationIndexActor = system.actorOf(LocationIndexActor.props(points))

      val id = "some-id-that-does-not-exist"
      locationIndexActor ! GetLocationRequest(id)

      expectMsg(timeout, GetLocationResponse(None))
    }
  }
}
