package com.butler.cloud.apis

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import com.butler.cloud.apis.GenderApiClient.{Get, Response}
import com.butler.cloud.testutils.Test

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class GenderApiClientSpec(_system: ActorSystem) extends Test(_system) {

  def this() = this(ActorSystem("GenderApiClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "GenderApiClient" must {

    val endpoint: ActorRef = system.actorOf(Props[GenderApiClient], "GenderApiClient")

    "recognise a male name" in {
      val future = endpoint ? Get("Alexandru", "RO")

      future.mapTo[Response].onSuccess {
        case response =>
          system.log.debug("Got response: {}", response)
          response.gender.shouldEqual("male")
      }

      Await.result(future, atMost)
    }

    "recognise a female name" in {
      val future = endpoint ? Get("Alexandra", "RO")

      future.mapTo[Response].onSuccess {
        case response =>
          system.log.debug("Got response: {}", response)
          response.gender.shouldEqual("female")
      }

      Await.result(future, atMost)
    }

  }

}
