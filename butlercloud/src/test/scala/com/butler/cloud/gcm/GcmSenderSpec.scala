package com.butler.cloud.gcm

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.butler.cloud.gcm.handlers.GcmSender
import com.butler.cloud.gcm.handlers.GcmSender.{ResendMessage, SendMessage}
import com.butler.cloud.gcm.requests.ServerMessageRequestTypes
import com.butler.cloud.models.data.ResponseData
import com.butler.cloud.models.payload.ServerMessage
import com.butler.cloud.testutils.Test
import com.butler.cloud.testutils.actors.ProxyActor
import com.butler.cloud.testutils.databases.TestMongoDB
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class GcmSenderSpec(_system: ActorSystem) extends Test(_system) with TestMongoDB {

  def this() = this(ActorSystem("GcmSenderSpec"))

  var gcm_probe = TestProbe()

  override def beforeAll() = {
    mongoBeforeAll()

    // Create an actor for /user/gcm
    system.actorOf(Props(classOf[ProxyActor], gcm_probe.ref), "gcm")
  }

  override def afterAll() {
    mongoAfterAll()
    TestKit.shutdownActorSystem(system)
  }

  "GcmSender" must {

    val sender = system.actorOf(Props[GcmSender], "GcmSender")

    "process SendMessage(message, gcmId)" in {
      val message = ServerMessage("1", ServerMessageRequestTypes.AUTH_RESPONSE, ResponseData())

      sender ! SendMessage(message, "testId")
      gcm_probe.expectMsgClass(classOf[String])

      val f = mongo.collections.gcm_sent_messages.find(BSONDocument("to" -> "testId")).one

      f.onComplete {
        case doc: Try[Option[BSONDocument]] =>
          doc.isSuccess should equal(true)
          doc.get.isDefined should equal(true)
      }

      Await.result(f, atMost)
    }

    "process ResendMessage(messageId)" in {
      val f1 = mongo.collections.gcm_sent_messages.find(BSONDocument("to" -> "testId")).one

      f1.onComplete {
        case result: Try[Option[BSONDocument]] =>
          result.isSuccess should equal(true)
          result.get.isDefined should equal(true)

          sender ! ResendMessage(result.get.get.getAs[String]("message_id").get)
          gcm_probe.expectMsgClass(classOf[String])
      }

      Await.result(f1, atMost)
      Thread.sleep(delay)

      val f2 = mongo.collections.gcm_sent_messages.find(BSONDocument("to" -> "testId")).cursor[BSONDocument].collect[List]()

      f2.onComplete {
        case result: Try[List[BSONDocument]] =>
          result.isSuccess should equal(true)
          result.get.size should equal(2)
      }

      Await.result(f2, atMost)
    }

  }

}
