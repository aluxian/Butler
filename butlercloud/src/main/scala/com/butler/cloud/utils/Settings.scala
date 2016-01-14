package com.butler.cloud.utils

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {
  object db {
    object mongo {
      val uri = config.getString("settings.db.mongo.uri")
      val name = config.getString("settings.db.mongo.name")
    }

    object neo4j {
      val uri = config.getString("settings.db.neo4j.uri")
      val user = config.getString("settings.db.neo4j.user")
      val password = config.getString("settings.db.neo4j.password")
    }
  }

  object gcm {
    val debug = config.getBoolean("settings.gcm.debug")
    val server = config.getString("settings.gcm.server")
    val senderId = config.getString("settings.gcm.sender-id")
    val port = config.getInt("settings.gcm.port")
  }

  object urls {
    val corenlpApi = config.getString("settings.urls.corenlp")
    val freebaseApi = config.getString("settings.urls.freebase-api")
    val freebaseUsercontent = config.getString("settings.urls.freebase-usercontent")
    val genderApi = config.getString("settings.urls.gender")
  }

  object keys {
    val google = config.getString("settings.keys.google")
    val gender = config.getString("settings.keys.gender")
  }
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {

  override def lookup() = Settings
  override def createExtension(system: ExtendedActorSystem) = new SettingsImpl(system.settings.config)

  /**
   * Java API: retrieve the Settings extension for the given system.
   */
  override def get(system: ActorSystem): SettingsImpl = super.get(system)

}
