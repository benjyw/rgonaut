package com.foursquare.rgonaut

import net.liftweb.json.JsonAST._


class JsonASTBuilder extends JsonBuilder[ObjectToAST, ArrayToAST] {
  def jsonObject = new ObjectToAST(Vector.empty)
  def jsonArray = new ArrayToAST(Vector.empty)
}

class ObjectToAST(fields: Vector[JField]) extends JsonObject[ObjectToAST, ArrayToAST] {
  type O = JObject

  def int(name: String, value: Long): ObjectToAST = addToAST(name, JInt(BigInt(value)))
  def double(name: String, value: Double): ObjectToAST = addToAST(name, JDouble(value))
  def boolean(name: String, value: Boolean): ObjectToAST = addToAST(name, JBool(value))
  def string(name: String, value: String): ObjectToAST = addToAST(name, JString(value))
  def json(name: String, value: => ObjectToAST): ObjectToAST = addToAST(name, value.build)
  def array(name: String, value: => ArrayToAST): ObjectToAST = addToAST(name, value.build)

  def finish(): JObject = build

  private def addToAST(name: String, value: JValue): ObjectToAST = new ObjectToAST(fields :+ JField(name, value))
  private[rgonaut] def build = new JObject(fields.toList)
}

class ArrayToAST(values: Vector[JValue]) extends JsonArray[ObjectToAST, ArrayToAST] {
  type A = JArray

  def int(value: Long): ArrayToAST = addToAST(JInt(BigInt(value)))
  def double(value: Double): ArrayToAST = addToAST(JDouble(value))
  def boolean(value: Boolean): ArrayToAST = addToAST(JBool(value))
  def string(value: String): ArrayToAST = addToAST(JString(value))
  def json(value: => ObjectToAST): ArrayToAST = addToAST(value.build)
  def array(value: => ArrayToAST): ArrayToAST = addToAST(value.build)

  def finish(): JArray = build

  private def addToAST(value: JValue): ArrayToAST = new ArrayToAST(values :+ value)
  private[rgonaut] def build = new JArray(values.toList)
}
