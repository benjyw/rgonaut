package com.foursquare.rgonaut

import org.codehaus.jackson.{JsonGenerator, JsonFactory}
import net.liftweb.json.{compact, render}
import java.io.{Writer, StringWriter}
import net.liftweb.json.JsonAST.{JArray, JObject}


object Example {
  def buildExampleJsonObject[JO <: JsonObject[JO, JA], JA <: JsonArray[JO, JA]](b: JsonBuilder[JO, JA]): JO = {
    b.jsonObject
        .int("id", 1)
        .string("full_name", "Johnny Foursquare")
        .boolean("verified_email", true)
        .json("address", (b.jsonObject
          .string("street1", "36 Cooper Sq")
          .string("street2", "Floor #6")
          .string("city", "New York")
          .string("state", "NY")
          .string("country", "US")
          .string("zip", "10003"))
        ).array("aliases", b.jsonArray
          .string("foursquare")
          .json(b.jsonObject
            .int("4", 4)
            .int("squared", 16)
           )
          .string("4sq")
          .int(4)
          .string("Foursquare"))
  }

  def buildExampleJsonArray[JO <: JsonObject[JO, JA], JA <: JsonArray[JO, JA]](b: JsonBuilder[JO, JA]): JA = {
    b.jsonArray
      .int(55)
      .string("hello")
      .json(b.jsonObject)  // Empty
      .boolean(false)
      .array(b.jsonArray
        .int(7)
        .double(3.14)
        .json(b.jsonObject
          .string("foo", "bar")
          .array("empty", b.jsonArray))
        .boolean(true))
      .int(123)
  }


  def buildExampleJsonObjectAST() = {
    val b = new JsonASTBuilder()
    val obj: JObject = buildExampleJsonObject(b).finish()
    compact(render(obj)).trim()
  }

  def buildExampleJsonArrayAST() = {
    val b = new JsonASTBuilder()
    val arr: JArray = buildExampleJsonArray(b).finish()
    compact(render(arr)).trim()
  }

  def createGenerator(out: Writer) = {
    val factory = new JsonFactory()  // In production code this should be long-lived and shared across multiple threads.
    // Make the generator strict, so we can verify that we close all objects/arrays properly.
    factory.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false)
    // We call close on the target ourselves.
    factory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
    factory.createJsonGenerator(out)
  }

  def generateExampleJsonObjectString() = {
    val out = new StringWriter()
    val b = new JsonGeneratorBuilder(createGenerator(out))
    buildExampleJsonObject(b).finish()
    out.toString.trim()
  }

  def generateExampleJsonArrayString() = {
    val out = new StringWriter()
    val b = new JsonGeneratorBuilder(createGenerator(out))
    buildExampleJsonArray(b).finish()
    out.toString.trim()
  }

  def compareStrings(fromAST: String, fromGenerator: String) {
    println(fromAST)
    println(fromGenerator)
    assert(fromAST == fromGenerator)
  }

  def main(args: Array[String]) {
    compareStrings(buildExampleJsonObjectAST(), generateExampleJsonObjectString())
    compareStrings(buildExampleJsonArrayAST(), generateExampleJsonArrayString())
  }
}
