package com.flagshiprnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = FlagshipRnSdkModuleImpl.NAME)
class FlagshipRnSdkModule(
  reactContext: ReactApplicationContext,
) : NativeFlagshipRnSdkSpec(reactContext) {
  private val flagshipRnSdkModuleImpl: FlagshipRnSdkModuleImpl = FlagshipRnSdkModuleImpl(reactContext)

  override fun getName(): String = NAME

  override fun multiply(a: Double, b: Double): Double = flagshipRnSdkModuleImpl.multiply(a, b)

  override fun initialize(config: ReadableMap, promise: Promise?) {
    flagshipRnSdkModuleImpl.initialize(config, promise)
  }

  override fun setContext(context: ReadableMap): Boolean {
    return flagshipRnSdkModuleImpl.setContext(context)
  }

  override fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
    return flagshipRnSdkModuleImpl.getBooleanValue(key, defaultValue)
  }

  override fun getStringValue(key: String, defaultValue: String): String {
    return flagshipRnSdkModuleImpl.getStringValue(key, defaultValue)
  }

  override fun getIntegerValue(key: String, defaultValue: Double): Double {
    return flagshipRnSdkModuleImpl.getIntegerValue(key, defaultValue)
  }

  override fun getDoubleValue(key: String, defaultValue: Double): Double {
    return flagshipRnSdkModuleImpl.getDoubleValue(key, defaultValue)
  }

  override fun getObjectValue(key: String, defaultValue: ReadableMap?): WritableMap {
    val result = flagshipRnSdkModuleImpl.getObjectValue(key, defaultValue)
    return when (result) {
      is WritableMap -> result
      null -> com.facebook.react.bridge.WritableNativeMap()
      else -> com.facebook.react.bridge.WritableNativeMap()
    }
  }

  companion object {
    const val NAME = "FlagshipRnSdk"
  }
}

