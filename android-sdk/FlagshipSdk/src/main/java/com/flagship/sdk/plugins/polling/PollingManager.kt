package com.flagship.sdk.plugins.polling

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PollingManager(
    private val interval: Long,
    private val pollingBlock: suspend () -> Unit,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null
    private var isPolling = false

    fun start() {
        if (isPolling) return

        isPolling = true

        job = scope.launch(Dispatchers.IO) {
            pollingBlock()

            while (scope.isActive && isPolling) {
                delay(interval)
                if (isPolling) {
                    pollingBlock()
                }
            }
        }
    }

    fun stop() {
        isPolling = false
        job?.cancel()
        job = null
    }
}

