package com.butler.cloud.databases.types

object RelTypes extends Enumeration {
  type RelationshipLabel = Value
  val PairedWith = Value("paired_with")
  val ConnectedTo = Value("connected_to")
}
