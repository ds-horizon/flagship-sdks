package com.flagship.sdk.plugins.transport.http.interceptors

import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.plugins.storage.sharedPref.SharedPreferencesKeys
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class ETagResponseInterceptor(
    private val persistentCache: ICache,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalResponse = chain.proceed(request)

        val requestUrl = request.url.toString()
        val isTimeOnlyRequest = requestUrl.contains("type=time-only")

        if (isTimeOnlyRequest) {
            return originalResponse
        }

        val updatedAtHeader = originalResponse.header("updated-at")
        val lastSyncTimestamp =
            persistentCache.get<Long>(SharedPreferencesKeys.FeatureFlags.LAST_SYNC_TIMESTAMP_IN_MILLIS)

        if (lastSyncTimestamp == null) {
            return originalResponse
        }

        val updatedAt =
            (updatedAtHeader?.toLong() ?: Long.MAX_VALUE).let {
                TimeUnit.SECONDS.toMillis(it)
            }

        if (updatedAt <= lastSyncTimestamp) {
            return originalResponse
                .newBuilder()
                .code(302)
                .headers(originalResponse.headers)
                .body(originalResponse.body)
                .build()
        }

        return originalResponse
    }
}
