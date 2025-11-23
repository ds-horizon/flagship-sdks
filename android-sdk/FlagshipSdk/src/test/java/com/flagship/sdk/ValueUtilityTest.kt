package com.flagship.sdk

import com.flagship.sdk.facade.jsonToValue
import dev.openfeature.kotlin.sdk.Value
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValueUtilityTest {
    @Test
    fun `should return object when value is object`() {
        // create deep nested json
        val jsonObjectStr =
            """
            {
              "user": {
                "id": 42,
                "name": "Farhan",
                "active": true,
                "profile": {
                  "age": 31,
                  "city": "Mumbai",
                  "skills": ["Kotlin", "React Native", "TypeScript"]
                }
              },
              "subscription": {
                "plan": "Pro",
                "renewal": {
                  "autoRenew": true,
                  "nextBillingDate": "2025-11-07"
                }
              },
              "devices": [
                {
                  "type": "android",
                  "model": "Pixel 8 Pro"
                },
                {
                  "type": "ios",
                  "model": "iPhone 15"
                }
              ]
            }
            """.trimIndent()

        val simpleMock =
            Value.Structure(
                mapOf(
                    "user" to
                        Value.Structure(
                            mapOf(
                                "id" to Value.Integer(42),
                                "name" to Value.String("Farhan"),
                                "active" to Value.Boolean(true),
                                "profile" to
                                    Value.Structure(
                                        mapOf(
                                            "age" to Value.Integer(31),
                                            "city" to Value.String("Mumbai"),
                                            "skills" to
                                                Value.List(
                                                    listOf(
                                                        Value.String("Kotlin"),
                                                        Value.String("React Native"),
                                                        Value.String("TypeScript"),
                                                    ),
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                    "subscription" to
                        Value.Structure(
                            mapOf(
                                "plan" to Value.String("Pro"),
                                "renewal" to
                                    Value.Structure(
                                        mapOf(
                                            "autoRenew" to Value.Boolean(true),
                                            "nextBillingDate" to Value.String("2025-11-07"),
                                        ),
                                    ),
                            ),
                        ),
                    "devices" to
                        Value.List(
                            listOf(
                                Value.Structure(
                                    mapOf(
                                        "type" to Value.String("android"),
                                        "model" to Value.String("Pixel 8 Pro"),
                                    ),
                                ),
                                Value.Structure(
                                    mapOf(
                                        "type" to Value.String("ios"),
                                        "model" to Value.String("iPhone 15"),
                                    ),
                                ),
                            ),
                        ),
                ),
            )
        val expectedValue: Value = simpleMock
        val result = jsonToValue(jsonObjectStr)
        assertEquals(expectedValue, result)
    }

    @Test
    fun `should return array when value is array`() {
        val jsonArrayStr =
            """
            [
              {
                "id": 1,
                "name": "John Doe"
              }
            ]
            """.trimIndent()

        val simpleMock =
            Value.List(
                listOf(
                    Value.Structure(
                        mapOf(
                            "id" to Value.Integer(1),
                            "name" to Value.String("John Doe"),
                        ),
                    ),
                ),
            )

        val expectedValue: Value = simpleMock
        val result = jsonToValue(jsonArrayStr)
        assertEquals(expectedValue, result)
    }
}
