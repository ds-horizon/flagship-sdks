package com.flagship.sdk.facade

import android.app.Application
import okhttp3.Interceptor

data class FlagShipConfig(
    val applicationContext: Application,
    val baseUrl: String,
    val flagshipApiKey: String,
    val refreshInterval: Long = 30000L,
    val mockInterceptors: List<Interceptor> = emptyList(), // For testing purposes
)
