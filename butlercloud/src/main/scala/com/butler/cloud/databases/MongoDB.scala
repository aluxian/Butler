package com.butler.cloud.databases

import com.butler.cloud.utils.Configured
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection

/**
 * Provides access to the Mongo database.
 */
trait MongoDB extends Configured {
  object mongo {
    lazy val db = configured[DefaultDB]

    object collections {
      lazy val gcm_sent_messages: BSONCollection = db("gcm_sent_messages")
      lazy val gcm_received_messages: BSONCollection = db("gcm_received_messages")
    }
  }
}
