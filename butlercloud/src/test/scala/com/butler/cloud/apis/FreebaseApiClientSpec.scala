package com.butler.cloud.apis

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import com.butler.cloud.apis.FreebaseApiClient.{Lookup, LookupResult, Search, SearchResponse}
import com.butler.cloud.testutils.Test

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class FreebaseApiClientSpec(_system: ActorSystem) extends Test(_system) {

  def this() = this(ActorSystem("FreebaseApiClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "FreebaseApiClient" must {

    val endpoint: ActorRef = system.actorOf(Props[FreebaseApiClient], "FreebaseApiClient")

    "do a simple search" in {
      val future = endpoint ? Search("Michael Jackson", limit = 1)

      future.mapTo[SearchResponse].onSuccess {
        case response =>
          system.log.info("Got response: {}", response)
          response.result(0).name.shouldEqual("Michael Jackson")
      }

      Await.result(future, atMost)
    }

    "do a filtered search" in {
      val future = endpoint ? Search(filter = "(all name:\"Michael Jackson\" type:/people/person)", limit = 1)

      future.mapTo[SearchResponse].onSuccess {
        case response =>
          system.log.info("Got response: {}", response)
          response.result(0).name.shouldEqual("Michael Jackson")
      }

      Await.result(future, atMost)
    }

    "do a lookup" in {
      val future = endpoint ? Search(filter = "(all name:\"Michael Jackson\" type:/people/person)", limit = 1)

      future.mapTo[SearchResponse].onSuccess {
        case response =>
          system.log.info("Got response: {}", response)

          val future = endpoint ? Lookup(response.result(0).mid)

          future.mapTo[LookupResult].onSuccess {
            case result =>
              system.log.info("Got response: {}", result)
              result.property.`/common/topic/description`.values(0).text.startsWith("Michael Joseph Jackson").shouldEqual(true)
          }

          Await.result(future, atMost)
      }

      Await.result(future, atMost)
    }

  }

}
