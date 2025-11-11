package com.flagship.sdk.plugins.storage.sharedPref

/**
 * Generic interface for SharedPreferences operations
 * Provides type-safe methods for storing and retrieving key-value pairs
 */
interface ISharedPreferencesManager {
    /**
     * Store a string value
     * @param key The key to store the value under
     * @param value The string value to store
     */
    fun putString(
        key: String,
        value: String,
    )

    /**
     * Retrieve a string value
     * @param key The key to retrieve the value for
     * @param defaultValue The default value to return if key doesn't exist
     * @return The stored string value or defaultValue
     */
    fun getString(
        key: String,
        defaultValue: String = "",
    ): String

    /**
     * Store an integer value
     * @param key The key to store the value under
     * @param value The integer value to store
     */
    fun putInt(
        key: String,
        value: Int,
    )

    /**
     * Retrieve an integer value
     * @param key The key to retrieve the value for
     * @param defaultValue The default value to return if key doesn't exist
     * @return The stored integer value or defaultValue
     */
    fun getInt(
        key: String,
        defaultValue: Int = 0,
    ): Int

    /**
     * Store a boolean value
     * @param key The key to store the value under
     * @param value The boolean value to store
     */
    fun putBoolean(
        key: String,
        value: Boolean,
    )

    /**
     * Retrieve a boolean value
     * @param key The key to retrieve the value for
     * @param defaultValue The default value to return if key doesn't exist
     * @return The stored boolean value or defaultValue
     */
    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean

    /**
     * Store a long value
     * @param key The key to store the value under
     * @param value The long value to store
     */
    fun putLong(
        key: String,
        value: Long,
    )

    /**
     * Retrieve a long value
     * @param key The key to retrieve the value for
     * @param defaultValue The default value to return if key doesn't exist
     * @return The stored long value or defaultValue
     */
    fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long

    /**
     * Store a float value
     * @param key The key to store the value under
     * @param value The float value to store
     */
    fun putFloat(
        key: String,
        value: Float,
    )

    /**
     * Retrieve a float value
     * @param key The key to retrieve the value for
     * @param defaultValue The default value to return if key doesn't exist
     * @return The stored float value or defaultValue
     */
    fun getFloat(
        key: String,
        defaultValue: Float = 0f,
    ): Float

    /**
     * Store a set of strings
     * @param key The key to store the value under
     * @param value The set of strings to store
     */
    fun putStringSet(
        key: String,
        value: Set<String>,
    )

    /**
     * Retrieve a set of strings
     * @param key The key to retrieve the value for
     * @param defaultValue The default value to return if key doesn't exist
     * @return The stored set of strings or defaultValue
     */
    fun getStringSet(
        key: String,
        defaultValue: Set<String> = emptySet(),
    ): Set<String>

    /**
     * Check if a key exists in preferences
     * @param key The key to check
     * @return true if key exists, false otherwise
     */
    fun contains(key: String): Boolean

    /**
     * Remove a specific key from preferences
     * @param key The key to remove
     */
    fun remove(key: String)

    /**
     * Clear all preferences
     */
    fun clear()

    /**
     * Get all keys stored in preferences
     * @return Set of all keys
     */
    fun getAllKeys(): Set<String>
}
