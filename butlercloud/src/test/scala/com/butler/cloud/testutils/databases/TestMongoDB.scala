package com.butler.cloud.testutils.databases

import com.butler.cloud.databases.MongoDB
import com.butler.cloud.testutils.TestConfig
import com.butler.cloud.utils.Configuration
import reactivemongo.api.MongoDriver

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

trait TestMongoDB extends MongoDB with Configuration with TestConfig {

  def mongoBeforeAll() {
    configure {MongoDriver().connection(List(settings.db.mongo.uri))(settings.db.mongo.name)}
    Await.result(mongo.db.connection.waitForPrimary, atMost)
  }

  def mongoAfterAll() {
    Await.result(mongo.db.drop(), atMost)
    mongo.db.connection.close()
  }

}
