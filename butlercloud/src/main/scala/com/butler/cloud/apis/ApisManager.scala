package com.butler.cloud.apis

import akka.actor.{Actor, Props}
import akka.routing.FromConfig
import com.butler.cloud.utils.DeafActor

/**
 * Actor that supervises different API clients. It doesn't receive any messages.
 */
class ApisManager extends DeafActor {
  context.actorOf(FromConfig.props(Props[CoreNlpApiClient]), "corenlp")
  context.actorOf(FromConfig.props(Props[FreebaseApiClient]), "freebase")
  context.actorOf(FromConfig.props(Props[GenderApiClient]), "gender")
}
