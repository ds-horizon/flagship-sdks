package com.flagship.sdk.facade.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.cancellation.CancellationException

object SdkScope {
    @Volatile private var _scope: CoroutineScope? = null

    val scope: CoroutineScope
        get() = _scope ?: error("SdkScope not initialized. Call SdkScope.init().")

    fun init(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        errorHandler: (Throwable) -> Unit = {},
    ) {
        if (_scope != null) return // idempotent
        val handler = CoroutineExceptionHandler { _, t -> errorHandler(t) }
        _scope = CoroutineScope(SupervisorJob() + handler + dispatcher)
    }

    /** Cancels EVERYTHING launched under this scope. */
    fun cancelAll(reason: String = "SDK shutdown") {
        _scope?.cancel(CancellationException(reason))
        _scope = null
    }
}
