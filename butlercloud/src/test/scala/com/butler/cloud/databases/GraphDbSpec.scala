package com.butler.cloud.databases

import java.net.Socket

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.butler.cloud.testutils.Test
import com.butler.cloud.testutils.databases.TestGraphDB
import com.sun.jndi.toolkit.url.Uri

class GraphDbSpec(_system: ActorSystem) extends Test(_system) with TestGraphDB {

  def this() = this(ActorSystem("GraphDbSpec"))

  override def beforeAll() = {
    graphBeforeAll()
  }

  override def afterAll() {
    graphAfterAll()
    TestKit.shutdownActorSystem(system)
  }

  "GraphDb" must {

    "connect successfully" in {
      val uri = new Uri(settings.db.neo4j.uri)
      new Socket(uri.getHost, uri.getPort).close()
    }

  }

}
