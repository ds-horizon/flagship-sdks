package com.flagship.sdk.facade.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
    val Main: CoroutineDispatcher? get() = null
}

object DefaultDispatchers : DispatcherProvider {
    override val default = Dispatchers.Default
    override val io = Dispatchers.IO
}
