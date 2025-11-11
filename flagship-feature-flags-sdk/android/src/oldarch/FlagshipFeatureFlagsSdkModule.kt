package com.flagshipfeatureflagssdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = FlagshipFeatureFlagsSdkModuleImpl.NAME)
class FlagshipFeatureFlagsSdkModule(
  val reactContext: ReactApplicationContext,
) : ReactContextBaseJavaModule(reactContext) {
  private val flagshipFeatureFlagsSdkModuleImpl: FlagshipFeatureFlagsSdkModuleImpl = FlagshipFeatureFlagsSdkModuleImpl()

  override fun getName(): String = FlagshipFeatureFlagsSdkModuleImpl.NAME

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun multiply(
    a: Double,
    b: Double,
  ): Double = flagshipFeatureFlagsSdkModuleImpl.multiply(a, b)

  @ReactMethod
  fun getBooleanValue(
    key: String?,
    defaultValue: Boolean,
    promise: Promise,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getBooleanValue(key, defaultValue, promise)
  }

  @ReactMethod
  fun getStringValue(
    key: String?,
    defaultValue: String?,
    promise: Promise,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getStringValue(key, defaultValue, promise)
  }

  @ReactMethod
  fun getIntegerValue(
    key: String?,
    defaultValue: Double,
    promise: Promise,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getIntegerValue(key, defaultValue, promise)
  }

  @ReactMethod
  fun getDoubleValue(
    key: String?,
    defaultValue: Double,
    promise: Promise,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getDoubleValue(key, defaultValue, promise)
  }

  @ReactMethod
  fun getObjectValue(
    key: String?,
    defaultValue: ReadableMap?,
    promise: Promise,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getObjectValue(key, defaultValue, promise)
  }
}
