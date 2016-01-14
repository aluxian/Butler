package com.butler.cloud.models.data

import com.butler.cloud.models.social.{FacebookAccount, GoogleAccount, TwitterAccount}

/**
 * @param facebookAccount The facebook account of the user.
 * @param targetName The name of the user to whom the message is to be sent.
 * @param requestResponse True if the friend request was accepted, false otherwise.
 * @param message The content of the message.
 * @param timestamp The timestamp when it was recorded.
 */
case class RequestData(facebookAccount: Option[FacebookAccount],
                       googleAccount: Option[GoogleAccount],
                       twitterAccount: Option[TwitterAccount],
                       targetName: Option[String],
                       requestResponse: Option[Boolean],
                       message: Option[String],
                       timestamp: Option[String])
