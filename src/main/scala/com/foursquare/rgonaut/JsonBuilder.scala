package com.foursquare.rgonaut


trait JsonBuilder[JO <: JsonObject[JO, JA], JA <: JsonArray[JO, JA]] {
  def jsonObject: JO
  def jsonArray: JA
}

trait JsonObject[JO <: JsonObject[JO, JA], JA <: JsonArray[JO, JA]] {
  type O  // The underlying type we build.

  def int(name: String, value: Long): JO
  def double(name: String, value: Double): JO
  def boolean(name: String, value: Boolean): JO
  def string(name: String, value: String): JO
  def json(name: String, value: => JO): JO
  def array(name: String, value: => JA): JO

  def finish(): O
}

trait JsonArray[JO <: JsonObject[JO, JA], JA <: JsonArray[JO, JA]] {
  type A  // The underlying type we build.

  def int(value: Long): JA
  def double(value: Double): JA
  def boolean(value: Boolean): JA
  def string(value: String): JA
  def json(value: => JO): JA
  def array(value: => JA): JA

  def finish(): A
}

