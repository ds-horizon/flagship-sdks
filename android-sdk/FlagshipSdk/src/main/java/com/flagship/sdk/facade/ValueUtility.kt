package com.flagship.sdk.facade

import dev.openfeature.kotlin.sdk.Value
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

fun jsonToValue(json: String): Value =
    when (val element = Json.parseToJsonElement(json)) {
        is JsonObject -> {
            val map = mutableMapOf<String, Value>()
            element.entries.forEach { (key, value) ->
                map[key] = jsonToValue(value.toString())
            }
            Value.Structure(map)
        }

        is JsonArray -> {
            val list = mutableListOf<Value>()
            element.forEach { value ->
                list.add(jsonToValue(value.toString()))
            }
            Value.List(list)
        }

        is kotlinx.serialization.json.JsonPrimitive -> {
            when {
                element.isString -> Value.String(element.content)
                element.booleanOrNull != null -> Value.Boolean(element.boolean)
                element.intOrNull != null -> Value.Integer(element.int)
                else -> {
                    Value.String(element.toString())
                }
            }
        }
    }.runCatching {
        this
    }.getOrElse {
        Value.Null
    }

fun valueToJson(value: Value): JsonObject =
    when (value) {
        is Value.Structure -> {
            val structure = value.asStructure()
            if (structure == null) {
                buildJsonObject { }
            } else {
                buildJsonObject {
                    structure.forEach { (key, nestedValue) ->
                        put(key, valueToJsonElement(nestedValue))
                    }
                }
            }
        }
        else -> {
            buildJsonObject {
                put("key", valueToJsonElement(value))
            }
        }
    }

@OptIn(ExperimentalSerializationApi::class)
private fun valueToJsonElement(value: Value): kotlinx.serialization.json.JsonElement =
    when (value) {
        is Value.Structure -> {
            val structure = value.asStructure()
                ?: throw IllegalArgumentException("Value.Structure returned null")
            buildJsonObject {
                structure.forEach { (key, nestedValue) ->
                    put(key, valueToJsonElement(nestedValue))
                }
            }
        }
        is Value.List -> {
            val list = value.asList()
                ?: throw IllegalArgumentException("Value.List returned null")
            buildJsonArray {
                list.forEach { item ->
                    add(valueToJsonElement(item))
                }
            }
        }
        is Value.String -> JsonPrimitive(value.asString())
        is Value.Boolean -> JsonPrimitive(value.asBoolean())
        is Value.Integer -> JsonPrimitive(value.asInteger())
        is Value.Double -> JsonPrimitive(value.asDouble())
        is Value.Instant -> {
            @OptIn(kotlin.time.ExperimentalTime::class)
            JsonPrimitive(value.asInstant().toString())
        }
        is Value.Null -> JsonPrimitive(null)
    }
