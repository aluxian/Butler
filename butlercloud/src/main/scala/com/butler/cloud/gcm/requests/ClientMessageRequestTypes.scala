package com.butler.cloud.gcm.requests

/**
 * Request types for ClientMessage objects.
 */
object ClientMessageRequestTypes extends Enumeration {
  type RequestType = Value

  val AuthRequest = Value("AUTH_REQUEST")
  val FriendRequest = Value("FRIEND_REQUEST")
  val FriendRequestResponse = Value("FRIEND_REQUEST_RESPONSE")
  val TellMessage = Value("TELL_MESSAGE")
  val TrackEvent = Value("TRACK_EVENT")
  val UserInput = Value("USER_INPUT")
}
