package com.butler.cloud.gcm

import akka.actor._
import akka.routing.FromConfig
import com.butler.cloud.gcm.handlers.{GcmConnectionsRouter, GcmReceiver, GcmSender}
import com.butler.cloud.models.ccs.ReceivedMessage

/**
 * Supervises the following routers:
 * - connections: A pool of connections to CCS for sending and receiving packets
 * - receivers: A pool of actors which process messages received from CCS
 * - senders: A pool of actors which process messages to be sent to CCS
 *
 * If it receives:
 * - a String, the message is forwarded to the connections router
 * - a ReceivedMessage, the message is forwarded to the receivers router
 * - Otherwise, the message is forwarded to the senders router
 */
class GcmManager extends Actor {

  val connectionsProps = Props[GcmConnectionsRouter].withDispatcher("dispatchers.gcm-connections-router")
  val connections: ActorRef = context.actorOf(connectionsProps, "connections")
  val receivers: ActorRef = context.actorOf(FromConfig.props(Props[GcmReceiver]), "receivers")
  val senders: ActorRef = context.actorOf(FromConfig.props(Props[GcmSender]), "senders")

  def receive = {
    case json: String => connections.forward(json)
    case msg: ReceivedMessage => receivers.forward(msg)
    case msg: Any => senders.forward(msg)
  }

}
