package com.foursquare.rgonaut

import net.liftweb.json.JsonAST._
import java.io.{StringWriter, Writer}
import org.codehaus.jackson.{JsonFactory, JsonGenerator}

trait Rgonaut {
  type ObjectBuilderT <: RgonautObjectBuilder
  type ArrayBuilderT <: RgonautArrayBuilder
}

trait ToJsonAST extends Rgonaut {
  type ObjectBuilderT = RgonautObjectToJsonAST
  type ArrayBuilderT = RgonautArrayToJsonAST
}

trait ToGenerator extends Rgonaut {
  type ObjectBuilderT = RgonautObjectToGenerator
  type ArrayBuilderT = RgonautArrayToGenerator
}


trait RgonautBuilder[R <: Rgonaut] {
  def JsonObject: R#ObjectBuilderT
  def JsonArray: R#ArrayBuilderT
}

class JsonASTBuilder extends RgonautBuilder[ToJsonAST] {
  def JsonObject = new RgonautObjectToJsonAST(Vector.empty)
  def JsonArray = new RgonautArrayToJsonAST(Vector.empty)
}

class GeneratorBuilder(g: JsonGenerator) extends RgonautBuilder[ToGenerator] {
  def JsonObject = new RgonautObjectToGenerator(g)
  def JsonArray = new RgonautArrayToGenerator(g)
}

trait RgonautObjectBuilder[R <: Rgonaut] {
  type ObjectType

  def int(name: String, value: Long): R#ObjectBuilderT
  def double(name: String, value: Double): R#ObjectBuilderT
  def boolean(name: String, value: Boolean): R#ObjectBuilderT
  def string(name: String, value: String): R#ObjectBuilderT
  def json(name: String, value: R#ObjectBuilderT): R#ObjectBuilderT
  def array(name: String, value: R#ArrayBuilderT): R#ObjectBuilderT
  def build: ObjectType
}

trait RgonautArrayBuilder[R <: Rgonaut] {
  type ArrayType

  def int(value: Long): R#ArrayBuilderT
  def double(value: Double): R#ArrayBuilderT
  def boolean(value: Boolean): R#ArrayBuilderT
  def string(value: String): R#ArrayBuilderT
  def json(value: R#ObjectBuilderT): R#ArrayBuilderT
  def array(value: R#ArrayBuilderT): R#ArrayBuilderT
  def build: ArrayType
}




object Example {
  def generateJsonObject[R <: Rgonaut](b: RgonautBuilder[R]) = {
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
          .string("zip", "10003").json("foo", b.JsonObject.boolean("foo", true)))
        )
        /*.array("aliases", b.JsonArray
          .string("foursquare")
          .json(b.JsonObject
            .int("4", 4)
            .int("squared", 16)
           )
          .string("4sq")
          .int(4)
          .string("Foursquare"))*/
        .build)
  }

  def generatorExample() {
    val out: Writer = new StringWriter()
    generateJsonObject(new GeneratorBuilder(new JsonFactory().createJsonGenerator(out)))
    println(out.toString)
  }

  def main(args: Array[String]) {
    generatorExample()
  }
}





class RgonautObjectToJsonAST(fields: Vector[JField]) extends RgonautObjectBuilder[ToJsonAST] {
  type ObjectType = JObject

  def int(name: String, value: Long): RgonautObjectToJsonAST = addToAST(name, JInt(BigInt(value)))
  def double(name: String, value: Double): RgonautObjectToJsonAST = addToAST(name, JDouble(value))
  def boolean(name: String, value: Boolean): RgonautObjectToJsonAST = addToAST(name, JBool(value))
  def string(name: String, value: String): RgonautObjectToJsonAST = addToAST(name, JString(value))
  def json(name: String, value: RgonautObjectToJsonAST): RgonautObjectToJsonAST = addToAST(name, value.build)
  def array(name: String, value: RgonautArrayToJsonAST): RgonautObjectToJsonAST = addToAST(name, value.build)

  private def addToAST(name: String, value: JValue): RgonautObjectToJsonAST =
    new RgonautObjectToJsonAST(fields :+ JField(name, value))

  def build = new JObject(fields.toList)
}

class RgonautArrayToJsonAST(values: Vector[JValue]) extends RgonautArrayBuilder[ToJsonAST] {
  type ArrayType = JArray

  def int(value: Long): RgonautArrayToJsonAST = addToAST(JInt(BigInt(value)))
  def double(value: Double): RgonautArrayToJsonAST = addToAST(JDouble(value))
  def boolean(value: Boolean): RgonautArrayToJsonAST = addToAST(JBool(value))
  def string(value: String): RgonautArrayToJsonAST = addToAST(JString(value))
  def json(value: RgonautObjectToJsonAST): RgonautArrayToJsonAST = addToAST(value.build)
  def array(value: RgonautArrayToJsonAST): RgonautArrayToJsonAST = addToAST(value.build)

  private def addToAST(value: JValue): RgonautArrayToJsonAST =
    new RgonautArrayToJsonAST(values :+ value)

  def build = new JArray(values.toList)
}

class RgonautObjectToGenerator(g: JsonGenerator) extends RgonautObjectBuilder[ToGenerator] {
  type ObjectType = Unit

  g.writeStartObject();

  def int(name: String, value: Long): RgonautObjectToGenerator = { g.writeNumberField(name, value); this }
  def double(name: String, value: Double): RgonautObjectToGenerator = { g.writeNumberField(name, value); this }
  def boolean(name: String, value: Boolean): RgonautObjectToGenerator = { g.writeBooleanField(name, value); this }
  def string(name: String, value: String): RgonautObjectToGenerator = { g.writeStringField(name, value); this }
  def json(name: String, value: RgonautObjectToGenerator): RgonautObjectToGenerator = { g.writeEndObject(); this }
  def array(name: String, value: RgonautArrayToGenerator): RgonautObjectToGenerator = { g.writeEndArray(); this }

  def build { g.close() }
}

class RgonautArrayToGenerator(g: JsonGenerator) extends RgonautArrayBuilder[ToGenerator] {
  type ArrayType = Unit

  g.writeStartArray();

  def int(value: Long): RgonautArrayToGenerator = { g.writeNumber(value); this }
  def double(value: Double): RgonautArrayToGenerator = { g.writeNumber(value); this }
  def boolean(value: Boolean): RgonautArrayToGenerator = { g.writeBoolean(value); this }
  def string(value: String): RgonautArrayToGenerator = { g.writeString(value); this }
  def json(value: RgonautObjectToGenerator): RgonautArrayToGenerator = { g.writeEndObject(); this }
  def array(value: RgonautArrayToGenerator): RgonautArrayToGenerator = { g.writeEndArray(); this }

  def build { g.close() }
}
