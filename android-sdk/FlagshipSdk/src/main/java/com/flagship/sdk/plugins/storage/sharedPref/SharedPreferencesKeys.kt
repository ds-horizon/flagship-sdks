package com.flagship.sdk.plugins.storage.sharedPref

/**
 * Centralized constants for SharedPreferences keys to avoid typos and ensure consistency
 */
object SharedPreferencesKeys {
    // Feature Flag related keys
    object FeatureFlags {
        const val LAST_SYNC_TIMESTAMP_IN_MILLIS = "feature_flags_last_sync"
    }

    // User related keys
    object User {
        const val USER_ID = "user_id"
        const val TARGETING_KEY = "targeting_key"
        const val USER_CONTEXT = "user_context"
        const val LAST_EVALUATION_CONTEXT = "last_evaluation_context"
    }
}
