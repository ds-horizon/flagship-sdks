package com.flagship.sdk.utils

import com.flagship.sdk.plugins.evaluation.AllocationUtility

object CommonUtils {
    /**
     * Finds a targeting key that produces a percentile within the specified range
     * @param flagKey The feature flag key
     * @param minPercentile Minimum percentile (0-99)
     * @param maxPercentile Maximum percentile (0-99)
     * @param maxAttempts Maximum number of attempts to find a matching key
     * @return A targeting key that produces a percentile in the specified range, or null if not found
     */
    @JvmStatic
    fun findTargetingKeyWithPercentile(
        flagKey: String,
        minPercentile: Int,
        maxPercentile: Int,
        maxAttempts: Int = 10000,
    ): String? {
        for (i in 0 until maxAttempts) {
            val targetingKey = "user-$i"
            val hash = AllocationUtility.generateHash(flagKey, targetingKey)
            val percentile = (hash % 100).toInt()
            if (percentile in minPercentile..maxPercentile) {
                return targetingKey
            }
        }
        return null
    }

    /**
     * Finds two targeting keys that produce different variants within the rollout range
     * For a 50% rollout with 50-50 variant split:
     * - First key should get variant-a (allocation percentile 0-49)
     * - Second key should get variant-b (allocation percentile 50-99)
     * Both keys must be in rollout (rollout percentile < rolloutPercentage)
     * Note: Rollout check uses flagKey:targetingKey, but allocation uses flagKey:ruleName:targetingKey
     * @param flagKey The feature flag key
     * @param ruleName The rule name (affects allocation hash)
     * @param rolloutPercentage The rollout percentage
     * @param maxAttempts Maximum number of attempts
     * @return Pair of targeting keys, or null if not found
     */
    @JvmStatic
    fun findTwoTargetingKeysForVariants(
        flagKey: String,
        ruleName: String,
        rolloutPercentage: Long,
        maxAttempts: Int = 10000,
    ): Pair<String, String>? {
        var key1: String? = null
        var key2: String? = null

        for (i in 0 until maxAttempts) {
            val targetingKey = "user-$i"

            // Check rollout (uses flagKey:targetingKey, no ruleName)
            val rolloutHash = AllocationUtility.generateHash(flagKey, targetingKey)
            val rolloutPercentile = (rolloutHash % 100).toInt()

            // Only proceed if user is in rollout
            if (rolloutPercentile < rolloutPercentage) {
                // Check allocation (uses flagKey:ruleName:targetingKey)
                val allocationHash = AllocationUtility.generateHash(flagKey, targetingKey, ruleName)
                val allocationPercentile = (allocationHash % 100).toInt()

                // For 50-50 split: 0-49 gets variant-a, 50-99 gets variant-b
                if (key1 == null && allocationPercentile < 50) {
                    key1 = targetingKey
                } else if (key2 == null && allocationPercentile >= 50) {
                    key2 = targetingKey
                }

                if (key1 != null && key2 != null) {
                    return Pair(key1, key2)
                }
            }
        }
        return null
    }
}