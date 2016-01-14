package com.butler.cloud.models

import com.butler.cloud.utils.{BSONFormats, Json}
import net.liftweb.json.JsonAST.JObject
import reactivemongo.bson.BSONDocument

trait MongoModel {
  def asBSON: BSONDocument = BSONFormats.toBSON[BSONDocument](Json.serialize[JObject](this))
}
