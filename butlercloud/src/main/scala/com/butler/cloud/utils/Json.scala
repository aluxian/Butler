package com.butler.cloud.utils

import net.liftweb.json._

/**
 * Utilities for working with lift-json.
 */
object Json {
  implicit val formats = DefaultFormats
  def serialize[JV](any: AnyRef): JV = parse(stringify(any)).asInstanceOf[JV]
  def stringify(any: AnyRef): String = Serialization.write(any)
}
