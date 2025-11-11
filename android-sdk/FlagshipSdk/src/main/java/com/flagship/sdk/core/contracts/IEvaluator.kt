package com.flagship.sdk.core.contracts

import com.flagship.sdk.core.models.EvaluationContext
import com.flagship.sdk.core.models.EvaluationResult
import com.flagship.sdk.core.models.Feature

interface IEvaluator {
    fun evaluateBoolean(
        flagKey: String,
        defaultValue: Boolean,
        config: Feature?,
        context: EvaluationContext = EvaluationContext.empty(),
    ): EvaluationResult<Boolean>

    fun evaluateString(
        flagKey: String,
        defaultValue: String,
        config: Feature?,
        context: EvaluationContext = EvaluationContext.empty(),
    ): EvaluationResult<String>

    fun evaluateInt(
        flagKey: String,
        defaultValue: Int,
        config: Feature?,
        context: EvaluationContext = EvaluationContext.empty(),
    ): EvaluationResult<Int>

    fun evaluateDouble(
        flagKey: String,
        defaultValue: Double,
        config: Feature?,
        context: EvaluationContext = EvaluationContext.empty(),
    ): EvaluationResult<Double>

    fun <T> evaluateObject(
        flagKey: String,
        defaultValue: T,
        config: Feature?,
        context: EvaluationContext = EvaluationContext.empty(),
    ): EvaluationResult<T>
}
