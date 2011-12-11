package com.foursquare.rgonaut

import net.liftweb.json.JsonAST._
import java.io.{StringWriter, Writer}
import org.codehaus.jackson.{JsonFactory, JsonGenerator}



class JsonASTBuilder {
  def JsonObject = new RgonautObjectToJsonAST(Vector.empty)
  def JsonArray = new RgonautArrayToJsonAST(Vector.empty)
}

class JsonGeneratorBuilder(g: JsonGenerator) {
  def JsonObject = new RgonautObjectToGenerator(g)
  def JsonArray = new RgonautArrayToGenerator(g)
}

class RgonautObjectToJsonAST(fields: Vector[JField]) {
  def int(name: String, value: Long): RgonautObjectToJsonAST = addToAST(name, JInt(BigInt(value)))
  def double(name: String, value: Double): RgonautObjectToJsonAST = addToAST(name, JDouble(value))
  def boolean(name: String, value: Boolean): RgonautObjectToJsonAST = addToAST(name, JBool(value))
  def string(name: String, value: String): RgonautObjectToJsonAST = addToAST(name, JString(value))
  def json(name: String, value: RgonautObjectToJsonAST): RgonautObjectToJsonAST = addToAST(name, value.build)
  def array(name: String, value: RgonautArrayToJsonAST): RgonautObjectToJsonAST = addToAST(name, value.build)

  def finish(): JObject = build

  private def addToAST(name: String, value: JValue): RgonautObjectToJsonAST = new RgonautObjectToJsonAST(fields :+ JField(name, value))

  private[rgonaut] def build = new JObject(fields.toList)
}

class RgonautArrayToJsonAST(values: Vector[JValue]) {
  def int(value: Long): RgonautArrayToJsonAST = addToAST(JInt(BigInt(value)))
  def double(value: Double): RgonautArrayToJsonAST = addToAST(JDouble(value))
  def boolean(value: Boolean): RgonautArrayToJsonAST = addToAST(JBool(value))
  def string(value: String): RgonautArrayToJsonAST = addToAST(JString(value))
  def json(value: RgonautObjectToJsonAST): RgonautArrayToJsonAST = addToAST(value.build)
  def array(value: RgonautArrayToJsonAST): RgonautArrayToJsonAST = addToAST(value.build)

  def finish(): JArray = build

  private def addToAST(value: JValue): RgonautArrayToJsonAST = new RgonautArrayToJsonAST(values :+ value)
  private[rgonaut] def build = new JArray(values.toList)
}

class RgonautObjectToGenerator(g: JsonGenerator) {
  g.writeStartObject();

  def int(name: String, value: Long): RgonautObjectToGenerator = { g.writeNumberField(name, value); this }
  def double(name: String, value: Double): RgonautObjectToGenerator = { g.writeNumberField(name, value); this }
  def boolean(name: String, value: Boolean): RgonautObjectToGenerator = { g.writeBooleanField(name, value); this }
  def string(name: String, value: String): RgonautObjectToGenerator = { g.writeStringField(name, value); this }
  def json(name: String, value: RgonautObjectToGenerator): RgonautObjectToGenerator = { g.writeEndObject(); this }
  def array(name: String, value: RgonautArrayToGenerator): RgonautObjectToGenerator = { g.writeEndArray(); this }

  def finish() { g.close() }
}

class RgonautArrayToGenerator(g: JsonGenerator) {
  g.writeStartArray();

  def int(value: Long): RgonautArrayToGenerator = { g.writeNumber(value); this }
  def double(value: Double): RgonautArrayToGenerator = { g.writeNumber(value); this }
  def boolean(value: Boolean): RgonautArrayToGenerator = { g.writeBoolean(value); this }
  def string(value: String): RgonautArrayToGenerator = { g.writeString(value); this }
  def json(value: RgonautObjectToGenerator): RgonautArrayToGenerator = { g.writeEndObject(); this }
  def array(value: RgonautArrayToGenerator): RgonautArrayToGenerator = { g.writeEndArray(); this }

  def finish() { g.close() }
}


object Example {
  def buildJsonAST() = {
    val b = new JsonASTBuilder()

    val res: JObject =
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
  }

  def generateJsonString() {
    val out = new StringWriter()
    val b = new JsonGeneratorBuilder(new JsonFactory().createJsonGenerator(out))

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
    println(out.toString)
  }

  def main(args: Array[String]) {
    generateJsonString()
  }
}


