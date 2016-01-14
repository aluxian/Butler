package com.butler.cloud.controllers

import akka.actor.{Actor, ActorLogging}
import com.butler.cloud.databases.GraphDB
import com.butler.cloud.models.payload.ClientMessage
import com.butler.cloud.utils.CommonActors

class FriendRequestCtrl extends Actor with ActorLogging with CommonActors with GraphDB {
  def receive = {
    case clientMessage: ClientMessage =>

    // TODO Validate the authToken

    // Find the target node
    //withTx {
    /*val id = clientMessage.requestData.facebookAccount.id

        for (iterator <- neo4j.findNodesByLabelAndProperty(new DynamicLabel("User"), "id", id).iterator())) {
          var node: Node = null

          if (iterator.hasNext) {
            node = iterator.next
          } else {
            node = neo4j.createNode(new DynamicLabel("User"))
            node.setProperty("id", clientMessage.requestData.facebookAccount.id)
          }

          node.setProperty("name", clientMessage.requestData.facebookAccount.name)
          node.setProperty("firstName", clientMessage.requestData.facebookAccount.firstName)
          node.setProperty("lastName", clientMessage.requestData.facebookAccount.lastName)
          node.setProperty("gender", clientMessage.requestData.facebookAccount.gender)
          node.setProperty("locale", clientMessage.requestData.facebookAccount.locale)
          node.setProperty("timezone", clientMessage.requestData.facebookAccount.timezone)
          node.setProperty("link", clientMessage.requestData.facebookAccount.link)
          node.setProperty("token", clientMessage.requestData.facebookAccount.token)
        }*/
    //}

  }
}
