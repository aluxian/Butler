package com.butler.cloud.models.payload

import com.butler.cloud.gcm.requests.ServerMessageRequestTypes.MessageType
import com.butler.cloud.models.data.ResponseData

/**
 * Message sent to a client.
 *
 * @param id The UID of the message.
 * @param messageType The type of the message.
 * @param responseData The response data to send.
 */
case class ServerMessage(id: String,
                         messageType: MessageType,
                         responseData: ResponseData)
