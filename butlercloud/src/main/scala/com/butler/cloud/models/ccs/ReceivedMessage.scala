package com.butler.cloud.models.ccs

import com.butler.cloud.models.MongoModel
import com.butler.cloud.models.payload.ClientMessage

/**
 * Message received from CCS.
 *
 * @param category App package name.
 * @param messageId The unique CCS ID of the message.
 * @param from The registration ID of the sender.
 * @param data Custom payload.
 * @param to Only for ACK/NACK messages.
 * @param messageType ACK or NACK, null for payload messages.
 * @param error The error code (if any).
 * @param errorDescription The description of the error (if any).
 * @param controlType The control type for control messages.
 */
case class ReceivedMessage(category: String,
                           messageId: String,
                           from: String,
                           data: ClientMessage,
                           to: String,
                           messageType: String,
                           error: String,
                           errorDescription: String,
                           controlType: String) extends MongoModel
