package com.flagship.sdk.facade

import dev.openfeature.kotlin.sdk.EvaluationContext

// create sanitise function for evaluation context
fun EvaluationContext?.sanitizeEvaluationContext(): Map<String, Any?> =
    this?.asObjectMap()
        ?: emptyMap()
