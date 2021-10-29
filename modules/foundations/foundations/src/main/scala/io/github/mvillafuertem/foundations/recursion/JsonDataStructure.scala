package io.github.mvillafuertem.foundations.recursion

object JsonDataStructure {

  sealed trait Json
  case class JsonNumber(number: Double)         extends Json
  case class JsonBoolean(bool: Boolean)         extends Json
  case class JsonString(text: String)           extends Json
  case class JsonObject(obj: Map[String, Json]) extends Json
  case class JsonArray(array: List[Json])       extends Json
  case object JsonNull                          extends Json

  def trimAll(json: Json): Json =
    json match {
      case _: JsonNumber | _: JsonBoolean => json
      case JsonString(str)                => JsonString(str.trim)
      case JsonObject(obj)                =>
        JsonObject(obj.map { case (key, value) =>
          key -> trimAll(value)
        })
      case JsonArray(array)               => JsonArray(array.map(trimAll))
      case JsonNull                       => JsonNull
    }

  // a. Implement `anonymize`, a method which keeps the structure of the JSON document
  // but removes all data such as:
  // * all `JsonString` are replaced by `***`.
  // * all `JsonNumbers` are replaced by 0
  // For example:
  // {                                          {
  //  "name": "John Doe",                         "name": "***",
  //  "age": 25,                                  "age": 0,
  //  "address": {                                "address": {
  //    "street": {             anonymize           "street": {
  //      "number" : 12,           ==>                "number" : 0,
  //      "name" : "Cody road"                        "name" : "***"
  //    },                                          },
  //    "country": "UK",                            "country": "***",
  //  }                                           }
  // }                                           }
  def anonymize(json: Json): Json =
    json match {
      case _: JsonNumber    => JsonNumber(0)
      case _: JsonBoolean   => json
      case _: JsonString    => JsonString("***")
      case JsonObject(obj)  =>
        JsonObject(obj.map { case (key, value) =>
          key -> anonymize(value)
        })
      case JsonArray(array) => JsonArray(array.map(anonymize))
      case JsonNull         => JsonNull
    }

  // b. Implement `search`, a method that checks if a JSON document contains a text.
  // Note: `search` doesn't look inside of the keys of a `JsonObject`, only the values.
  // For example:
  // * search({ }, "ll") == false
  // * search(5, "ll") == false
  // * search("Hello", "ll") == true
  // * search({ "message" : "hello" }, "ll") == true
  // * search({ "message" : "hi" }, "ll") == false
  def search(json: Json, searchText: String, maxDepth: Int): Boolean =
    if (maxDepth < 0) false
    else
      json match {
        case _: JsonNumber | _: JsonBoolean | JsonNull => false
        case JsonString(text)                          => text.contains(searchText)
        case JsonObject(obj)                           => obj.values.exists(search(_, searchText, maxDepth - 1))
        case JsonArray(array)                          => array.exists(search(_, searchText, maxDepth - 1))
      }

  // c. Implement `depth`, a method that calculates the maximum level of nesting of a JSON document.
  // For example:
  // * { }, 5 or "hello" have depth 0
  // * { "name" : "john" } has depth 1
  // * { "name" : "john", "address" : { "postcode" : "E16 4SR" } } has depth 2
  def depth(json: Json): Int =
    json match {
      case _: JsonNumber | _: JsonString | _: JsonBoolean | JsonNull =>
        0
      case JsonObject(obj)                                           =>
        obj.values.map(depth).maxOption.fold(0)(_ + 1)
      case JsonArray(array)                                          =>
        array.map(depth).maxOption.fold(0)(_ + 1)
    }

  // ////////////////////////////////////////////
  // Bonus question (not covered by the video)
  // ////////////////////////////////////////////

  // d. Add the missing cases to JSON enumeration: JsonBoolean, JsonArray and JsonNull.

  // e. add an extra parameter to search so that it limits the depth of the search.
  // such as search({ "user" : { "name" : "John" } }, "o", 2) == true
  // but     search({ "user" : { "name" : "John" } }, "o", 1) == false because "John" is at depth 2

}
