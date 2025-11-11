package com.flagship.sdk.plugins.storage.sqlite.utility

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

object JsonUtility {
    inline fun <reified T> toJson(data: T): String = Json.encodeToString(serializer(), data)

    inline fun <reified T> fromJson(json: String): T = Json.decodeFromString(serializer(), json)
}
