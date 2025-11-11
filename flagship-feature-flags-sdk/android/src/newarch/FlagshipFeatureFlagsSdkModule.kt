package com.flagshipfeatureflagssdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = FlagshipFeatureFlagsSdkModuleImpl.NAME)
class FlagshipFeatureFlagsSdkModule(
  reactContext: ReactApplicationContext,
) : NativeFlagshipFeatureFlagsSdkSpec(reactContext) {
  private val flagshipFeatureFlagsSdkModuleImpl: FlagshipFeatureFlagsSdkModuleImpl = FlagshipFeatureFlagsSdkModuleImpl()

  override fun getName(): String = NAME

  override fun multiply(
    a: Double,
    b: Double,
  ): Double = flagshipFeatureFlagsSdkModuleImpl.multiply(a, b)

  override fun getBooleanValue(
    key: String?,
    defaultValue: Boolean,
    promise: Promise?,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getBooleanValue(key, defaultValue, promise)
  }

  override fun getStringValue(
    key: String?,
    defaultValue: String?,
    promise: Promise?,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getStringValue(key, defaultValue, promise)
  }

  override fun getIntegerValue(
    key: String?,
    defaultValue: Double,
    promise: Promise?,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getIntegerValue(key, defaultValue, promise)
  }

  override fun getDoubleValue(
    key: String?,
    defaultValue: Double,
    promise: Promise?,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getDoubleValue(key, defaultValue, promise)
  }

  override fun getObjectValue(
    key: String?,
    defaultValue: ReadableMap?,
    promise: Promise?,
  ) {
    flagshipFeatureFlagsSdkModuleImpl.getObjectValue(key, defaultValue, promise)
  }
}
