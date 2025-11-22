package com.flagship.sdk.facade

import android.app.Application

data class FlagShipConfig(
    val applicationContext: Application,
    val baseUrl: String,
    val flagshipApiKey: String,
    val refreshInterval: Long = 30000L,
)
