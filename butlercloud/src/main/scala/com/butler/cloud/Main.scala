package com.butler.cloud

import akka.actor.{ActorSystem, Props}
import com.butler.cloud.apis.ApisManager
import com.butler.cloud.gcm.GcmManager
import com.butler.cloud.processing.ProcessingManager
import com.butler.cloud.utils.{Configuration, Configured, Settings}
import org.neo4j.rest.graphdb.RestGraphDatabase
import reactivemongo.api.{DefaultDB, MongoDriver}

import scala.concurrent.ExecutionContext.Implicits.global

object Main {

  class MainSystem extends Configuration with Configured {
    val system = ActorSystem("ButlerCloudSystem")
    val settings = Settings(system)

    system.log.info("Starting system...")

    configure {MongoDriver().connection(List(settings.db.mongo.uri))(settings.db.mongo.name)}
    configure {
      System.setProperty("org.neo4j.rest.batch_transaction", "false")
      new RestGraphDatabase(settings.db.neo4j.uri)
    }

    system.actorOf(Props[GcmManager], "gcm")
    system.actorOf(Props[ApisManager], "apis")
    system.actorOf(Props[ProcessingManager], "processing")

    sys.addShutdownHook {
      system.log.warning("Closing databases")
      configured[DefaultDB].connection.close()
      configured[RestGraphDatabase].shutdown()

      system.log.warning("Shutting down the system")
      system.terminate()
    }
  }

  def main(args: Array[String]) {
    new MainSystem()
  }

}
