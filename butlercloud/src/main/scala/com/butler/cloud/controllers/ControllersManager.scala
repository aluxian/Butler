package com.butler.cloud.controllers

import akka.actor._
import akka.routing.FromConfig
import com.butler.cloud.gcm.requests.{ClientMessageRequestTypes => Types}
import com.butler.cloud.utils.DeafActor

/**
 * Holds all the controller actors. They must be configured in deployment.conf.
 */
class ControllersManager extends DeafActor {
  Map(
    classOf[AuthRequestCtrl] -> Types.AuthRequest,
    classOf[FriendRequestCtrl] -> Types.FriendRequest
  ).foreach {
    case (cls, requestType) => context.actorOf(FromConfig.props(Props(cls)), requestType.toString)
  }
}
