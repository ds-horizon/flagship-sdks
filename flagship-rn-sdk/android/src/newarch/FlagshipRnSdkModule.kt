package com.flagshiprnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
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

  override fun setContext(context: ReadableMap, promise: Promise?) {
    flagshipRnSdkModuleImpl.setContext(context, promise)
  }

  override fun getBooleanValue(key: String, defaultValue: Boolean, promise: Promise?) {
    flagshipRnSdkModuleImpl.getBooleanValue(key, defaultValue, promise)
  }

  override fun getStringValue(key: String, defaultValue: String, promise: Promise?) {
    flagshipRnSdkModuleImpl.getStringValue(key, defaultValue, promise)
  }

  override fun getIntegerValue(key: String, defaultValue: Double, promise: Promise?) {
    flagshipRnSdkModuleImpl.getIntegerValue(key, defaultValue, promise)
  }

  override fun getDoubleValue(key: String, defaultValue: Double, promise: Promise?) {
    flagshipRnSdkModuleImpl.getDoubleValue(key, defaultValue, promise)
  }

  override fun getObjectValue(key: String, defaultValue: ReadableMap?, promise: Promise?) {
    flagshipRnSdkModuleImpl.getObjectValue(key, defaultValue, promise)
  }

  companion object {
    const val NAME = "FlagshipRnSdk"
  }
}

