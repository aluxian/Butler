package com.butler.cloud.controllers

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import com.butler.cloud.databases.GraphDB
import com.butler.cloud.databases.types.{NodeTypes, RelTypes}
import com.butler.cloud.gcm.handlers.GcmSender.SendMessage
import com.butler.cloud.gcm.requests.ServerMessageRequestTypes
import com.butler.cloud.models.data.ResponseData
import com.butler.cloud.models.payload.{ClientMessage, ServerMessage}
import com.butler.cloud.utils.{CommonActors, Json}
import net.liftweb.json.JsonAST.JObject

class AuthRequestCtrl extends Actor with ActorLogging with CommonActors with GraphDB {
  def receive = {
    case clientMessage: ClientMessage =>

      val authToken = UUID.randomUUID.toString

      withTx {
        // Human
        val human = upsertNode(NodeTypes.Human, "id", clientMessage.userId)

        // Human --> SocialAccount
        Map(
          clientMessage.requestData.facebookAccount -> NodeTypes.FacebookAccount,
          clientMessage.requestData.googleAccount -> NodeTypes.GoogleAccount,
          clientMessage.requestData.twitterAccount -> NodeTypes.TwitterAccount
        ).foreach { case (requestData, nodeClass) =>
          if (requestData.isDefined) {
            val props = Json.serialize[JObject](requestData.get).values
            val account = upsertNode(nodeClass, "id", requestData.get.id, props)
            upsertRelationship(human, account, RelTypes.ConnectedTo)
          }
        }

        // Human --> Device
        val deviceProps = Map("authToken" -> authToken, "gcmId" -> clientMessage.receivedMessage.get.from)
        val device = upsertNode(NodeTypes.Device, "id", clientMessage.deviceId, deviceProps)
        upsertRelationship(human, device, RelTypes.PairedWith)
      }

      // Reply
      val serverMessage = ServerMessage(clientMessage.id, ServerMessageRequestTypes.AUTH_RESPONSE, ResponseData(authToken))
      actors.gcm ! SendMessage(serverMessage, clientMessage.receivedMessage.get.from)

  }
}
