package com.flagship.sdk.plugins.storage.sharedPref

import android.content.Context

/**
 * Factory class for creating SharedPreferencesManager instances
 */
object SharedPreferencesFactory {
    /**
     * Create a SharedPreferencesManager with default preferences name
     * @param context Android context
     * @return SharedPreferencesManager instance
     */
    fun create(context: Context): ISharedPreferencesManager = SharedPreferencesManager(context)

    /**
     * Create a SharedPreferencesManager with custom preferences name
     * @param context Android context
     * @param preferencesName Custom name for the SharedPreferences file
     * @return SharedPreferencesManager instance
     */
    fun create(
        context: Context,
        preferencesName: String,
    ): ISharedPreferencesManager = SharedPreferencesManager(context, preferencesName)
}
