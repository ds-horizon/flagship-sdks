package com.flagship.sdk.core.models

data class EvaluationContext(
    val targetingKey: String,
    val attributes: Map<String, Any?>,
) {
    companion object {
        fun empty() = EvaluationContext("", emptyMap())
    }
}

data class EvaluationResult<T>(
    val value: T,
    val variant: String? = null, // which variant was picked (A/B/n testing)
    val reason: Reason, // why this value was chosen
    val metadata: Map<String, Any> = emptyMap(), // rollout %, rules applied, etc.
)

enum class Reason {
    // The resolved value is static (no dynamic evaluation).
    STATIC,

    // The resolved value fell back to a pre-configured value (no dynamic evaluation occurred or dynamic evaluation yielded no result).
    DEFAULT,

    // The resolved value was the result of a dynamic evaluation, such as a rule or specific user-targeting.
    TARGETING_MATCH,

    // The resolved value was the result of a dynamic evaluation in the default rule, such as a rule or specific user-targeting.
    DEFAULT_TARGETING_MATCH,

    // The resolved value was the result of pseudorandom assignment.
    SPLIT,

    // The resolved value was retrieved from cache.
    CACHED,

    // / The resolved value was the result of the flag being disabled in the management system.
    DISABLED,

    // / The reason for the resolved value could not be determined.
    UNKNOWN,

    // / The resolved value is non-authoritative or possible out of date
    STALE,

    // / The resolved value was the result of an error.
    ERROR,

    /// The return value is there because there is no such feature
    INVALID_FEATURE
}
