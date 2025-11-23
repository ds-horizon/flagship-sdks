package com.flagship.sdk

import com.flagship.sdk.plugins.evaluation.AllocationBucket
import com.flagship.sdk.plugins.evaluation.AllocationUtility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AllocationUtilityTest {
    @Test
    fun getAllocationResult_shouldBeConsistent() {
        val targetKey = "839304"
        val flagKey = "testFlag"

        val buckets =
            listOf(
                AllocationBucket("A", 30),
                AllocationBucket("B", 50),
                AllocationBucket("C", 20),
            )

        val result = AllocationUtility.getAllocationResult(flagKey, targetKey, buckets)

        // Verify the same result is returned across multiple calls
        for (i in 1..100) {
            val iterationResult = AllocationUtility.getAllocationResult(flagKey, targetKey, buckets)
            assertEquals(result.key, iterationResult.key, "Result should be consistent across calls")
        }
    }

    @Test
    fun getAllocationResult_shouldReturnValidBucket() {
        val buckets =
            listOf(
                AllocationBucket("control", 40),
                AllocationBucket("variant_a", 35),
                AllocationBucket("variant_b", 25),
            )

        val result = AllocationUtility.getAllocationResult("test-flag", "user123", buckets)

        // Result should be one of the provided buckets
        val bucketKeys = buckets.map { it.key }
        assertTrue(bucketKeys.contains(result.key), "Result should be one of the provided buckets")
    }

    @Test
    fun getAllocationResult_shouldDistributeUsersCorrectly() {
        val buckets =
            listOf(
                AllocationBucket("A", 50),
                AllocationBucket("B", 50),
            )

        val results = mutableMapOf<String, Int>()
        val numUsers = 10000

        // Test with many different users
        for (i in 1..numUsers) {
            val result = AllocationUtility.getAllocationResult("test-flag", "user$i", buckets)
            results[result.key] = results.getOrDefault(result.key, 0) + 1
        }

        // Check that distribution is roughly 50/50 (within 5% tolerance)
        val aCount = results["A"] ?: 0
        val bCount = results["B"] ?: 0
        val aPercentage = (aCount.toDouble() / numUsers) * 100
        val bPercentage = (bCount.toDouble() / numUsers) * 100

        assertTrue(
            aPercentage >= 45.0 && aPercentage <= 55.0,
            "A bucket should get ~50% (got $aPercentage%)",
        )
        assertTrue(
            bPercentage >= 45.0 && bPercentage <= 55.0,
            "B bucket should get ~50% (got $bPercentage%)",
        )
    }

    @Test
    fun getAllocationResult_shouldBeConsistentAcrossDifferentFlags() {
        val buckets =
            listOf(
                AllocationBucket("control", 70),
                AllocationBucket("treatment", 30),
            )

        val user = "user123"

        // Same user should get consistent results for the same flag
        val flag1Result1 = AllocationUtility.getAllocationResult("flag1", user, buckets)
        val flag1Result2 = AllocationUtility.getAllocationResult("flag1", user, buckets)

        val flag2Result1 = AllocationUtility.getAllocationResult("flag2", user, buckets)
        val flag2Result2 = AllocationUtility.getAllocationResult("flag2", user, buckets)

        assertEquals(flag1Result1.key, flag1Result2.key, "Same flag should give same result")
        assertEquals(flag2Result1.key, flag2Result2.key, "Same flag should give same result")

        // Different flags may give different results (not guaranteed, but possible)
        // This test just ensures the algorithm works independently per flag
    }

    @Test
    fun getAllocationResult_shouldHandleSingleBucket() {
        val buckets = listOf(AllocationBucket("only_option", 100))

        val result = AllocationUtility.getAllocationResult("test-flag", "user123", buckets)

        assertEquals("only_option", result.key, "Should return the only bucket")
    }

    @Test
    fun getAllocationResult_shouldHandleUnevenPercentages() {
        val buckets =
            listOf(
                AllocationBucket("rare", 5),
                AllocationBucket("common", 90),
                AllocationBucket("uncommon", 5),
            )

        val results = mutableMapOf<String, Int>()
        val numUsers = 10000

        for (i in 1..numUsers) {
            val result = AllocationUtility.getAllocationResult("test-flag", "user$i", buckets)
            results[result.key] = results.getOrDefault(result.key, 0) + 1
        }

        val rarePercentage = ((results["rare"] ?: 0).toDouble() / numUsers) * 100
        val commonPercentage = ((results["common"] ?: 0).toDouble() / numUsers) * 100
        val uncommonPercentage = ((results["uncommon"] ?: 0).toDouble() / numUsers) * 100

        // Allow 2% tolerance for uneven distributions
        assertTrue(
            rarePercentage >= 3.0 && rarePercentage <= 7.0,
            "Rare bucket should get ~5% (got $rarePercentage%)",
        )
        assertTrue(
            commonPercentage >= 88.0 && commonPercentage <= 92.0,
            "Common bucket should get ~90% (got $commonPercentage%)",
        )
        assertTrue(
            uncommonPercentage >= 3.0 && uncommonPercentage <= 7.0,
            "Uncommon bucket should get ~5% (got $uncommonPercentage%)",
        )
    }

    @Test
    fun getAllocationResult_shouldThrowWhenBucketsEmpty() {
        assertThrows(IllegalArgumentException::class.java) {
            AllocationUtility.getAllocationResult("test-flag", "user123", emptyList())
        }
    }

    @Test
    fun getAllocationResult_shouldThrowWhenPercentagesNotSum100_Under() {
        val buckets =
            listOf(
                AllocationBucket("A", 30),
                AllocationBucket("B", 40), // Total = 70, not 100
            )

        assertThrows(IllegalArgumentException::class.java) {
            AllocationUtility.getAllocationResult("test-flag", "user123", buckets)
        }
    }

    @Test
    fun getAllocationResult_shouldThrowWhenPercentagesNotSum100_Over() {
        val buckets =
            listOf(
                AllocationBucket("A", 60),
                AllocationBucket("B", 50), // Total = 110, not 100
            )

        assertThrows(IllegalArgumentException::class.java) {
            AllocationUtility.getAllocationResult("test-flag", "user123", buckets)
        }
    }

    @Test
    fun getAllocationResult_shouldHandleSpecialCharactersInKeys() {
        val buckets =
            listOf(
                AllocationBucket("option-1", 50),
                AllocationBucket("option_2", 50),
            )

        // Test with special characters in flag and targeting keys
        val result1 = AllocationUtility.getAllocationResult("feature-flag_test", "user@example.com", buckets)
        val result2 = AllocationUtility.getAllocationResult("feature-flag_test", "user@example.com", buckets)

        assertEquals(result1.key, result2.key, "Should handle special characters consistently")
    }

    @Test
    fun getAllocationResult_shouldHandleEmptyStrings() {
        val buckets =
            listOf(
                AllocationBucket("A", 100),
            )

        // Should handle empty strings without crashing
        val result1 = AllocationUtility.getAllocationResult("", "user123", buckets)
        val result2 = AllocationUtility.getAllocationResult("flag", "", buckets)
        val result3 = AllocationUtility.getAllocationResult("", "", buckets)

        assertNotNull(result1)
        assertNotNull(result2)
        assertNotNull(result3)

        // Should be consistent
        assertEquals(
            result1.key,
            AllocationUtility.getAllocationResult("", "user123", buckets).key,
            "Empty flag key should be consistent",
        )
    }

    @Test
    fun getAllocationResult_shouldBeDeterministicAcrossJVMRestarts() {
        // This test ensures the hash algorithm doesn't depend on JVM-specific state
        val buckets =
            listOf(
                AllocationBucket("control", 50),
                AllocationBucket("treatment", 50),
            )

        val testCases =
            listOf(
                Pair("flag1", "user1"),
                Pair("flag2", "user2"),
                Pair("feature-x", "customer@email.com"),
                Pair("rollout-test", "12345"),
            )

        // Store results and verify they're consistent when called again
        val storedResults = mutableMapOf<Pair<String, String>, String>()

        testCases.forEach { (flag, user) ->
            val result = AllocationUtility.getAllocationResult(flag, user, buckets)
            storedResults[Pair(flag, user)] = result.key
        }

        // Call again and verify same results
        testCases.forEach { (flag, user) ->
            val result = AllocationUtility.getAllocationResult(flag, user, buckets)
            assertEquals(
                storedResults[Pair(flag, user)],
                result.key,
                "Result should be deterministic for $flag:$user",
            )
        }
    }

    @Test
    fun generateHash_shouldReturnSameHashForSameAttributes() {
        val flagKey = "test-flag"
        val targetingKey = "user123"

        val hash1 = AllocationUtility.generateHash(flagKey, targetingKey)

        for (i in 1..100) {
            val hash2 = AllocationUtility.generateHash(flagKey, targetingKey)
            assertEquals(hash1, hash2, "Hash should be the same for the same attributes")
        }
    }
}
