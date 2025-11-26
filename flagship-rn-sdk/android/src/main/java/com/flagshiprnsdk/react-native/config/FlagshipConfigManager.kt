package com.flagshiprnsdk.config

import android.app.Application
import android.util.Log
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReactApplicationContext
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.facade.FlagshipProvider
import com.flagshiprnsdk.core.FlagshipState
import dev.openfeature.kotlin.sdk.OpenFeatureAPI

data class FlagshipRnSdkConfig(
    val baseUrl: String,
    val flagshipApiKey: String,
    val refreshInterval: Long,
)

object FlagshipConfigManager {
    private const val TAG = "FlagshipSdk"
    private var config: FlagshipRnSdkConfig? = null

    fun getConfig(): FlagshipRnSdkConfig? = config

    fun initialize(
        reactContext: ReactApplicationContext,
        configMap: ReadableMap,
    ) {
        if (FlagshipState.isInitialized()) {
            Log.i(TAG, "SDK already initialized from native or previous call")
            return
        }

        val baseUrl = configMap.getString("baseUrl") ?: ""
        val flagshipApiKey = configMap.getString("flagshipApiKey") ?: ""
        val refreshIntervalSeconds = if (configMap.hasKey("refreshInterval")) {
            configMap.getDouble("refreshInterval").toLong()
        } else {
            10L
        }
        val refreshIntervalMs = refreshIntervalSeconds * 1000

        config = FlagshipRnSdkConfig(
            baseUrl = baseUrl,
            flagshipApiKey = flagshipApiKey,
            refreshInterval = refreshIntervalMs,
        )

        val application = reactContext.applicationContext as? Application
            ?: throw IllegalStateException("Unable to get Application context")

        val flagShipConfig = FlagShipConfig(
            applicationContext = application,
            baseUrl = baseUrl,
            flagshipApiKey = flagshipApiKey,
            refreshInterval = refreshIntervalMs,
        )

        val provider = FlagshipProvider("react-native", flagShipConfig)
        OpenFeatureAPI.setProvider(provider)

        FlagshipState.markInitialized()
        Log.i(TAG, "Initialized from React Native with baseUrl=$baseUrl, refreshInterval=${refreshIntervalSeconds}s")
    }
}

