package com.butler.cloud.gcm.handlers

import java.util.UUID

import akka.actor._
import com.butler.cloud.databases.MongoDB
import com.butler.cloud.gcm.handlers.GcmSender.{DelayedMessage, ResendMessage, SendMessage}
import com.butler.cloud.models.ccs.SentMessage
import com.butler.cloud.models.payload.ServerMessage
import com.butler.cloud.utils.CommonActors
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

/**
 * Handles messages to be sent to CCS.
 */
class GcmSender extends Actor with ActorLogging with MongoDB with CommonActors {
  def receive = {

    case SendMessage(message, gcmId) =>
      val sentMessage = SentMessage(gcmId, UUID.randomUUID().toString, message)
      val doc = sentMessage.asBSON

      actors.gcm ! sentMessage.asPacket

      mongo.collections.gcm_sent_messages.insert(doc).onFailure {
        case t => log.error(t, "Failed inserting document: {}", doc)
      }

    case ResendMessage(messageId) =>
      mongo.collections.gcm_sent_messages.find(BSONDocument("message_id" -> messageId)).one.map {
        result =>
          val uid = UUID.randomUUID().toString
          val json = ("to" -> result.get.getAs[String]("to")) ~ ("message_id" -> uid) ~ ("data" -> result.get.getAs[String]("data"))
          val newDoc = BSONDocument(result.get.copy().add(BSONDocument("message_id" -> uid)).elements.filterNot(_._1 == "_id"))

          actors.gcm ! compact(render(json))
          mongo.collections.gcm_sent_messages.insert(newDoc).onFailure {
            case t => log.error(t, "Failed inserting document: {}", newDoc)
          }
      }

    case DelayedMessage(msg, delay) =>
      context.system.scheduler.scheduleOnce(delay, self, msg)

  }
}

object GcmSender {

  /**
   * Send a message.
   *
   * @param message The message to send.
   * @param gcmId The GCM ID of the device to send it to.
   */
  case class SendMessage(message: ServerMessage, gcmId: String)

  /**
   * Resend a previous message.
   *
   * @param ccsMessageId The CCS ID of the message.
   */
  case class ResendMessage(ccsMessageId: String)

  /**
   * Process the given msg after the delay time has passed.
   *
   * @param msg The message to process.
   * @param delay The delay.
   */
  case class DelayedMessage(msg: Any, delay: FiniteDuration)

}
