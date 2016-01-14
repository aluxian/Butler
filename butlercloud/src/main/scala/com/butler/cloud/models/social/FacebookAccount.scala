package com.butler.cloud.models.social

/**
 * @param id The ID of the user.
 * @param firstName User's first name.
 * @param lastName User's last name.
 * @param name User's full name.
 * @param gender User's gender.
 * @param link Link to the user's profile.
 * @param locale User's locale.
 * @param timezone User's timezone.
 * @param token The authentication token.
 */
case class FacebookAccount(id: String,
                           firstName: String,
                           lastName: String,
                           name: String,
                           gender: String,
                           link: String,
                           locale: String,
                           timezone: Int,
                           token: String) extends SocialAccount
