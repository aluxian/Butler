package com.butler.cloud.utils

import net.liftweb.json.JsonAST._
import reactivemongo.bson._

/**
 * Convert between JSON and BSON objects.
 */
object BSONFormats {

  def toBSON[T <: BSONValue](json: JValue): T = (json match {
    case JObject(JField("$oid", JString(oid)) :: Nil) => BSONObjectID(oid)
    case JObject(JField("$date", JInt(date)) :: Nil) => BSONDateTime(date.toLong)
    case JObject(JField("$time", JInt(time)) :: Nil) => BSONTimestamp(time.toLong)
    case JObject(JField("$long", JInt(long)) :: Nil) => BSONLong(long.toLong)
    case obj: JObject => BSONDocument(obj.obj.map { field => field.name -> toBSON[BSONValue](field.value)})
    case arr: JArray => BSONArray(arr.arr.map(toBSON))
    case JString(str) => BSONString(str)
    case JDouble(double) => BSONDouble(double)
    case JBool(bool) => BSONBoolean(bool)
    case JInt(num) => BSONInteger(num.toInt)
    case JNull => BSONNull
    case _ => BSONUndefined
  }).asInstanceOf[T]

  def toJSON[T <: JValue](bson: BSONValue): T = (bson match {
    case doc: BSONDocument => JObject(doc.elements.toList.map { elem => JField(elem._1, toJSON[JValue](elem._2))})
    case array: BSONArray => JArray(array.values.toList.map(toJSON))
    case str: BSONString => JString(str.value)
    case double: BSONDouble => JDouble(double.value)
    case boolean: BSONBoolean => JBool(boolean.value)
    case int: BSONInteger => JInt(int.value)
    case long: BSONLong => JInt(long.value)
    case oid: BSONObjectID => JObject(JField("$oid", JString(oid.stringify)) :: Nil)
    case datetime: BSONDateTime => JObject(JField("$date", JInt(datetime.value)) :: Nil)
    case timestamp: BSONTimestamp => JObject(JField("$time", JInt(timestamp.value.toInt)) :: Nil)
    case _ => JNull
  }).asInstanceOf[T]

  /*object BSONRegexFormat extends Format[BSONRegex] {
    val partialReads: PartialFunction[JValue, BSONRegex] = {
      case js: JObject if js.obj.size == 1 && js.obj.head.name == "$regex" => BSONRegex(js.obj.head.value.asInstanceOf[JString].s, "")
      case js: JObject if js.obj.size == 2 && js.obj.exists {_.name == "$regex"} && js.obj.exists {_.name == "$options"} =>
        BSONRegex(js.values("$regex").asInstanceOf[String], js.values("$options").asInstanceOf[String])
    }
    val partialWrites: PartialFunction[BSONValue, JValue] = {
      case rx: BSONRegex =>
        if (rx.flags.isEmpty)
          JObject(JField("$regex", JString(rx.value)) :: Nil)
        else
          JObject(JField("$regex", JString(rx.value)) :: JField("$options", JString(rx.flags)) :: Nil)
    }
  }

  object BSONBinaryFormat extends Format[BSONBinary] {
    val partialReads: PartialFunction[JValue, BSONBinary] = {
      case JString(str) => BSONBinary(Converters.str2Hex(str), Subtype.UserDefinedSubtype)
      case obj: JObject if obj.obj.exists {_.name == "$binary"} =>
        BSONBinary(Converters.str2Hex(obj.values("$binary").asInstanceOf[String]), Subtype.UserDefinedSubtype)
    }
    val partialWrites: PartialFunction[BSONValue, JValue] = {
      case binary: BSONBinary =>
        val remaining = binary.value.readable()
        JObject(
          JField("$binary", JString(Converters.hex2Str(binary.value.slice(remaining).readArray(remaining)))) ::
            JField("$type", JString(Converters.hex2Str(Array(binary.subtype.value)))) :: Nil)
    }
  }*/

}
