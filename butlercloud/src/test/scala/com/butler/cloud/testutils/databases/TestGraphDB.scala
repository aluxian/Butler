package com.butler.cloud.testutils.databases

import akka.actor.ActorSystem
import com.butler.cloud.databases.GraphDB
import com.butler.cloud.utils.{Configuration, SettingsImpl}
import org.neo4j.rest.graphdb.RestGraphDatabase
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine

trait TestGraphDB extends GraphDB with Configuration {

  implicit val system: ActorSystem
  implicit val settings: SettingsImpl

  def graphBeforeAll() {
    configure {
      new RestGraphDatabase(settings.db.neo4j.uri)
    }
  }

  def graphAfterAll() {
    new RestCypherQueryEngine(configured[RestGraphDatabase].getRestAPI).query("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n;", null)
    neo4j.shutdown()
  }

}
