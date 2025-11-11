package com.flagshipfeatureflagssdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import dev.openfeature.kotlin.sdk.Value

class FlagshipFeatureFlagsSdkModuleImpl {
  fun multiply(
    a: Double,
    b: Double,
  ): Double = a + b

  fun getBooleanValue(
    key: String?,
    defaultValue: Boolean,
    promise: Promise?,
  ) {
    try {
      FlagShipClientManager.openFeatureClient?.let { openfeatureClient ->
        key?.let {
          promise?.resolve(openfeatureClient.getBooleanValue(key, defaultValue))
        } ?: kotlin.run {
          promise?.reject(KEY_NULL, "Key is null")
        }
      } ?: kotlin.run {
        promise?.reject(CLIENT_NULL, "Client is null")
      }
    } catch (throwable: Throwable) {
      promise?.reject(throwable)
    }
  }

  fun getStringValue(
    key: String?,
    defaultValue: String?,
    promise: Promise?,
  ) {
    try {
      FlagShipClientManager.openFeatureClient?.let { openfeatureClient ->
        key?.let {
          val safeDefault = defaultValue ?: ""
          promise?.resolve(openfeatureClient.getStringValue(key, safeDefault))
        } ?: kotlin.run {
          promise?.reject(KEY_NULL, "Key is null")
        }
      } ?: kotlin.run {
        promise?.reject(CLIENT_NULL, "Client is null")
      }
    } catch (throwable: Throwable) {
      promise?.reject(throwable)
    }
  }

  fun getIntegerValue(
    key: String?,
    defaultValue: Double,
    promise: Promise?,
  ) {
    try {
      FlagShipClientManager.openFeatureClient?.let { openfeatureClient ->
        key?.let {
          val defaultInt = defaultValue.toInt()
          val result = openfeatureClient.getIntegerValue(key, defaultInt)
          val asInt =
            when (result) {
              is Int -> result
              is Long -> result.toInt()
              else -> defaultInt
            }
          promise?.resolve(asInt)
        } ?: kotlin.run {
          promise?.reject(KEY_NULL, "Key is null")
        }
      } ?: kotlin.run {
        promise?.reject(CLIENT_NULL, "Client is null")
      }
    } catch (throwable: Throwable) {
      promise?.reject(throwable)
    }
  }

  fun getDoubleValue(
    key: String?,
    defaultValue: Double,
    promise: Promise?,
  ) {
    try {
      FlagShipClientManager.openFeatureClient?.let { openfeatureClient ->
        key?.let {
          promise?.resolve(openfeatureClient.getDoubleValue(key, defaultValue))
        } ?: kotlin.run {
          promise?.reject(KEY_NULL, "Key is null")
        }
      } ?: kotlin.run {
        promise?.reject(CLIENT_NULL, "Client is null")
      }
    } catch (throwable: Throwable) {
      promise?.reject(throwable)
    }
  }

  fun getObjectValue(
    key: String?,
    defaultValue: ReadableMap?,
    promise: Promise?,
  ) {
    try {
      FlagShipClientManager.openFeatureClient?.let { openfeatureClient ->
        key?.let {
          val defaultObject = convertReadableMapToValue(defaultValue)
          val result = openfeatureClient.getObjectValue(key, defaultObject)
          val reactValue = convertValueToReact(result)
          promise?.resolve(reactValue)
        } ?: kotlin.run {
          promise?.reject(KEY_NULL, "Key is null")
        }
      } ?: kotlin.run {
        promise?.resolve(defaultValue)
      }
    } catch (throwable: Throwable) {
      promise?.reject(throwable)
    }
  }

  private fun convertReadableMapToValue(map: ReadableMap?): Value {
    if (map == null) return Value.Null
    val iterator = map.keySetIterator()
    val result = mutableMapOf<String, Value>()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      when (map.getType(key)) {
        ReadableType.Null -> result[key] = Value.Null
        ReadableType.Boolean -> result[key] = Value.Boolean(map.getBoolean(key))
        ReadableType.Number -> {
          val number = map.getDouble(key)
          val isWhole = number % 1.0 == 0.0
          result[key] = if (isWhole) Value.Integer(number.toInt()) else Value.Double(number)
        }
        ReadableType.String -> result[key] = Value.String(map.getString(key) ?: "")
        ReadableType.Map -> result[key] = convertReadableMapToValue(map.getMap(key))
        ReadableType.Array -> result[key] = convertReadableArrayToValue(map.getArray(key))
      }
    }
    return Value.Structure(result)
  }

  private fun convertReadableArrayToValue(array: ReadableArray?): Value {
    if (array == null) return Value.List(emptyList())
    val list = mutableListOf<Value>()
    for (i in 0 until array.size()) {
      when (array.getType(i)) {
        ReadableType.Null -> list.add(Value.Null)
        ReadableType.Boolean -> list.add(Value.Boolean(array.getBoolean(i)))
        ReadableType.Number -> {
          val number = array.getDouble(i)
          val isWhole = number % 1.0 == 0.0
          list.add(if (isWhole) Value.Integer(number.toInt()) else Value.Double(number))
        }
        ReadableType.String -> list.add(Value.String(array.getString(i) ?: ""))
        ReadableType.Map -> list.add(convertReadableMapToValue(array.getMap(i)))
        ReadableType.Array -> list.add(convertReadableArrayToValue(array.getArray(i)))
      }
    }
    return Value.List(list)
  }

  private fun convertValueToReact(value: Value): Any? =
    when (value) {
      is Value.Null -> null
      is Value.String -> value.string
      is Value.Boolean -> value.boolean
      is Value.Double -> value.double
      is Value.Integer -> value.integer
      is Value.List -> {
        val arr = WritableNativeArray()
        value.list.forEach { v ->
          when (val child = convertValueToReact(v)) {
            null -> arr.pushNull()
            is String -> arr.pushString(child)
            is Boolean -> arr.pushBoolean(child)
            is Int -> arr.pushInt(child)
            is Double -> arr.pushDouble(child)
            is WritableNativeMap -> arr.pushMap(child)
            is WritableNativeArray -> arr.pushArray(child)
            else -> arr.pushNull()
          }
        }
        arr
      }
      is Value.Structure -> {
        val map = WritableNativeMap()
        value.structure.forEach { (k, v) ->
          when (val child = convertValueToReact(v)) {
            null -> map.putNull(k)
            is String -> map.putString(k, child)
            is Boolean -> map.putBoolean(k, child)
            is Int -> map.putInt(k, child)
            is Double -> map.putDouble(k, child)
            is WritableNativeMap -> map.putMap(k, child)
            is WritableNativeArray -> map.putArray(k, child)
            else -> map.putNull(k)
          }
        }
        map
      }

      else -> {
        val map = WritableNativeMap()
        map
      }
    }

  companion object {
    const val NAME = "FlagshipFeatureFlagsSdk"
  }
}
