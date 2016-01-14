package com.butler.cloud.testutils.actors

import akka.actor.{Actor, ActorRef}

/**
 * An actor that forwards every message it receives to the given ActorRef.
 *
 * @param ref The actor to forward messages to.
 */
class ProxyActor(ref: ActorRef) extends Actor {
  def receive = {
    case msg: Any => ref.forward(msg)
  }
}
