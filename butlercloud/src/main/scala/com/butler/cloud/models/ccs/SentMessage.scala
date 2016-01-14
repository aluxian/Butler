package com.butler.cloud.models.ccs

import com.butler.cloud.models.MongoModel
import com.butler.cloud.utils.Json
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

/**
 * Message sent to CCS.
 *
 * @param to The registration ID to send it to.
 * @param message_id The unique CCS ID of the message.
 * @param data Custom payload.
 * @param ack Whether an ACK message has been received from CCS.
 * @param nack Whether a NACK message has been received from CCS.
 */
case class SentMessage(to: String,
                       message_id: String,
                       data: AnyRef,
                       ack: Boolean = false,
                       nack: Boolean = false) extends MongoModel {
  implicit val formats = DefaultFormats
  def asPacket = compact(render(("to" -> to) ~ ("message_id" -> message_id) ~ ("data" -> Json.stringify(data))))
}
