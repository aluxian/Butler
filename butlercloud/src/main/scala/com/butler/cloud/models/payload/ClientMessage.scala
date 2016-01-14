package com.butler.cloud.models.payload

import com.butler.cloud.gcm.requests.ClientMessageRequestTypes.RequestType
import com.butler.cloud.models.ccs.ReceivedMessage
import com.butler.cloud.models.data.RequestData

/**
 * Message received from a client.
 *
 * @param id The UID of the message.
 * @param userId The unique ID of the user.
 * @param deviceId The UID of the client device.
 * @param authToken Authentication token sent by the client.
 * @param requestType The type of the request.
 * @param requestData A RequestData object.
 */
case class ClientMessage(id: String,
                         userId: String,
                         deviceId: String,
                         authToken: String,
                         requestType: RequestType,
                         requestData: RequestData,
                         var receivedMessage: Option[ReceivedMessage] = None)
