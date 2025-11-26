//
//  ConstraintValidationTests.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 19/11/25.
//  Copyright © 2025 CocoaPods. All rights reserved.
//

import Quick
import Nimble
import OpenFeature
@testable import FlagshipFeatureFlags

class ConstraintValidationTestsSpec: QuickSpec {
    override class func spec() {
        describe("Constraint Validation Tests") {
            
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
            
            // Helper function to setup feature with context fields
            func setupFeatureWithContextFields(_ feature: [String: Any], contextFields: [[String: Any]]) {
                let config: [String: Any] = [
                    "features": [feature],
                    "context_fields": contextFields
                ]
                featureRepository.updateFeaturesMap(from: config)
                featureRepository.updateContextFieldsMap(from: config)
            }
            
            context("Valid constraint combinations") {
                
                it("should match string eq with string value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
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
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match string neq with string value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "neq",
                                        "value": "CA"
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
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match string in with valid list") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "in",
                                        "value": ["US", "UK"]
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
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match integer eq with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "eq",
                                        "value": 10
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
                        attributes: ["age": Value.integer(10)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match integer neq with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "neq",
                                        "value": 5
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
                        attributes: ["age": Value.integer(10)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match integer gt with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "gt",
                                        "value": 10
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
                        attributes: ["age": Value.integer(15)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match integer gte with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "gte",
                                        "value": 10
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
                        attributes: ["age": Value.integer(10)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match integer lt with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "lt",
                                        "value": 10
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
                        attributes: ["age": Value.integer(5)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match integer lte with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "lte",
                                        "value": 10
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
                        attributes: ["age": Value.integer(10)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match integer in with valid list") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "in",
                                        "value": [100, 200]
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
                        attributes: ["age": Value.integer(100)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match boolean eq with boolean value false") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "isPremium",
                                        "operator": "eq",
                                        "value": false
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
                        attributes: ["isPremium": Value.boolean(false)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match boolean eq with boolean value true") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "isPremium",
                                        "operator": "eq",
                                        "value": true
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
                        attributes: ["isPremium": Value.boolean(true)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match boolean neq with boolean value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "isPremium",
                                        "operator": "neq",
                                        "value": false
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
                        attributes: ["isPremium": Value.boolean(true)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match semver eq with semver value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "version",
                                        "operator": "eq",
                                        "value": "1.2.3"
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
                    let contextFields: [[String: Any]] = [
                        ["key": "version", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["version": Value.string("1.2.3")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match semver neq with semver value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "version",
                                        "operator": "neq",
                                        "value": "1.0.0"
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
                    let contextFields: [[String: Any]] = [
                        ["key": "version", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["version": Value.string("1.2.3")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match semver gt with semver value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "version",
                                        "operator": "gt",
                                        "value": "1.2.3"
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
                    let contextFields: [[String: Any]] = [
                        ["key": "version", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["version": Value.string("1.3.0")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match semver gte with semver value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "version",
                                        "operator": "gte",
                                        "value": "1.2.3"
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
                    let contextFields: [[String: Any]] = [
                        ["key": "version", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["version": Value.string("1.2.3")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match semver lt with semver value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "version",
                                        "operator": "lt",
                                        "value": "1.2.3"
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
                    let contextFields: [[String: Any]] = [
                        ["key": "version", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["version": Value.string("1.2.2")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match semver lte with semver value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "version",
                                        "operator": "lte",
                                        "value": "1.2.3"
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
                    let contextFields: [[String: Any]] = [
                        ["key": "version", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["version": Value.string("1.2.3")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match semver in with valid list") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "version",
                                        "operator": "in",
                                        "value": ["1.2.3", "1.2.4"]
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
                    let contextFields: [[String: Any]] = [
                        ["key": "version", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["version": Value.string("1.2.3")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match double gt with double value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "score",
                                        "operator": "gt",
                                        "value": 10.23
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
                        attributes: ["score": Value.double(15.5)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match double gte with double value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "score",
                                        "operator": "gte",
                                        "value": 10.23
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
                        attributes: ["score": Value.double(10.23)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match double lt with double value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "score",
                                        "operator": "lt",
                                        "value": 10.23
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
                        attributes: ["score": Value.double(5.5)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match double lte with double value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "score",
                                        "operator": "lte",
                                        "value": 10.23
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
                        attributes: ["score": Value.double(10.23)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match list_string ct with valid string value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "roles",
                                        "operator": "ct",
                                        "value": "admin"
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
                        attributes: ["roles": Value.list([Value.string("admin"), Value.string("user")])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match list_integer ct with valid integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "ids",
                                        "operator": "ct",
                                        "value": 10
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
                        attributes: ["ids": Value.list([Value.integer(10), Value.integer(20)])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
                
                it("should match list_semver ct with valid semver value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "versions",
                                        "operator": "ct",
                                        "value": "1.2.3"
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
                    let contextFields: [[String: Any]] = [
                        ["key": "versions", "type": "semver"]
                    ]
                    setupFeatureWithContextFields(feature, contextFields: contextFields)
                    
                    let context = createContext(
                        targetingKey: "user123",
                        attributes: ["versions": Value.list([Value.string("1.2.3"), Value.string("2.0.0")])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    expect(result).to(equal("matched"))
                }
            }
            
            context("Invalid constraint combinations - Type errors") {
                
                it("should not match integer eq with string value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "eq",
                                        "value": "10" // String instead of integer
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
                        attributes: ["age": Value.integer(10)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match integer eq with boolean value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "eq",
                                        "value": true // Boolean instead of integer
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
                        attributes: ["age": Value.integer(10)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match double gt with string value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "score",
                                        "operator": "gt",
                                        "value": "10.23" // String instead of double
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
                        attributes: ["score": Value.double(15.5)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match boolean eq with string value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "isPremium",
                                        "operator": "eq",
                                        "value": "true" // String instead of boolean
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
                        attributes: ["isPremium": Value.boolean(true)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match string eq with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "eq",
                                        "value": 123 // Integer instead of string
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
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match string in with invalid list element types") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "in",
                                        "value": [12, 23] // Integers instead of strings
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
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match integer in with invalid list element types") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "age",
                                        "operator": "in",
                                        "value": ["abc", "def"] // Strings instead of integers
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
                        attributes: ["age": Value.integer(10)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match list_string ct with integer value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "roles",
                                        "operator": "ct",
                                        "value": 123 // Integer instead of string
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
                        attributes: ["roles": Value.list([Value.string("admin")])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match list_integer ct with string value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "ids",
                                        "operator": "ct",
                                        "value": "abc" // String instead of integer
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
                        attributes: ["ids": Value.list([Value.integer(10)])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Type mismatch, should use default rule
                    expect(result).to(equal("matched"))
                }
            }
            
            context("Invalid constraint combinations - Invalid operators") {
                
                it("should not match string gt operator") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "gt",
                                        "value": "abc" // gt not supported for string
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
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Invalid operator, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match boolean gt operator") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "isPremium",
                                        "operator": "gt",
                                        "value": true // gt not supported for boolean
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
                        attributes: ["isPremium": Value.boolean(true)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Invalid operator, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match boolean in operator") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "isPremium",
                                        "operator": "in",
                                        "value": [true, false] // in not supported for boolean
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
                        attributes: ["isPremium": Value.boolean(true)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Invalid operator, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match double eq operator") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "score",
                                        "operator": "eq",
                                        "value": 1.2 // eq not supported for double
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
                        attributes: ["score": Value.double(1.2)]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Invalid operator, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match string in with non-list value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "in",
                                        "value": "US" // Single value instead of list
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
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Invalid value type for in operator, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match string in with empty list") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "country",
                                        "operator": "in",
                                        "value": [] // Empty list
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
                        attributes: ["country": Value.string("US")]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Empty list, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match list_string ct with list value") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "roles",
                                        "operator": "ct",
                                        "value": ["a", "b"] // List instead of single value
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
                        attributes: ["roles": Value.list([Value.string("admin")])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Invalid value type, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match list_string ct with empty string") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "roles",
                                        "operator": "ct",
                                        "value": "" // Empty string
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
                        attributes: ["roles": Value.list([Value.string("admin")])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Empty string, should use default rule
                    expect(result).to(equal("matched"))
                }
                
                it("should not match list_string with eq operator") {
                    let feature: [String: Any] = [
                        "key": "test_flag",
                        "enabled": true,
                        "rollout_percentage": 100,
                        "variants": [
                            ["key": "variant1", "value": "matched"]
                        ],
                        "rules": [
                            [
                                "name": "rule1",
                                "constraints": [
                                    [
                                        "context_field": "roles",
                                        "operator": "eq",
                                        "value": "test" // eq not supported for list types
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
                        attributes: ["roles": Value.list([Value.string("admin")])]
                    )
                    let result = flagEvaluator.evaluateStringFlag(
                        key: "test_flag",
                        defaultValue: "default",
                        context: context
                    )
                    // Invalid operator for list type, should use default rule
                    expect(result).to(equal("matched"))
                }
            }
        }
    }
}

