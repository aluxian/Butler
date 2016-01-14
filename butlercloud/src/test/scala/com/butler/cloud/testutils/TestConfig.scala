package com.butler.cloud.testutils

import akka.actor.ActorSystem
import akka.util.Timeout
import com.butler.cloud.utils.Settings

import scala.concurrent.duration._

trait TestConfig {

  implicit val system: ActorSystem

  val settings = Settings(system)
  val atMost = 5.seconds
  val delay = 50

  implicit val timeout = Timeout(atMost)
  implicit val waitForAvailability = atMost

}
