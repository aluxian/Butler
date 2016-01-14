package com.butler.cloud.gcm.handlers

import akka.actor._
import akka.dispatch.ControlMessage
import akka.routing.{ActorRefRoutee, DefaultResizer, RoundRobinRoutingLogic, Routee}
import com.butler.cloud.gcm.GcmConnection
import com.butler.cloud.gcm.GcmConnection.ConnectionRemoved
import com.butler.cloud.gcm.handlers.GcmConnectionsRouter.{PoisonMe, RemoveConnection}

import scala.collection.{immutable, mutable}

/**
 * Manages and routes messages to a pool of actors connected to CCS.
 */
class GcmConnectionsRouter extends Actor with ActorLogging {

  // TODO: Find a way to resize the pool depending on the messages throughput.

  val resizer = DefaultResizer(2, 5, messagesPerResize = 100)
  val roundRobin = RoundRobinRoutingLogic()
  var pool = mutable.Stack[ActorRef]()
  var size = 3

  def balancePool() = {
    while (pool.size > size) pool.pop() ! PoisonPill
    while (pool.size < size) pool.push(context.actorOf(Props[GcmConnection]))
  }

  def receive = {
    case RemoveConnection =>
      log.info("Removing connection of actor {}", sender())
      pool = pool.diff(Seq(sender()))
      sender() ! ConnectionRemoved
      balancePool()

    case PoisonMe =>
      sender() ! PoisonPill

    case msg: Any =>
      roundRobin.select(msg, immutable.IndexedSeq[Routee](pool.map(ActorRefRoutee): _*))
      balancePool()
  }

  override def preStart() = {
    balancePool()
  }

}

object GcmConnectionsRouter {

  /**
   * Remove the given actor from the pool.
   */
  case class RemoveConnection() extends ControlMessage

  /**
   * Send the given actor a PoisonPill. Usually sent after a RemoveConnection message. GcmConnection actors need to remove themselves
   * from the router and get killed by it in order to make sure that no messages are lost.
   */
  case class PoisonMe()

}
