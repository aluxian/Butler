package com.butler.cloud.models.social

case class TwitterAccount(id: String,
                          name: String,
                          location: String,
                          lang: String) extends SocialAccount
