package com.foursquare.rgonaut

import org.codehaus.jackson.JsonGenerator


class JsonGeneratorBuilder(g: JsonGenerator) extends JsonBuilder[ObjectToGenerator, ArrayToGenerator] {
  def jsonObject = new ObjectToGenerator(g)
  def jsonArray = new ArrayToGenerator(g)
}

class ObjectToGenerator(g: JsonGenerator) extends JsonObject[ObjectToGenerator, ArrayToGenerator] {
  type O = Unit  // Output goes straight to the generator.

  g.writeStartObject();

  def int(name: String, value: Long): ObjectToGenerator = { g.writeNumberField(name, value); this }
  def double(name: String, value: Double): ObjectToGenerator = { g.writeNumberField(name, value); this }
  def boolean(name: String, value: Boolean): ObjectToGenerator = { g.writeBooleanField(name, value); this }
  def string(name: String, value: String): ObjectToGenerator = { g.writeStringField(name, value); this }
  def json(name: String, value: => ObjectToGenerator): ObjectToGenerator = {
    g.writeFieldName(name)
    value
    g.writeEndObject();
    this
  }
  def array(name: String, value: => ArrayToGenerator): ObjectToGenerator = {
    g.writeFieldName(name)
    value
    g.writeEndArray();
    this
  }

  def finish() { g.writeEndObject(); g.close() }
}

class ArrayToGenerator(g: JsonGenerator) extends JsonArray[ObjectToGenerator, ArrayToGenerator] {
  type A = Unit  // Output goes straight to the generator.

  g.writeStartArray();

  def int(value: Long): ArrayToGenerator = { g.writeNumber(value); this }
  def double(value: Double): ArrayToGenerator = { g.writeNumber(value); this }
  def boolean(value: Boolean): ArrayToGenerator = { g.writeBoolean(value); this }
  def string(value: String): ArrayToGenerator = { g.writeString(value); this }
  def json(value: => ObjectToGenerator): ArrayToGenerator = { value; g.writeEndObject(); this }
  def array(value: => ArrayToGenerator): ArrayToGenerator = { value; g.writeEndArray(); this }

  def finish() { g.writeEndArray(); g.close() }
}

