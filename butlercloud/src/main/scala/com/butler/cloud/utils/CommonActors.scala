package com.butler.cloud.utils

import akka.actor.ActorContext

/**
 * Holds references to commonly used actors.
 */
trait CommonActors {
  implicit val context: ActorContext

  object actors {
    lazy val gcm = context.actorSelection("/user/gcm")
  }
}
