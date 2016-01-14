package com.butler.cloud.testutils

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.butler.cloud.utils.Configuration
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class Test(_system: ActorSystem) extends TestKit(_system) with TestConfig with WordSpecLike with Matchers with BeforeAndAfterAll with
Configuration
