package com.flagship.sdk.test

import com.flagship.sdk.core.models.ConstraintValue
import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.core.models.Operator
import com.flagship.sdk.core.models.VariantValue
import com.flagship.sdk.plugins.transport.http.MockResponseInterceptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JsonParsingTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }

    @Test
    fun testSimpleJsonParsing() {
        // Try to parse a very simple JSON first
        val simpleJson = """{
            "features": [],
            "updated_at": 0
        }"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(simpleJson)
            assert(result.features.isEmpty())
            println("✅ Simple JSON parsing successful: ${result.features.size} flags")
        } catch (e: Exception) {
            println("❌ Simple JSON parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testSampleDataParsing() {
        try {
            val result =
                json.decodeFromString<FeatureFlagsSchema>(MockResponseInterceptor.SAMPLE_FEATURE_FLAGS_JSON)
            assert(result.features.size == 2)
            val first = result.features[0]
            assert(first.key.isNotEmpty())
            assert(first.defaultRule?.allocation?.isNotEmpty() == true)
            assert(first.variants.isNotEmpty())
            val second = result.features[1]
            assert(!second.enabled)
            assert(second.rules.isEmpty())
            println("✅ Sample JSON parsing successful: ${result.features.size} flags")
        } catch (e: Exception) {
            println("❌ Sample JSON parsing failed: ${e.message}")
            e.printStackTrace()
            assert(false)

        }
    }

    @Test
    fun testMinimalFlagParsing() {
        // Test with a flag that has minimal data
        val minimalJson = """{
            "features": [
                {
                    "enabled": true,
                    "key": "test-flag",
                    "rollout_percentage": 100,
                    "type": "boolean",
                    "updated_at": 1759231203,
                    "rules": [],
                    "variants": [],
                    "default_rule": {
                        "name": "1",
                        "allocation": [
                            { "variant_key": "off", "percentage": 100 }
                        ]
                    }
                }
            ],
            "updated_at": 1759231299
        }"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(minimalJson)
            assert(result.features.size == 1)
            val flag = result.features.first()
            assert(flag.key == "test-flag")
            assert(flag.defaultRule?.allocation?.get(0)?.variantKey == "off")
            println("✅ Minimal flag parsing successful: ${result.features.first().key}")
        } catch (e: Exception) {
            println("❌ Minimal flag parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testConstraintValueParsing() {
        // Test with just a constraint value
        val constraintJson = """{
            "features": [
                {
                    "enabled": true,
                    "key": "test-flag",
                    "rollout_percentage": 100,
                    "type": "string",
                    "updated_at": 1759231203,
                    "rules": [
                        {
                            "name": 1,
                            "allocations": [],
                            "constraints": [
                                {
                                    "context_field": "userId",
                                    "operator": "eq",
                                    "value": "12345"
                                }
                            ]
                        }
                    ],
                    "variants": [],
                    "default_rule": {
                        "name": "2",
                        "allocation": [
                            { "variant_key": "default", "percentage": 100 }
                        ]
                    }
                }
            ],
            "updated_at": 1759231299
        }"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(constraintJson)
            val rule = result.features[0].rules[0]
            assert(rule.constraints.size == 1)
            assert(rule.constraints[0].operator == Operator.Eq)
            println("✅ Constraint value parsing successful")
        } catch (e: Exception) {
            println("❌ Constraint value parsing failed: ${e.message}")
            assert(false)
        }
    }

    @Test
    fun testVariantValueParsing() {
        // Test with just a variant value
        val variantJson = """{
            "features": [
                {
                    "enabled": true,
                    "key": "test-flag",
                    "rollout_percentage": 100,
                    "type": "object",
                    "updated_at": 1759231203,
                    "rules": [],
                    "variants": [
                        {
                            "key": "variant-a",
                            "value": {
                                "color": "blue",
                                "buttonText": "Continue"
                            }
                        }
                    ],
                    "default_rule": {
                        "name": "1",
                        "allocation": [
                            { "variant_key": "variant-a", "percentage": 100 }
                        ]
                    }
                }
            ],
            "updated_at": 1759231299
        }"""

        try {
            val resultObj = json.decodeFromString<FeatureFlagsSchema>(variantJson)
            assert(resultObj.features[0].variants.size == 1)
            println("✅ Variant value parsing successful")
            assert(true)
        } catch (e: Exception) {
            println("❌ Variant value parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testBasicFeatureFlagsJsonParsing() {
        val basicJson = """{
  "features": [
    {
      "key": "simple_toggle",
      "enabled": true,
      "rollout_percentage": 50,
      "type": "boolean",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "on",
          "value": true
        },
        {
          "key": "off",
          "value": false
        }
      ],
      "rules": [],
      "default_rule": {
        "name": "1",
        "allocation": [
          { "variant_key": "off", "percentage": 100 }
        ]
      }
    },
    {
      "key": "feature_message",
      "enabled": true,
      "rollout_percentage": 100,
      "type": "string",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "welcome",
          "value": "Welcome to our new feature!"
        },
        {
          "key": "default",
          "value": "Standard message"
        }
      ],
      "rules": [
        {
          "name": "1",
          "constraints": [
            {
              "context_field": "user_type",
              "operator": "eq",
              "value": "premium"
            }
          ],
          "allocations": [
            {
              "variant_key": "welcome",
              "percentage": 100
            }
          ]
        }
      ],
      "default_rule": {
        "name": "2",
        "allocation": [
          { "variant_key": "default", "percentage": 100 }
        ]
      }
    }
  ],
  "updated_at": 1759231299
}"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(basicJson)
            println("✅ Basic feature flags parsing successful: ${result.features.size} flags")

            // Validate first flag
            val simpleToggle = result.features[0]
            assert(simpleToggle.key == "simple_toggle")
            assert(simpleToggle.enabled == true)
            assert(simpleToggle.rolloutPercentage == 50L)
            assert(simpleToggle.type.name.lowercase() == "boolean")
            assert(simpleToggle.variants.size == 2)
            assert(simpleToggle.rules.isEmpty())
            assert(simpleToggle.defaultRule?.allocation?.size == 1)

            // Validate second flag
            val featureMessage = result.features[1]
            assert(featureMessage.key == "feature_message")
            assert(featureMessage.rules.size == 1)
            assert(featureMessage.rules[0].constraints.size == 1)
            assert(featureMessage.rules[0].constraints[0].operator == Operator.Eq)

            println("  - All basic validations passed")
        } catch (e: Exception) {
            println("❌ Basic feature flags parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testComprehensiveFeatureFlagsJsonParsing() {
        val comprehensiveJson = """{
  "features": [
    {
      "key": "ui_theme",
      "enabled": true,
      "rollout_percentage": 75,
      "type": "string",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "dark",
          "value": "dark_theme"
        },
        {
          "key": "light",
          "value": "light_theme"
        },
        {
          "key": "auto",
          "value": "system_theme"
        }
      ],
      "rules": [
        {
          "name": "1",
          "constraints": [
            {
              "context_field": "device_type",
              "operator": "eq",
              "value": "mobile"
            },
            {
              "context_field": "app_version",
              "operator": "gte",
              "value": "2.0.0"
            }
          ],
          "allocations": [
            {
              "variant_key": "dark",
              "percentage": 60
            },
            {
              "variant_key": "light",
              "percentage": 30
            },
            {
              "variant_key": "auto",
              "percentage": 10
            }
          ]
        }
      ],
      "default_rule": {
        "name": "2",
        "allocation": [
          { "variant_key": "light", "percentage": 100 }
        ]
      }
    },
    {
      "key": "max_upload_size",
      "enabled": true,
      "rollout_percentage": 80,
      "type": "double",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "small",
          "value": 5.0
        },
        {
          "key": "medium",
          "value": 10.5
        },
        {
          "key": "large",
          "value": 25.0
        }
      ],
      "rules": [
        {
          "name": "3",
          "constraints": [
            {
              "context_field": "subscription_tier",
              "operator": "eq",
              "value": "premium"
            },
            {
              "context_field": "storage_used_gb",
              "operator": "lt",
              "value": 50
            }
          ],
          "allocations": [
            {
              "variant_key": "large",
              "percentage": 100
            }
          ]
        }
      ],
      "default_rule": {
        "name": "4",
        "allocation": [
          { "variant_key": "medium", "percentage": 100 }
        ]
      }
    }
  ],
  "updated_at": 1759231299
}"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(comprehensiveJson)
            println("✅ Comprehensive feature flags parsing successful: ${result.features.size} flags")

            // Validate UI theme flag
            val uiTheme = result.features[0]
            assert(uiTheme.key == "ui_theme")
            assert(uiTheme.variants.size == 3)
            assert(uiTheme.rules[0].constraints.size == 2)
            assert(uiTheme.rules[0].allocations.size == 3)

            // Test different operators
            val constraints = uiTheme.rules[0].constraints
            assert(constraints[0].operator == Operator.Eq)
            assert(constraints[1].operator == Operator.Gte)

            // Validate upload size flag with double values
            val uploadSize = result.features[1]
            assert(uploadSize.key == "max_upload_size")
            val smallVariant = uploadSize.variants.find { it.key == "small" }
            assert(smallVariant != null)
            assert(smallVariant?.value is VariantValue.DoubleValue)
            assert((smallVariant?.value as VariantValue.DoubleValue).value == 5.0)
            assert(uploadSize.defaultRule?.allocation?.isNotEmpty() == true)

            println("  - All comprehensive validations passed")
        } catch (e: Exception) {
            println("❌ Comprehensive feature flags parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testEdgeCaseFeatureFlagsJsonParsing() {
        val edgeCaseJson = """{
  "features": [
    {
      "key": "minimal_flag",
      "enabled": false,
      "rollout_percentage": 0,
      "type": "string",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "default",
          "value": ""
        }
      ],
      "rules": [],
      "default_rule": {
        "name": "1",
        "allocation": [
          { "variant_key": "default", "percentage": 100 }
        ]
      }
    },
    {
      "key": "boundary_values",
      "enabled": true,
      "rollout_percentage": 100,
      "type": "double",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "max_double",
          "value": 999999999.999999
        },
        {
          "key": "min_double",
          "value": -999999999.999999
        },
        {
          "key": "zero",
          "value": 0.0
        }
      ],
      "rules": [
        {
          "name": "2",
          "constraints": [
            {
              "context_field": "score",
              "operator": "gt",
              "value": 0
            }
          ],
          "allocations": [
            {
              "variant_key": "max_double",
              "percentage": 100
            }
          ]
        }
      ],
      "default_rule": {
        "name": "3",
        "allocation": [
          { "variant_key": "zero", "percentage": 100 }
        ]
      }
    }
  ],
  "updated_at": 1759231299
}"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(edgeCaseJson)
            println("✅ Edge case feature flags parsing successful: ${result.features.size} flags")

            // Validate minimal flag
            val minimalFlag = result.features[0]
            assert(minimalFlag.key == "minimal_flag")
            assert(minimalFlag.enabled == false)
            assert(minimalFlag.rolloutPercentage == 0L)
            assert(minimalFlag.variants.size == 1)
            assert(minimalFlag.rules.isEmpty())
            assert(minimalFlag.defaultRule?.allocation?.size == 1)

            // Validate boundary values
            val boundaryFlag = result.features[1]
            val maxVariant = boundaryFlag.variants.find { it.key == "max_double" }
            assert(maxVariant?.value is VariantValue.DoubleValue)
            assert((maxVariant?.value as VariantValue.DoubleValue).value == 999999999.999999)

            val minVariant = boundaryFlag.variants.find { it.key == "min_double" }
            assert(minVariant?.value is VariantValue.DoubleValue)
            assert((minVariant?.value as VariantValue.DoubleValue).value == -999999999.999999)

            println("  - All edge case validations passed")
        } catch (e: Exception) {
            println("❌ Edge case feature flags parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testComplexObjectVariantParsing() {
        val complexObjectJson = """{
  "features": [
    {
      "key": "api_config",
      "enabled": true,
      "rollout_percentage": 100,
      "type": "object",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "production",
          "value": {
            "endpoint": "https://api.prod.example.com",
            "timeout": "5000",
            "retries": "3"
          }
        },
        {
          "key": "staging",
          "value": {
            "endpoint": "https://api.staging.example.com",
            "timeout": "10000",
            "retries": "5"
          }
        }
      ],
      "rules": [
        {
          "name": "1",
          "constraints": [
            {
              "context_field": "environment",
              "operator": "eq",
              "value": "staging"
            }
          ],
          "allocations": [
            {
              "variant_key": "staging",
              "percentage": 100
            }
          ]
        }
      ],
      "default_rule": {
        "name": "2",
        "allocation": [
          { "variant_key": "production", "percentage": 100 }
        ]
      }
    }
  ],
  "updated_at": 1759231299
}"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(complexObjectJson)
            println("✅ Complex object variant parsing successful: ${result.features.size} flags")

            val apiConfig = result.features[0]
            assert(apiConfig.key == "api_config")
            assert(apiConfig.defaultRule?.allocation?.isNotEmpty() == true)

            val prodVariant = apiConfig.variants.find { it.key == "production" }
            assert(prodVariant?.value is VariantValue.AnythingMapValue)
            val prodValue = (prodVariant?.value as VariantValue.AnythingMapValue).value
            assert(prodValue["endpoint"]?.jsonPrimitive?.content == "https://api.prod.example.com")
            assert(prodValue["timeout"]?.jsonPrimitive?.content == "5000")

            println("  - Complex object validations passed")
        } catch (e: Exception) {
            println("❌ Complex object variant parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testAllOperatorTypes() {
        val operatorsJson = """{
  "features": [
    {
      "key": "all_operators_test",
      "enabled": true,
      "rollout_percentage": 25,
      "type": "boolean",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "true_variant",
          "value": true
        },
        {
          "key": "false_variant",
          "value": false
        }
      ],
      "rules": [
        {
          "name": "1",
          "constraints": [
            {
              "context_field": "string_field",
              "operator": "eq",
              "value": "exact_match"
            }
          ],
          "allocations": [
            {
              "variant_key": "true_variant",
              "percentage": 100
            }
          ]
        },
        {
          "name": "2",
          "constraints": [
            {
              "context_field": "numeric_field",
              "operator": "gt",
              "value": 100
            }
          ],
          "allocations": [
            {
              "variant_key": "true_variant",
              "percentage": 80
            },
            {
              "variant_key": "false_variant",
              "percentage": 20
            }
          ]
        },
        {
          "name": "3",
          "constraints": [
            {
              "context_field": "age",
              "operator": "lt",
              "value": 18
            }
          ],
          "allocations": [
            {
              "variant_key": "false_variant",
              "percentage": 100
            }
          ]
        },
        {
          "name": "4",
          "constraints": [
            {
              "context_field": "tags",
              "operator": "in",
              "value": ["premium"]
            }
          ],
          "allocations": [
            {
              "variant_key": "true_variant",
              "percentage": 100
            }
          ]
        }
      ],
      "default_rule": {
        "name": "5",
        "allocation": [
          { "variant_key": "false_variant", "percentage": 100 }
        ]
      }
    }
  ],
  "updated_at": 1759231299
}"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(operatorsJson)
            println("✅ All operators parsing successful: ${result.features.size} flags")

            val flag = result.features[0]
            val rules = flag.rules
            assert(rules.size == 4)

            // Test all operators
            assert(rules[0].constraints[0].operator == Operator.Eq)
            assert(rules[1].constraints[0].operator == Operator.Gt)
            assert(rules[2].constraints[0].operator == Operator.Lt)
            assert(rules[3].constraints[0].operator == Operator.In)
            assert(result.features[0].defaultRule?.allocation?.isNotEmpty() == true)

            // Test different constraint value types
            assert(rules[0].constraints[0].value is ConstraintValue.StringValue)
            assert(rules[1].constraints[0].value is ConstraintValue.IntegerValue)
            assert(rules[2].constraints[0].value is ConstraintValue.IntegerValue)
            assert(rules[3].constraints[0].value is ConstraintValue.AnythingArrayValue)

            println("  - All operator validations passed")
        } catch (e: Exception) {
            println("❌ All operators parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testConstraintValueTypes() {
        val constraintTypesJson = """{
  "features": [
    {
      "key": "constraint_types_test",
      "enabled": true,
      "rollout_percentage": 50,
      "type": "string",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "default",
          "value": "test"
        }
      ],
      "rules": [
        {
          "name": "1",
          "constraints": [
            {
              "context_field": "string_field",
              "operator": "eq",
              "value": "string_value"
            },
            {
              "context_field": "bool_field",
              "operator": "eq",
              "value": true
            },
            {
              "context_field": "int_field",
              "operator": "gt",
              "value": 42
            },
            {
              "context_field": "double_field",
              "operator": "lt",
              "value": 3.14159
            },
            {
              "context_field": "array_field",
              "operator": "in",
              "value": ["item1", "item2", "item3"]
            }
          ],
          "allocations": [
            {
              "variant_key": "default",
              "percentage": 100
            }
          ]
        }
      ],
      "default_rule": {
        "name": "2",
        "allocation": [
          { "variant_key": "default", "percentage": 100 }
        ]
      }
    }
  ],
  "updated_at": 1759231299
}"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(constraintTypesJson)
            println("✅ Constraint value types parsing successful")

            val constraints = result.features[0].rules[0].constraints
            assert(constraints.size == 5)

            // Validate constraint value types
            assert(constraints[0].value is ConstraintValue.StringValue)
            assert((constraints[0].value as ConstraintValue.StringValue).value == "string_value")

            assert(constraints[1].value is ConstraintValue.BoolValue)
            assert((constraints[1].value as ConstraintValue.BoolValue).value)

            assert(constraints[2].value is ConstraintValue.IntegerValue)
            assert((constraints[2].value as ConstraintValue.IntegerValue).value == 42L)

            assert(constraints[3].value is ConstraintValue.DoubleValue)
            assert((constraints[3].value as ConstraintValue.DoubleValue).value == 3.14159)

            assert(constraints[4].value is ConstraintValue.AnythingArrayValue)
            val arrayValue = (constraints[4].value as ConstraintValue.AnythingArrayValue).value
            assert(arrayValue.size == 3)
//            assert(arrayValue.contains("item1"))

            println("  - All constraint value type validations passed")
        } catch (e: Exception) {
            println("❌ Constraint value types parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }

    @Test
    fun testVariantValueTypes() {
        val variantTypesJson = """{
  "features": [
    {
      "key": "variant_types_test",
      "enabled": true,
      "rollout_percentage": 100,
      "type": "string",
      "updated_at": 1759231203,
      "variants": [
        {
          "key": "string_variant",
          "value": "hello_world"
        },
        {
          "key": "bool_variant",
          "value": false
        },
        {
          "key": "double_variant",
          "value": 2.718281828
        },
        {
          "key": "object_variant",
          "value": {
            "name": "John Doe",
            "age": "30",
            "active": "true"
          }
        }
      ],
      "rules": [],
      "default_rule": {
        "name": "1",
        "allocation": [
          { "variant_key": "string_variant", "percentage": 100 }
        ]
      }
    }
  ],
  "updated_at": 1759231299
}"""

        try {
            val result = json.decodeFromString<FeatureFlagsSchema>(variantTypesJson)
            println("✅ Variant value types parsing successful")

            val variants = result.features[0].variants
            assert(variants.size == 4)
            assert(result.features[0].defaultRule?.allocation?.isNotEmpty() == true)

            // Validate variant value types
            val stringVariant = variants.find { it.key == "string_variant" }
            assert(stringVariant?.value is VariantValue.StringValue)
            assert((stringVariant?.value as VariantValue.StringValue).value == "hello_world")

            val boolVariant = variants.find { it.key == "bool_variant" }
            assert(boolVariant?.value is VariantValue.BoolValue)
            assert((boolVariant?.value as VariantValue.BoolValue).value == false)

            val doubleVariant = variants.find { it.key == "double_variant" }
            assert(doubleVariant?.value is VariantValue.DoubleValue)
            assert((doubleVariant?.value as VariantValue.DoubleValue).value == 2.718281828)

            val objectVariant = variants.find { it.key == "object_variant" }
            assert(objectVariant?.value is VariantValue.AnythingMapValue)
            val objectValue = (objectVariant?.value as VariantValue.AnythingMapValue).value
            assert(objectValue["name"]?.jsonPrimitive?.content == "John Doe")
            assert(objectValue["age"]?.jsonPrimitive?.content == "30")

            println("  - All variant value type validations passed")
        } catch (e: Exception) {
            println("❌ Variant value types parsing failed: ${e.message}")
            assert(false)
            e.printStackTrace()
        }
    }
}

//Adding Test commit to run 1
