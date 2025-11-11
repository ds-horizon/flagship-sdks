package com.flagship.sdk.core.contracts

import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.core.models.Result
import kotlinx.coroutines.flow.Flow

interface ITransport {
    fun fetchConfig(type: String): Flow<Result<FeatureFlagsSchema>>
}
