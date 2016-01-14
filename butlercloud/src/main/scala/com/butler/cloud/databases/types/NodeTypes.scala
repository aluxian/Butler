package com.butler.cloud.databases.types

object NodeTypes extends Enumeration {
  type NodeLabel = Value
  val Human = Value("Human")
  val Device = Value("Device")
  val FacebookAccount = Value("FacebookAccount")
  val GoogleAccount = Value("GoogleAccount")
  val TwitterAccount = Value("TwitterAccount")
}
