//
//  FlagEvaluationFunctionalTests.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 19/11/25.
//  Copyright © 2025 CocoaPods. All rights reserved.
//

import Quick
import Nimble
import OpenFeature
@testable import FlagshipFeatureFlags

class FlagEvaluationFunctionalTestsSpec: QuickSpec {
    override class func spec() {
        describe("Flag Evaluation Functional Tests") {
            
            var featureRepository: FeatureRepository!
            var flagEvaluator: FlagEvaluator!
            var evaluationPlugin: FlagshipEvaluationPlugin!
            
            beforeEach {
                featureRepository = FeatureRepository()
                evaluationPlugin = FlagshipEvaluationPlugin()
                flagEvaluator = FlagEvaluator(
                    evaluationPlugin: evaluationPlugin,
                    featureRepository: featureRepository
                )
            }
            
            afterEach {
                flagEvaluator.clearFlagCache()
            }
            
            // Helper function to create EvaluationContext
            func createContext(targetingKey: String, attributes: [String: Value] = [:]) -> EvaluationContext {
                return MutableContext(
                    targetingKey: targetingKey,
                    structure: MutableStructure(attributes: attributes)
                )
            }
            
            // Helper function to setup feature in repository
            func setupFeature(_ feature: [String: Any]) {
                let config: [String: Any] = [
                    "features": [feature],
                    "context_fields": []
                ]
                featureRepository.updateFeaturesMap(from: config)
            }
            
            // Helper function to setup context fields
            func setupContextFields(_ contextFields: [[String: Any]]) {
                let config: [String: Any] = [
                    "features": [],
                    "context_fields": contextFields
                ]
                featureRepository.updateContextFieldsMap(from: config)
            }
            
            context("FLAG_NOT_FOUND scenarios") {
                
                it("should return user default when flag key not found") {
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "non_existent_flag",
                        defaultValue: "default_value",
                        context: context
                    )
                    expect(result).to(equal("default_value"))
                }
            }
            
            context("TYPE_MISMATCH scenarios") {
                
                it("should return user default when flag key exists but flag type mismatches getter type - boolean getter on string flag") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "string_value"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "test_flag",
                        defaultValue: false,
                        context: context
                    )
                    expect(result).to(equal(false))
                }
                
                it("should return user default when flag key exists but flag type mismatches getter type - string getter on boolean flag") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": true]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
                
                it("should return user default when boolean getter on multivariate flag") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 50, "variant_key": "variant1"],
                                ["percentage": 50, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "test_flag",
                        defaultValue: false,
                        context: context
                    )
                    expect(result).to(equal(false))
                }
                
                it("should return user default when string/JSON getter on boolean flag") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": true]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
            }
            
            context("Variant array scenarios") {
                
                it("should return user default when variants array is missing") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
                
                it("should return user default when variants array is empty") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
                
                it("should return user default when flag's variant value is null or missing") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1"] // missing value
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
            }
            
            context("DISABLED scenarios") {
                
                it("should return user default when enabled = false") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": false,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
                
                it("should treat missing enabled as false (user default)") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
            }
            
            context("Rollout percentage scenarios") {
                
                it("should return user default when user hash ≥ rollout percentage (outside rollout)") {
                    // Create a feature with 0% rollout to ensure user is outside
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 0,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("default"))
                }
                
                it("should always return user default when 0% rollout") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 0,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // Test with multiple users
                    for i in 1...10 {
                        let context = createContext(targetingKey: "user\(i)")
                        let result = flagEvaluator.evaluateStringFlag(
                            key: "test_flag",
                            defaultValue: "default",
                            context: context
                        )
                        expect(result).to(equal("default"))
                    }
                }
                
                it("should assume rollout_percentage to be 100, if missing") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    // Should assume rollout_percentage is 100 and evaluate normally
                    expect(result).to(equal("value1"))
                }
                
                it("should return variant for users inside rollout and default for users outside when rollout percentage is between 0 and 100") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 50, // 50% rollout
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // Test with multiple users to verify partial rollout
                    var variantCount = 0
                    var defaultCount = 0
                    
                    for i in 1...100 {
                        let context = createContext(targetingKey: "user\(i)")
                        flagEvaluator.clearFlagCache() // Clear cache to get fresh evaluation
                        let result = flagEvaluator.evaluateStringFlag(
                            key: "test_flag",
                            defaultValue: "user_default",
                            context: context
                        )
                        
                        if result == "value1" {
                            variantCount += 1
                        } else if result == "user_default" {
                            defaultCount += 1
                        }
                    }
                    
                    // With 50% rollout, approximately half should get variant, half should get default
                    // Allow some variance due to hash distribution
                    expect(variantCount).to(beGreaterThan(30)) // At least 30% should get variant
                    expect(variantCount).to(beLessThan(70)) // At most 70% should get variant
                    expect(defaultCount).to(beGreaterThan(30)) // At least 30% should get default
                    expect(defaultCount).to(beLessThan(70)) // At most 70% should get default
                    expect(variantCount + defaultCount).to(equal(100)) // All users should get one or the other
                }
            }
            
            context("Rules and allocations scenarios") {
                
                it("should return config default when flag enabled but no rules/allocations") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    // Should return variant value, not user default
                    expect(result).to(equal("value1"))
                }
                
                it("should return correct variant when context matches first rule") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "priority": 1,
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ],
                            [
                                "name": "rule2",
                                "priority": 2,
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "CA"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant2"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("value1"))
                }
                
                it("should return correct variant when context skips first and matches second rule") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "priority": 1,
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ],
                            [
                                "name": "rule2",
                                "priority": 2,
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "CA"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant2"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("CA")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("value2"))
                }
                
                it("should return correct variant when context skips first two and matches third rule") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"],
                            ["key": "variant3", "value": "value3"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "priority": 1,
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ],
                            [
                                "name": "rule2",
                                "priority": 2,
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "CA"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant2"]
                                ]
                            ],
                            [
                                "name": "rule3",
                                "priority": 3,
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "UK"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant3"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("UK")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("value3"))
                }
            
                it("should succeed for non-default rule with missing constraints") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Rule without constraints should not match (constraints array missing means false)
                    // Should fall through to default rule
                    expect(result).to(equal("value1"))
                }
                
                it("should return config default for rule with missing allocations") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    // Rule matches but no allocations, should return user default
                    expect(result).to(equal("user_default"))
                }
                
                it("should treat default rule with empty constraints as catch-all") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // Context that doesn't match rule1 should use default rule
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("CA")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    expect(result).to(equal("value2"))
                }
            }
            
            context("Constraint evaluation scenarios") {
                
                it("should not match rule for type mismatch of context field") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "eq",
                                        "value": "25" // String, but context has number
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["age": Value.integer(25)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("value1"))
                }
                
                it("should not match rule for value mismatch of context field") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("CA")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Value mismatch, should use default rule
                    expect(result).to(equal("value2"))
                }
                
                it("should treat unknown context_field in constraint as violation and continue to next rule") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "unknown_field",
                                        "operator": "eq",
                                        "value": "value"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ],
                            [
                                "name": "rule2",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant2"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Unknown field should fail, continue to rule2 which matches
                    expect(result).to(equal("value2"))
                }
            }
            
            context("Allocation percentage scenarios") {
                
                it("should distribute variants correctly when allocation sums to 100") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"],
                            ["key": "variant3", "value": "value3"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 33, "variant_key": "variant1"],
                                ["percentage": 33, "variant_key": "variant2"],
                                ["percentage": 34, "variant_key": "variant3"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // Test multiple users to verify distribution
                    var variant1Count = 0
                    var variant2Count = 0
                    var variant3Count = 0
                    
                    for i in 1...100 {
                        let context = createContext(targetingKey: "user\(i)")
                        let result = flagEvaluator.evaluateStringFlag(
                            key: "test_flag",
                            defaultValue: "default",
                            context: context
                        )
                        flagEvaluator.clearFlagCache() // Clear cache to get fresh evaluation
                        
                        if result == "value1" { variant1Count += 1 }
                        else if result == "value2" { variant2Count += 1 }
                        else if result == "value3" { variant3Count += 1 }
                    }
                    
                    // All users should get a variant (sum = 100)
                    expect(variant1Count + variant2Count + variant3Count).to(equal(100))
                }
                
                it("should return config default when allocations are missing") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default"
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    expect(result).to(equal("user_default"))
                }
                
                it("should return same variant when multiple allocations all point to same variant and sum to 100") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 30, "variant_key": "variant1"],
                                ["percentage": 30, "variant_key": "variant1"],
                                ["percentage": 40, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // Test with multiple users - all should get the same variant
                    for i in 1...100 {
                        let context = createContext(targetingKey: "user\(i)")
                        flagEvaluator.clearFlagCache()
                        let result = flagEvaluator.evaluateStringFlag(
                            key: "test_flag",
                            defaultValue: "user_default",
                            context: context
                        )
                        expect(result).to(equal("value1"))
                    }
                }
                
                it("should return same variant when multiple allocations with different percentages all point to same variant") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 10, "variant_key": "variant1"],
                                ["percentage": 25, "variant_key": "variant1"],
                                ["percentage": 15, "variant_key": "variant1"],
                                ["percentage": 50, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // All allocations sum to 100% and point to same variant
                    // Test with multiple users - all should get the variant
                    for i in 1...100 {
                        let context = createContext(targetingKey: "user\(i)")
                        flagEvaluator.clearFlagCache()
                        let result = flagEvaluator.evaluateStringFlag(
                            key: "test_flag",
                            defaultValue: "user_default",
                            context: context
                        )
                        expect(result).to(equal("value1"))
                    }
                }
                
                it("should return same variant when rule has multiple allocations all pointing to same variant") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 40, "variant_key": "variant1"],
                                    ["percentage": 30, "variant_key": "variant1"],
                                    ["percentage": 30, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    
                    // Test with multiple users - all should get variant (rule matches, allocations sum to 100%)
                    for i in 1...50 {
                        let context = createContext(
                            targetingKey: "user\(i)",
                            attributes: ["country": Value.string("US")]
                        )
                        flagEvaluator.clearFlagCache()
                        let result = flagEvaluator.evaluateStringFlag(
                            key: "test_flag",
                            defaultValue: "user_default",
                            context: context
                        )
                        expect(result).to(equal("value1"))
                    }
                }
                
            }
            
            context("Default rule scenarios") {
                
                it("should return config default when no rule matches and user inside rollout") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // Context that doesn't match rule1
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("CA")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    // Should use default rule
                    expect(result).to(equal("value1"))
                }
                
                it("should return default rule allocation when no context available") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // No context attributes
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    // Should use default rule
                    expect(result).to(equal("value1"))
                }
                
                it("should return user default when default rule missing entirely") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": []
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    expect(result).to(equal("user_default"))
                }
            }
            
            context("Expiry scenarios") {

                it("should evaluate normally when flags not expired (expiry > now)") {
                    let futureTimestamp = Int64(Date().timeIntervalSince1970) + 86400 // 1 day in future
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "expiry": futureTimestamp,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    // Should evaluate normally
                    expect(result).to(equal("value1"))
                }
            }
            
            context("Missing fields scenarios") {
                
                it("should treat missing rules array as use config default") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "user_default",
                        context: context
                    )
                    // Should use default rule
                    expect(result).to(equal("value1"))
                }
            }
            
            context("Deterministic behavior scenarios") {
                
                it("should return same output for same input (flagKey + context) every call") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"],
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 50, "variant_key": "variant1"],
                                ["percentage": 50, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    
                    // Clear cache first
                    flagEvaluator.clearFlagCache()
                    let result1 = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    
                    // Clear cache and evaluate again
                    flagEvaluator.clearFlagCache()
                    let result2 = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    
                    // Should be deterministic
                    expect(result1).to(equal(result2))
                }
                
                it("should use updated data deterministically after config refresh") {
                    let feature1: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature1)
                    
                    let context = createContext(targetingKey: "user123")
                    flagEvaluator.clearFlagCache()
                    let result1 = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result1).to(equal("value1"))
                    
                    // Update feature
                    let feature2: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant2", "value": "value2"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature2)
                    
                    flagEvaluator.clearFlagCache()
                    let result2 = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result2).to(equal("value2"))
                }
            }
            
            context("Multivariate scenarios") {
                
                it("should always return that variant when multivariate has one variant") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "value1"]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    // Test with multiple users
                    for i in 1...10 {
                        let context = createContext(targetingKey: "user\(i)")
                        flagEvaluator.clearFlagCache()
                        let result = flagEvaluator.evaluateStringFlag(
                            key: "test_flag",
                            defaultValue: "default",
                            context: context
                        )
                        expect(result).to(equal("value1"))
                    }
                }
            }
            
            context("Boolean flag type - positive scenarios") {
                
                it("should return true when boolean flag variant is true") {
                    let feature: [String: Any] = [
                        "key": "boolean_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": true]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "boolean_flag",
                        defaultValue: false,
                        context: context
                    )
                    expect(result).to(equal(true))
                }
                
                it("should return false when boolean flag variant is false") {
                    let feature: [String: Any] = [
                        "key": "boolean_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": false]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "boolean_flag",
                        defaultValue: true,
                        context: context
                    )
                    expect(result).to(equal(false))
                }
                
                it("should return boolean value when rule matches") {
                    let feature: [String: Any] = [
                        "key": "boolean_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": true],
                            ["key": "variant2", "value": false]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "boolean_flag",
                        defaultValue: false,
                        context: context
                    )
                    expect(result).to(equal(true))
                }
            }
            
            context("Boolean flag type - negative scenarios") {
                
                it("should return user default when boolean flag not found") {
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "non_existent_boolean",
                        defaultValue: false,
                        context: context
                    )
                    expect(result).to(equal(false))
                }
                
                it("should return user default when boolean flag is disabled") {
                    let feature: [String: Any] = [
                        "key": "boolean_flag",
                        "enabled": false,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": true]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "boolean_flag",
                        defaultValue: false,
                        context: context
                    )
                    expect(result).to(equal(false))
                }
                
                it("should return user default when boolean flag variant value is missing") {
                    let feature: [String: Any] = [
                        "key": "boolean_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1"] // missing value
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateBooleanFlag(
                        key: "boolean_flag",
                        defaultValue: false,
                        context: context
                    )
                    expect(result).to(equal(false))
                }
            }
            
            context("Integer flag type - positive scenarios") {
                
                it("should return integer value when integer flag is enabled") {
                    let feature: [String: Any] = [
                        "key": "integer_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 42]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "integer_flag",
                        defaultValue: 0,
                        context: context
                    )
                    expect(result).to(equal(42))
                }
                
                it("should return zero when integer flag variant value is zero") {
                    let feature: [String: Any] = [
                        "key": "integer_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 0]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "integer_flag",
                        defaultValue: 100,
                        context: context
                    )
                    expect(result).to(equal(0))
                }
                
                it("should return negative integer value when integer flag variant is negative") {
                    let feature: [String: Any] = [
                        "key": "integer_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": -100]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "integer_flag",
                        defaultValue: 0,
                        context: context
                    )
                    expect(result).to(equal(-100))
                }
                
                it("should return large integer value when integer flag variant is large") {
                    let feature: [String: Any] = [
                        "key": "integer_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 999999999]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "integer_flag",
                        defaultValue: 0,
                        context: context
                    )
                    expect(result).to(equal(999999999))
                }
                
                it("should return integer value when rule matches") {
                    let feature: [String: Any] = [
                        "key": "integer_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 100],
                            ["key": "variant2", "value": 200]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "integer_flag",
                        defaultValue: 0,
                        context: context
                    )
                    expect(result).to(equal(100))
                }
            }
            
            context("Integer flag type - negative scenarios") {
                
                it("should return user default when integer flag not found") {
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "non_existent_integer",
                        defaultValue: 0,
                        context: context
                    )
                    expect(result).to(equal(0))
                }
                
                it("should return user default when integer flag is disabled") {
                    let feature: [String: Any] = [
                        "key": "integer_flag",
                        "enabled": false,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 42]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "integer_flag",
                        defaultValue: 0,
                        context: context
                    )
                    expect(result).to(equal(0))
                }
                
                it("should return user default when integer flag variant value is missing") {
                    let feature: [String: Any] = [
                        "key": "integer_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1"] // missing value
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateIntegerFlag(
                        key: "integer_flag",
                        defaultValue: 0,
                        context: context
                    )
                    expect(result).to(equal(0))
                }
            }
            
            context("Double flag type - positive scenarios") {
                
                it("should return double value when double flag is enabled") {
                    let feature: [String: Any] = [
                        "key": "double_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 3.14159]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "double_flag",
                        defaultValue: 0.0,
                        context: context
                    )
                    expect(result).to(equal(3.14159))
                }
                
                it("should return zero when double flag variant value is zero") {
                    let feature: [String: Any] = [
                        "key": "double_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 0.0]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "double_flag",
                        defaultValue: 1.0,
                        context: context
                    )
                    expect(result).to(equal(0.0))
                }
                
                it("should return negative double value when double flag variant is negative") {
                    let feature: [String: Any] = [
                        "key": "double_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": -123.456]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "double_flag",
                        defaultValue: 0.0,
                        context: context
                    )
                    expect(result).to(equal(-123.456))
                }
                
                it("should return large double value when double flag variant is large") {
                    let feature: [String: Any] = [
                        "key": "double_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 999999.999]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "double_flag",
                        defaultValue: 0.0,
                        context: context
                    )
                    expect(result).to(equal(999999.999))
                }
                
                it("should return double value when rule matches") {
                    let feature: [String: Any] = [
                        "key": "double_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 10.5],
                            ["key": "variant2", "value": 20.5]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "double_flag",
                        defaultValue: 0.0,
                        context: context
                    )
                    expect(result).to(equal(10.5))
                }
            }
            
            context("Double flag type - negative scenarios") {
                
                it("should return user default when double flag not found") {
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "non_existent_double",
                        defaultValue: 0.0,
                        context: context
                    )
                    expect(result).to(equal(0.0))
                }
                
                it("should return user default when double flag is disabled") {
                    let feature: [String: Any] = [
                        "key": "double_flag",
                        "enabled": false,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": 3.14]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "double_flag",
                        defaultValue: 0.0,
                        context: context
                    )
                    expect(result).to(equal(0.0))
                }
                
                it("should return user default when double flag variant value is missing") {
                    let feature: [String: Any] = [
                        "key": "double_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1"] // missing value
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let result = flagEvaluator.evaluateDoubleFlag(
                        key: "double_flag",
                        defaultValue: 0.0,
                        context: context
                    )
                    expect(result).to(equal(0.0))
                }
            }
            
            context("Object flag type - positive scenarios") {
                
                it("should return object value when object flag is enabled with dictionary variant") {
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": [
                                "name": "test",
                                "value": 123,
                                "enabled": true
                            ]]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([
                        "name": Value.string("default"),
                        "value": Value.integer(0)
                    ])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    
                    expect(result).toNot(beNil())
                    if case .structure(let dict) = result {
                        expect(dict["name"]).toNot(beNil())
                        expect(dict["value"]).toNot(beNil())
                        expect(dict["enabled"]).toNot(beNil())
                    } else {
                        fail("Expected structure value")
                    }
                }
                
                it("should return object value when object flag has nested structure") {
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": [
                                "user": [
                                    "id": 123,
                                    "name": "John",
                                    "settings": [
                                        "theme": "dark",
                                        "notifications": true
                                    ]
                                ],
                                "metadata": [
                                    "version": "1.0"
                                ]
                            ]]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([:])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    
                    expect(result).toNot(beNil())
                    if case .structure(let dict) = result {
                        expect(dict["user"]).toNot(beNil())
                        expect(dict["metadata"]).toNot(beNil())
                    } else {
                        fail("Expected structure value")
                    }
                }
                
                it("should return object value when object flag has array in structure") {
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": [
                                "items": [1, 2, 3, 4, 5],
                                "tags": ["tag1", "tag2", "tag3"]
                            ]]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([:])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    
                    expect(result).toNot(beNil())
                    if case .structure(let dict) = result {
                        expect(dict["items"]).toNot(beNil())
                        expect(dict["tags"]).toNot(beNil())
                    } else {
                        fail("Expected structure value")
                    }
                }
                
                it("should return object value when object flag variant is JSON string") {
                    let jsonString = """
                    {
                        "name": "test",
                        "value": 123,
                        "enabled": true
                    }
                    """
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": jsonString]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([:])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    
                    expect(result).toNot(beNil())
                    if case .structure(let dict) = result {
                        expect(dict["name"]).toNot(beNil())
                        expect(dict["value"]).toNot(beNil())
                    } else {
                        fail("Expected structure value")
                    }
                }
                
                it("should return object value when object flag has empty structure") {
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": [:]]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([
                        "default": Value.string("value")
                    ])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    
                    expect(result).toNot(beNil())
                    if case .structure(let dict) = result {
                        expect(dict.count).to(equal(0))
                    } else {
                        fail("Expected structure value")
                    }
                }
                
                it("should return object value when rule matches") {
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": [
                                "config": "variant1_config"
                            ]],
                            ["key": "variant2", "value": [
                                "config": "variant2_config"
                            ]]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": "US"
                                    ]
                                ],
                                "allocations": [
                                    ["percentage": 100, "variant_key": "variant1"]
                                ]
                            ]
                        ],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant2"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let defaultObject = Value.structure([:])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    
                    expect(result).toNot(beNil())
                    if case .structure(let dict) = result {
                        expect(dict["config"]).toNot(beNil())
                    } else {
                        fail("Expected structure value")
                    }
                }
            }
            
            context("Object flag type - negative scenarios") {
                
                it("should return user default when object flag not found") {
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([
                        "default": Value.string("value")
                    ])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "non_existent_object",
                        defaultValue: defaultObject,
                        context: context
                    )
                    expect(result).to(equal(defaultObject))
                }
                
                it("should return user default when object flag is disabled") {
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": false,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": [
                                "name": "test"
                            ]]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([
                        "default": Value.string("value")
                    ])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    expect(result).to(equal(defaultObject))
                }
                
                it("should return user default when object flag variant value is missing") {
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1"] // missing value
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([
                        "default": Value.string("value")
                    ])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    expect(result).to(equal(defaultObject))
                }
                
                it("should return user default when object flag variant has invalid JSON string") {
                    let invalidJson = "not valid json {"
                    let feature: [String: Any] = [
                        "key": "object_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": invalidJson]
                        ],
                        "rules": [],
                        "default_rule": [
                            "name": "default",
                            "allocation": [
                                ["percentage": 100, "variant_key": "variant1"]
                            ]
                        ]
                    ]
                    setupFeature(feature)
                    
                    let context = createContext(targetingKey: "user123")
                    let defaultObject = Value.structure([
                        "default": Value.string("value")
                    ])
                    let result = flagEvaluator.evaluateObjectFlag(
                        key: "object_flag",
                        defaultValue: defaultObject,
                        context: context
                    )
                    expect(result).to(equal(defaultObject))
                }
            }
        }
    }
}

