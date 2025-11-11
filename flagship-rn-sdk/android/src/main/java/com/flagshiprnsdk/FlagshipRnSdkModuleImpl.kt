package com.flagshiprnsdk

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.flagshiprnsdk.config.FlagshipConfigManager
import com.flagshiprnsdk.context.FlagshipContextManager
import com.flagshiprnsdk.evaluation.FlagshipEvaluationManager

class FlagshipRnSdkModuleImpl(
  private val reactContext: ReactApplicationContext,
) {
  fun multiply(
    a: Double,
    b: Double,
  ): Double = a * b

  fun initialize(
    config: ReadableMap,
    promise: Promise?,
  ) {
    try {
      FlagshipConfigManager.initialize(reactContext, config)
      promise?.resolve(true)
    } catch (e: Exception) {
      println("FlagshipRnSdk: initialization failed: ${e.message}")
      promise?.reject("INIT_ERROR", e.message ?: "Initialization failed", e)
    }
  }

  fun setContext(
    context: ReadableMap,
    promise: Promise?,
  ) {
    try {
      FlagshipContextManager.setContext(context)
      promise?.resolve(true)
    } catch (e: Exception) {
      println("FlagshipRnSdk: setContext failed: ${e.message}")
      promise?.reject("SET_CONTEXT_ERROR", e.message ?: "setContext failed", e)
    }
  }

  fun getBooleanValue(
    key: String?,
    defaultValue: Boolean,
    promise: Promise?,
  ) {
    FlagshipEvaluationManager.getBooleanValue(key, defaultValue, promise)
  }

  fun getStringValue(
    key: String?,
    defaultValue: String?,
    promise: Promise?,
  ) {
    FlagshipEvaluationManager.getStringValue(key, defaultValue, promise)
  }

  fun getIntegerValue(
    key: String?,
    defaultValue: Double,
    promise: Promise?,
  ) {
    FlagshipEvaluationManager.getIntegerValue(key, defaultValue, promise)
  }

  fun getDoubleValue(
    key: String?,
    defaultValue: Double,
    promise: Promise?,
  ) {
    FlagshipEvaluationManager.getDoubleValue(key, defaultValue, promise)
  }

  fun getObjectValue(
    key: String?,
    defaultValue: ReadableMap?,
    promise: Promise?,
  ) {
    FlagshipEvaluationManager.getObjectValue(key, defaultValue, promise)
  }

  companion object {
    const val NAME = "FlagshipRnSdk"
  }
}

