package com.flagship.sdk.facade

import dev.openfeature.kotlin.sdk.EvaluationContext

fun EvaluationContext?.sanitizeEvaluationContext(): Map<String, Any?> =
    this?.asObjectMap()
        ?: emptyMap()
