package com.flagship.sdk.facade

import dev.openfeature.kotlin.sdk.Value
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull

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
