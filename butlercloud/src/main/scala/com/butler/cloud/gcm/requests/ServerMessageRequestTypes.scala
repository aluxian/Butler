package com.butler.cloud.gcm.requests

/**
 * Request types for ServerMessage objects.
 */
object ServerMessageRequestTypes extends Enumeration {
  type MessageType = Value

  val AUTH_RESPONSE = Value("AUTH_RESPONSE")
  val FRIEND_REQUEST = Value("FRIEND_REQUEST")
  val FRIEND_REQUEST_RESPONSE = Value("FRIEND_REQUEST_RESPONSE")
  val FRIEND_REQUEST_NOT_FOUND = Value("FRIEND_REQUEST_NOT_FOUND")
  val TELL_MESSAGE = Value("TELL_MESSAGE")
  val PROCESSED_OUTPUT = Value("PROCESSED_OUTPUT")
}
