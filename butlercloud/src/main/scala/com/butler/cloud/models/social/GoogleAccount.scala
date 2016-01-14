package com.butler.cloud.models.social

/**
 * @param id The ID of the user.
 * @param givenName User's first name.
 * @param familyName User's last name.
 * @param displayName User's full name.
 * @param gender User's gender.
 */
case class GoogleAccount(id: String,
                         givenName: String,
                         familyName: String,
                         displayName: String,
                         gender: String) extends SocialAccount
