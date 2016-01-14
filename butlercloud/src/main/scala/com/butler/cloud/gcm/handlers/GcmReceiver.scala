package com.butler.cloud.gcm.handlers

import akka.actor.{Actor, ActorLogging}
import com.butler.cloud.databases.{GraphDB, MongoDB}
import com.butler.cloud.gcm.handlers.GcmConnectionsRouter.RemoveConnection
import com.butler.cloud.gcm.handlers.GcmSender.{DelayedMessage, ResendMessage}
import com.butler.cloud.models.ccs.ReceivedMessage
import com.butler.cloud.utils.CommonActors
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Handles messages received from CCS.
 */
class GcmReceiver extends Actor with ActorLogging with MongoDB with GraphDB with CommonActors {
  def receive = {
    case gcmMessage: ReceivedMessage => gcmMessage.messageType match {

      case "ack" =>
        log.info("ACK message")

        mongo.collections.gcm_sent_messages.update(
          BSONDocument("message_id" -> gcmMessage.messageId),
          BSONDocument("$set" -> BSONDocument("ack" -> true))
        ).onFailure {
          case t => log.error(t, "Failed ACKing message: {}", gcmMessage.messageId)
        }

      case "nack" =>
        log.warning("NACK message")

        // Handle error
        gcmMessage.error match {
          case "BAD_ACK" | "BAD_REGISTRATION" | "DEVICE_UNREGISTERED" | "INVALID_JSON" =>
            log.error("Error from CCS: {} for messageId: {}", gcmMessage.error, gcmMessage.messageId)

          case "INTERNAL_SERVER_ERROR" | "QUOTA_EXCEEDED" | "SERVICE_UNAVAILABLE" =>
            log.warning("Error from CCS: {} for messageId: {}", gcmMessage.error, gcmMessage.messageId)
            actors.gcm ! DelayedMessage(ResendMessage(gcmMessage.messageId), 5.seconds)

          case "CONNECTION_DRAINING" =>
            log.warning("Connection draining, sending reconnect message")
            actors.gcm.tell(RemoveConnection(), sender())
            actors.gcm ! ResendMessage(gcmMessage.messageId)
        }

        mongo.collections.gcm_sent_messages.update(
          BSONDocument("message_id" -> gcmMessage.messageId),
          BSONDocument("$set" -> BSONDocument("nack" -> true))
        ).onFailure {
          case t => log.error(t, "Failed NACKing message: {}", gcmMessage.messageId)
        }

      case "control" =>
        log.info("Control message")

        // Handle control type
        gcmMessage.controlType match {
          case "CONNECTION_DRAINING" =>
            log.warning("Connection draining, sending reconnect message")
            actors.gcm.tell(RemoveConnection(), sender())
        }

      case _ =>
        log.info("Payload message")

        val clientMessage = gcmMessage.data
        clientMessage.receivedMessage = Option(gcmMessage)
        context.actorSelection("/user/controllers/" + clientMessage.requestType.toString) ! clientMessage

    }
  }
}

/*clientMessage.requestType match {
      case FRIEND_REQUEST => {
        log.info("FRIEND_REQUEST")
        val properties: List[String] = new ArrayList[String]
        properties.add("gcmId")
        val findNode: GraphDb.FindNode = new GraphDb.FindNode(DynamicLabel.label("Device"), "ownerName",
        clientMessage.friendRequest.targetOwnerName, properties)
        log.info("Asking GraphDB " + Utils.gson.toJson(findNode))
        val future: Future[AnyRef] = new GraphDb().ask(context, findNode)
        future.onSuccess(new OnSuccess[AnyRef] {
          def onSuccess(result: AnyRef) {
            log.info("Got response " + Utils.gson.toJson(result))
            if (result.isInstanceOf[HashMap[_, _]]) {
              val properties: HashMap[String, AnyRef] = result.asInstanceOf[HashMap[String, AnyRef]]
              val targetDeviceGcmId: String = properties.get("gcmId").asInstanceOf[String]
              new GcmManager().tell(new GcmServerMessage(clientMessage.messageId, GcmServerMessage.Type.FRIEND_REQUEST,
              clientMessage.friendRequest.sourceOwnerName, targetDeviceGcmId), self)
            }
            else {
              new GcmManager().tell(new GcmServerMessage(clientMessage.messageId, GcmServerMessage.Type.FRIEND_REQUEST_NOT_FOUND, null,
              clientMessage.gcmId), self)
            }
          }
        }, context.system.dispatcher)
        future.onFailure(new OnFailure {
          def onFailure(failure: Throwable) {
            throw failure
          }
        }, context.system.dispatcher)
        break //todo: break is not supported
      }
      case FRIEND_REQUEST_RESPONSE => {
        log.info("FRIEND_REQUEST_RESPONSE")
        val properties: ArrayList[String] = new ArrayList[String]
        properties.add("deviceId")
        properties.add("gcmId")
        val findNode: GraphDb.FindNode = new GraphDb.FindNode(DynamicLabel.label("Device"), "ownerName",
        clientMessage.friendRequestResponse.targetOwnerName, properties)
        new GraphDb().ask(context, findNode).onSuccess(new OnSuccess[AnyRef] {
          def onSuccess(result: AnyRef) {
            if (result.isInstanceOf[HashMap[_, _]]) {
              val properties: HashMap[String, AnyRef] = result.asInstanceOf[HashMap[String, AnyRef]]
              val targetDeviceId: String = properties.get("deviceId").asInstanceOf[String]
              val targetDeviceGcmId: String = properties.get("gcmId").asInstanceOf[String]
              new GcmManager().tell(new GcmServerMessage(clientMessage.messageId, GcmServerMessage.Type.FRIEND_REQUEST_RESPONSE,
              clientMessage.friendRequestResponse.response, targetDeviceGcmId), self)
              new GraphDb().tell(new GraphDb.CreateRelationship(clientMessage.deviceId, targetDeviceId, "FRIENDSHIP"), self)
            }
          }
        }, context.system.dispatcher)
        break //todo: break is not supported
      }
      case TELL_MESSAGE => {
        log.info("TELL_MESSAGE")
        val properties: List[String] = new ArrayList[String]
        properties.add("gcmId")
        val findNode: GraphDb.FindNode = new GraphDb.FindNode(DynamicLabel.label("Device"), "ownerName",
        clientMessage.tellMessage.targetOwnerName, properties)
        log.info("Asking GraphDB " + Utils.gson.toJson(findNode))
        val future: Future[AnyRef] = new GraphDb().ask(context, findNode)
        future.onSuccess(new OnSuccess[AnyRef] {
          def onSuccess(result: AnyRef) {
            log.info("Got response " + Utils.gson.toJson(result))
            if (result.isInstanceOf[HashMap[_, _]]) {
              val properties: HashMap[String, AnyRef] = result.asInstanceOf[HashMap[String, AnyRef]]
              val targetDeviceGcmId: String = properties.get("gcmId").asInstanceOf[String]
              new GcmManager().tell(new GcmServerMessage(clientMessage.messageId, GcmServerMessage.Type.TELL_MESSAGE,
              clientMessage.tellMessage.sourceOwnerName + "|" + clientMessage.tellMessage.message, targetDeviceGcmId), self)
            }
            else {
              new GcmManager().tell(new GcmServerMessage(clientMessage.messageId, GcmServerMessage.Type.FRIEND_REQUEST_NOT_FOUND, null,
              clientMessage.gcmId), self)
            }
          }
        }, context.system.dispatcher)
        future.onFailure(new OnFailure {
          def onFailure(failure: Throwable) {
            throw failure
          }
        }, context.system.dispatcher)
        break //todo: break is not supported
      }
      case TRACK_EVENT => {
        log.info("TRACK_EVENT")
        val timestamp: Date = new Date(Long.valueOf(clientMessage.trackEvent.timestamp))
        val message: String = clientMessage.trackEvent.message
        new RedisDb().tell(new RedisDb.TrackEvent(clientMessage.deviceId, timestamp, message), self)
        break //todo: break is not supported
      }
    }*/
