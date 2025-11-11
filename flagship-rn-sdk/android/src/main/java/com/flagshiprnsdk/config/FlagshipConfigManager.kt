package com.flagshiprnsdk.config

import android.app.Application
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReactApplicationContext
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.facade.FlagshipProvider
import dev.openfeature.kotlin.sdk.OpenFeatureAPI

data class FlagshipRnSdkConfig(
  val baseUrl: String,
  val tenantId: String,
  val refreshInterval: Long,
)

object FlagshipConfigManager {
  private var config: FlagshipRnSdkConfig? = null

  fun getConfig(): FlagshipRnSdkConfig? = config

  fun initialize(
    reactContext: ReactApplicationContext,
    configMap: ReadableMap,
  ) {
    val baseUrl = configMap.getString("baseUrl") ?: ""
    val tenantId = configMap.getString("tenantId") ?: ""
    val refreshIntervalSeconds = if (configMap.hasKey("refreshInterval")) {
      configMap.getDouble("refreshInterval").toLong()
    } else {
      10L
    }
    val refreshIntervalMs = refreshIntervalSeconds * 1000

    config = FlagshipRnSdkConfig(
      baseUrl = baseUrl,
      tenantId = tenantId,
      refreshInterval = refreshIntervalMs,
    )

    val application = reactContext.applicationContext as? Application
      ?: throw IllegalStateException("Unable to get Application context")

    val flagShipConfig = FlagShipConfig(
      applicationContext = application,
      baseUrl = baseUrl,
      tenantId = tenantId,
      refreshInterval = refreshIntervalMs,
    )

    val provider = FlagshipProvider("ABcd", flagShipConfig)
    OpenFeatureAPI.setProvider(provider)

    println("FlagshipRnSdk: initialize called with baseUrl=$baseUrl, tenantId=$tenantId, refreshInterval=${refreshIntervalSeconds}s (${refreshIntervalMs}ms)")
  }
}

