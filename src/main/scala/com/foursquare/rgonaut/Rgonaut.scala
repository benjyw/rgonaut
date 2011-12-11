package com.foursquare.rgonaut

import net.liftweb.json.compact
import net.liftweb.json.JsonAST._
import java.io.StringWriter
import org.codehaus.jackson.{JsonFactory, JsonGenerator}


class JsonASTBuilder {
  def JsonObject = new ObjectToAST(Vector.empty)
  def JsonArray = new ArrayToAST(Vector.empty)
}

class JsonGeneratorBuilder(g: JsonGenerator) {
  def JsonObject = new ObjectToGenerator(g)
  def JsonArray = new ArrayToGenerator(g)
}


class ObjectToAST(fields: Vector[JField]) {
  def int(name: String, value: Long): ObjectToAST = addToAST(name, JInt(BigInt(value)))
  def double(name: String, value: Double): ObjectToAST = addToAST(name, JDouble(value))
  def boolean(name: String, value: Boolean): ObjectToAST = addToAST(name, JBool(value))
  def string(name: String, value: String): ObjectToAST = addToAST(name, JString(value))
  def json(name: String, value: ObjectToAST): ObjectToAST = addToAST(name, value.build)
  def array(name: String, value: ArrayToAST): ObjectToAST = addToAST(name, value.build)

  def finish(): JObject = build

  private def addToAST(name: String, value: JValue): ObjectToAST = new ObjectToAST(fields :+ JField(name, value))
  private[rgonaut] def build = new JObject(fields.toList)
}

class ArrayToAST(values: Vector[JValue]) {
  def int(value: Long): ArrayToAST = addToAST(JInt(BigInt(value)))
  def double(value: Double): ArrayToAST = addToAST(JDouble(value))
  def boolean(value: Boolean): ArrayToAST = addToAST(JBool(value))
  def string(value: String): ArrayToAST = addToAST(JString(value))
  def json(value: ObjectToAST): ArrayToAST = addToAST(value.build)
  def array(value: ArrayToAST): ArrayToAST = addToAST(value.build)

  def finish(): JArray = build

  private def addToAST(value: JValue): ArrayToAST = new ArrayToAST(values :+ value)
  private[rgonaut] def build = new JArray(values.toList)
}



class ObjectToGenerator(g: JsonGenerator) {
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


class ArrayToGenerator(g: JsonGenerator) {
  g.writeStartArray();

  def int(value: Long): ArrayToGenerator = { g.writeNumber(value); this }
  def double(value: Double): ArrayToGenerator = { g.writeNumber(value); this }
  def boolean(value: Boolean): ArrayToGenerator = { g.writeBoolean(value); this }
  def string(value: String): ArrayToGenerator = { g.writeString(value); this }
  def json(value: ObjectToGenerator): ArrayToGenerator = { g.writeEndObject(); this }
  def array(value: ArrayToGenerator): ArrayToGenerator = { g.writeEndArray(); this }

  def finish() { g.writeEndArray(); g.close() }
}


object Example {
  def buildJsonAST() = {
    val b = new JsonASTBuilder()

    val obj: JObject =
    (b.JsonObject
        .int("id", 1)
        .string("full_name", "Johnny Foursquare")
        .boolean("verified_email", true)
        .json("address", (b.JsonObject
          .string("street1", "36 Cooper Sq")
          .string("street2", "Floor #6")
          .string("city", "New York")
          .string("state", "NY")
          .string("country", "US")
          .string("zip", "10003"))
        ).array("aliases", b.JsonArray
          .string("foursquare")
          .json(b.JsonObject
            .int("4", 4)
            .int("squared", 16)
           )
          .string("4sq")
          .int(4)
          .string("Foursquare"))
        .finish())

    compact(render(obj)).trim()
  }

  def generateJsonString() = {
    val out = new StringWriter()
    val factory = new JsonFactory()
    // Make the generator strict, so we can verify that we close all objects/arrays properly.
    factory.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false)
    // We call close on the target ourselves.
    factory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
    val b = new JsonGeneratorBuilder(factory.createJsonGenerator(out))

    (b.JsonObject
        .int("id", 1)
        .string("full_name", "Johnny Foursquare")
        .boolean("verified_email", true)
        .json("address", (b.JsonObject
          .string("street1", "36 Cooper Sq")
          .string("street2", "Floor #6")
          .string("city", "New York")
          .string("state", "NY")
          .string("country", "US")
          .string("zip", "10003"))
        ).array("aliases", b.JsonArray
          .string("foursquare")
          .json(b.JsonObject
            .int("4", 4)
            .int("squared", 16)
           )
          .string("4sq")
          .int(4)
          .string("Foursquare"))
        .finish())
    out.toString.trim()
  }

  def main(args: Array[String]) {
    val jsonStringFromAST = buildJsonAST()
    val jsonStringFromGenerator = generateJsonString()
    println(jsonStringFromAST)
    println(jsonStringFromGenerator)
    assert(jsonStringFromAST == jsonStringFromGenerator)
  }
}

