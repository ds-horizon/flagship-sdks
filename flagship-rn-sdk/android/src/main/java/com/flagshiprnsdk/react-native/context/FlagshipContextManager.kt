package com.flagshiprnsdk.context

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import dev.openfeature.kotlin.sdk.ImmutableContext
import dev.openfeature.kotlin.sdk.OpenFeatureAPI
import dev.openfeature.kotlin.sdk.Value

object FlagshipContextManager {
  fun setContext(contextMap: ReadableMap) {
    val targetingKey = contextMap.getString("targetingKey") ?: ""
    val contextAttributes = mutableMapOf<String, Value>()

    val iterator = contextMap.keySetIterator()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      if (key == "targetingKey") continue

      val value = convertReadableMapValueToValue(contextMap, key)
      value?.let { contextAttributes[key] = it }
    }

    OpenFeatureAPI.setEvaluationContext(
      ImmutableContext(
        targetingKey = targetingKey,
        contextAttributes
      )
    )
  }

  private fun convertReadableMapValueToValue(map: ReadableMap, key: String): Value? {
    return when (map.getType(key)) {
      ReadableType.Null -> Value.Null
      ReadableType.Boolean -> Value.Boolean(map.getBoolean(key))
      ReadableType.Number -> {
        val number = map.getDouble(key)
        val isWhole = number % 1.0 == 0.0
        if (isWhole) {
          Value.Integer(number.toInt())
        } else {
          Value.Double(number)
        }
      }
      ReadableType.String -> {
        val stringValue = map.getString(key) ?: ""
        Value.String(stringValue)
      }
      ReadableType.Map -> {
        val nestedMap = map.getMap(key)
        convertReadableMapToValue(nestedMap)
      }
      ReadableType.Array -> {
        val array = map.getArray(key)
        convertReadableArrayToValue(array)
      }
    }
  }

  private fun convertReadableMapToValue(readableMap: ReadableMap?): Value {
    if (readableMap == null) return Value.Null
    val iterator = readableMap.keySetIterator()
    val result = mutableMapOf<String, Value>()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      val value = convertReadableMapValueToValue(readableMap, key)
      value?.let { result[key] = it }
    }
    return Value.Structure(result)
  }

  private fun convertReadableArrayToValue(array: ReadableArray?): Value {
    if (array == null) return Value.List(emptyList())
    val list = mutableListOf<Value>()
    for (i in 0 until array.size()) {
      val value = when (array.getType(i)) {
        ReadableType.Null -> Value.Null
        ReadableType.Boolean -> Value.Boolean(array.getBoolean(i))
        ReadableType.Number -> {
          val number = array.getDouble(i)
          val isWhole = number % 1.0 == 0.0
          if (isWhole) {
            Value.Integer(number.toInt())
          } else {
            Value.Double(number)
          }
        }
        ReadableType.String -> Value.String(array.getString(i) ?: "")
        ReadableType.Map -> convertReadableMapToValue(array.getMap(i))
        ReadableType.Array -> convertReadableArrayToValue(array.getArray(i))
      }
      list.add(value)
    }
    return Value.List(list)
  }
}

