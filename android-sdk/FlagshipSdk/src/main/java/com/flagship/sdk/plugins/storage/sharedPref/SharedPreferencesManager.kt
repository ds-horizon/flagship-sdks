package com.flagship.sdk.plugins.storage.sharedPref

import android.content.Context
import android.content.SharedPreferences

/**
 * Generic SharedPreferences implementation for storing key-value pairs
 *
 * @param context Android context for accessing SharedPreferences
 * @param preferencesName Name of the SharedPreferences file (optional, defaults to "flagship_prefs")
 */
class SharedPreferencesManager(
    private val context: Context,
    private val preferencesName: String = "flagship_prefs",
) : ISharedPreferencesManager {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    override fun putString(
        key: String,
        value: String,
    ) {
        editor.putString(key, value).apply()
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = sharedPreferences.getString(key, defaultValue) ?: defaultValue

    override fun putInt(
        key: String,
        value: Int,
    ) {
        editor.putInt(key, value).apply()
    }

    override fun getInt(
        key: String,
        defaultValue: Int,
    ): Int = sharedPreferences.getInt(key, defaultValue)

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        editor.putBoolean(key, value).apply()
    }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = sharedPreferences.getBoolean(key, defaultValue)

    override fun putLong(
        key: String,
        value: Long,
    ) {
        editor.putLong(key, value).apply()
    }

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = sharedPreferences.getLong(key, defaultValue)

    override fun putFloat(
        key: String,
        value: Float,
    ) {
        editor.putFloat(key, value).apply()
    }

    override fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float = sharedPreferences.getFloat(key, defaultValue)

    override fun putStringSet(
        key: String,
        value: Set<String>,
    ) {
        editor.putStringSet(key, value).apply()
    }

    override fun getStringSet(
        key: String,
        defaultValue: Set<String>,
    ): Set<String> = sharedPreferences.getStringSet(key, defaultValue) ?: defaultValue

    override fun contains(key: String): Boolean = sharedPreferences.contains(key)

    override fun remove(key: String) {
        editor.remove(key).apply()
    }

    override fun clear() {
        editor.clear().apply()
    }

    override fun getAllKeys(): Set<String> = sharedPreferences.all.keys
}
