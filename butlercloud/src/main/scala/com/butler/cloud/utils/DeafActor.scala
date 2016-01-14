package com.butler.cloud.utils

import akka.actor.Actor

/**
 * An actor which doesn't respond to any message. To be extended.
 */
class DeafActor extends Actor {
  override def receive: Receive = {
    case _ =>
  }
}
