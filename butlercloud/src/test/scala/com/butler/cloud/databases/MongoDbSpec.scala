package com.butler.cloud.databases

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.butler.cloud.testutils.databases.TestMongoDB
import com.butler.cloud.testutils.{Test, TestConfig}
import reactivemongo.api.MongoDriver

import scala.concurrent.ExecutionContext.Implicits.global

class MongoDbSpec(_system: ActorSystem) extends Test(_system) with TestMongoDB {

  def this() = this(ActorSystem("MongoDbSpec"))

  override def beforeAll() = {
    mongoBeforeAll()
  }

  override def afterAll() {
    mongoAfterAll()
    TestKit.shutdownActorSystem(system)
  }

  "MongoDb" must {

    "connect successfully" in {
      mongo.db.connection.waitForPrimary.onFailure {
        case ex =>
      }
    }

  }

}
