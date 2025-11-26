package com.flagshiprnsdk.core

import android.util.Log

object FlagshipState {
    private const val TAG = "FlagshipSdk"

    @Volatile
    private var initialized = false

    @Synchronized
    fun isInitialized(): Boolean = initialized

    @Synchronized
    fun markInitialized(): Boolean {
        if (initialized) {
            Log.w(TAG, "SDK already initialized, skipping re-initialization")
            return false
        }
        initialized = true
        Log.i(TAG, "SDK initialized successfully")
        return true
    }

    @Synchronized
    fun reset() {
        initialized = false
        Log.i(TAG, "SDK state reset")
    }
}

