package com.flagship.sdk.core.contracts

import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.core.models.Result

interface ITransport {
    suspend fun fetchConfig(type: String): Result<FeatureFlagsSchema>
}
