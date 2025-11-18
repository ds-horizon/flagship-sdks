package com.flagshiprnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = FlagshipRnSdkModuleImpl.NAME)
class FlagshipRnSdkModule(
  val reactContext: ReactApplicationContext,
) : ReactContextBaseJavaModule(reactContext) {
  private val flagshipRnSdkModuleImpl: FlagshipRnSdkModuleImpl = FlagshipRnSdkModuleImpl(reactContext)

  override fun getName(): String = FlagshipRnSdkModuleImpl.NAME

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun multiply(a: Double, b: Double): Double = flagshipRnSdkModuleImpl.multiply(a, b)

  @ReactMethod
  fun initialize(config: ReadableMap, promise: Promise) {
    flagshipRnSdkModuleImpl.initialize(config, promise)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setContext(context: ReadableMap): Boolean {
    return flagshipRnSdkModuleImpl.setContext(context)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
    return flagshipRnSdkModuleImpl.getBooleanValue(key, defaultValue)
  }

  @ReactMethod
  fun getStringValue(key: String, defaultValue: String, promise: Promise) {
    flagshipRnSdkModuleImpl.getStringValue(key, defaultValue, promise)
  }

  @ReactMethod
  fun getIntegerValue(key: String, defaultValue: Double, promise: Promise) {
    flagshipRnSdkModuleImpl.getIntegerValue(key, defaultValue, promise)
  }

  @ReactMethod
  fun getDoubleValue(key: String, defaultValue: Double, promise: Promise) {
    flagshipRnSdkModuleImpl.getDoubleValue(key, defaultValue, promise)
  }

  @ReactMethod
  fun getObjectValue(key: String, defaultValue: ReadableMap, promise: Promise) {
    flagshipRnSdkModuleImpl.getObjectValue(key, defaultValue, promise)
  }

  companion object {
    const val NAME = "FlagshipRnSdk"
  }
}

