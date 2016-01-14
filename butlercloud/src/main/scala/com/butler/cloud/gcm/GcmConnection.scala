package com.butler.cloud.gcm

import javax.net.ssl.SSLSocketFactory

import akka.actor.{Actor, ActorLogging}
import com.butler.cloud.databases.MongoDB
import com.butler.cloud.gcm.GcmConnection.ConnectionRemoved
import com.butler.cloud.gcm.GcmPacketExtension._
import com.butler.cloud.gcm.handlers.GcmConnectionsRouter.{PoisonMe, RemoveConnection}
import com.butler.cloud.models.ccs.ReceivedMessage
import com.butler.cloud.utils.{CommonActors, Settings}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.jivesoftware.smack._
import org.jivesoftware.smack.filter.PacketTypeFilter
import org.jivesoftware.smack.packet.{Message, Packet}
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smack.tcp.XMPPTCPConnection

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Actor responsible for managing a connection to CCS.
 */
class GcmConnection extends Actor with ActorLogging with MongoDB with CommonActors {

  val settings = Settings(context.system)
  val config = new ConnectionConfiguration(settings.gcm.server, settings.gcm.port)

  config.setSocketFactory(SSLSocketFactory.getDefault)
  config.setDebuggerEnabled(settings.gcm.debug)
  config.setRosterLoadedAtLogin(false)
  config.setSendPresence(false)

  var xmppConnection: XMPPConnection = null
  var removedFromPool: Boolean = false

  val connectionListener = new ConnectionListener {
    def connected(connection: XMPPConnection) = log.info("Connected to CCS {}", connection.getConnectionID)
    def authenticated(connection: XMPPConnection) = log.info("Authenticated to CCS {}", connection.getConnectionID)

    def connectionClosed() {
      log.info("Connection to CCS closed")
      actors.gcm ! RemoveConnection()
      actors.gcm ! PoisonMe()
    }

    def connectionClosedOnError(e: Exception) {
      log.error(e, "Connection to CCS closed on error")
      actors.gcm ! RemoveConnection()
      actors.gcm ! PoisonMe()
    }

    def reconnectingIn(seconds: Int) = log.info("Reconnecting to CCS in {}", seconds)
    def reconnectionSuccessful() = log.info("Reconnection to CCS successful")
    def reconnectionFailed(e: Exception) = log.error(e, "Reconnection to CCS failed")
  }

  val packetListener = new PacketListener {
    override def processPacket(packet: Packet) = {
      val json = packet.getExtension(NAMESPACE).asInstanceOf[GcmPacketExtension].getJson
      log.info("Received message from CCS: {}", json)

      implicit val formats = DefaultFormats
      val receivedMessage = parse(json).extract[ReceivedMessage]

      // Store the message
      mongo.collections.gcm_received_messages.insert(receivedMessage.asBSON)

      // Send ACK
      actors.gcm ! compact(render(("message_type" -> "ack") ~ ("to" -> receivedMessage.from) ~ ("message_id" -> receivedMessage.messageId)))

      // Send the received message to a handler
      actors.gcm ! receivedMessage
    }
  }

  override def preStart() = {
    log.info("Connecting to CCS")
    xmppConnection = new XMPPTCPConnection(config)
    xmppConnection.addConnectionListener(connectionListener)
    xmppConnection.addPacketListener(packetListener, new PacketTypeFilter(classOf[Message]))
    xmppConnection.connect()
    xmppConnection.login(settings.gcm.senderId + "@gcm.googleapis.com", settings.keys.google)
  }

  override def postStop() = {
    xmppConnection.disconnect()
  }

  def receive = {
    case json: String =>
      log.info("Sending GCM packet {}", json)

      if (xmppConnection.isAuthenticated && !removedFromPool) {
        log.info("Sending packet")
        xmppConnection.sendPacket(new GcmPacketExtension(json).toPacket)
      } else {
        log.info("Just re-queueing")
        actors.gcm ! json
      }

    case ConnectionRemoved =>
      log.info("Removed self = true")
      removedFromPool = true
  }

}

object GcmConnection {

  /**
   * The connection has been removed from the router's pool.
   */
  case class ConnectionRemoved()

  // XMPP additional config
  ProviderManager.addExtensionProvider(ELEMENT_NAME, NAMESPACE, GcmPacketExtension.provider)
  SASLAuthentication.supportSASLMechanism("PLAIN")

}
