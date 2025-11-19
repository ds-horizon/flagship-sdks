//
//  FeatureAllocationEvaluator.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation
import OpenFeature

public final class FeatureAllocationEvaluator {
    
    public init() {}
    
    private func isContextFieldSemver(contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        guard let contextField = contextFields[contextFieldName],
              let contextFieldType = contextField["type"] as? String else {
            return false
        }
        return contextFieldType == ContextFieldTypes.semver
    }
    
    public func evaluateFeatureAllocation<T>(
        key: String,
        feature: [String: Any],
        userHash: Int,
        targetingKey: String?,
        context: [String: Value]?,
        contextFields: [String: [String: Any]],
        defaultValue: T
    ) -> T {
        if let rules = feature["rules"] as? [[String: Any]] {
            for rule in rules {
                if evaluateRuleConstraints(rule: rule, context: context, contextFields: contextFields) {
                    let ruleName = rule["name"] as? String ?? ""
                    let combinedString = "\(key):\(ruleName):\(targetingKey ?? "")"
                    let ruleHash = HashUtility.generateHashForFlag(combinedString: combinedString)
                    if let result = evaluateRuleAllocation(rule: rule, userHash: ruleHash, feature: feature, defaultValue: defaultValue) {
                        return result
                    } else {
                        return defaultValue
                    }
                }
            }
        }
        
        return evaluateDefaultRule(
            key: key,
            feature: feature,
            userHash: userHash,
            targetingKey: targetingKey,
            context: context,
            defaultValue: defaultValue
        )
    }
    
    private func evaluateRuleConstraints(rule: [String: Any], context: [String: Value]?, contextFields: [String: [String: Any]]) -> Bool {
        guard let constraints = rule["constraints"] as? [[String: Any]] else {
            return false
        }
        
        for constraint in constraints {
            if !evaluateConstraint(constraint: constraint, context: context, contextFields: contextFields) {
                return false
            }
        }
        
        return true
    }
    
    private func evaluateConstraint(constraint: [String: Any], context: [String: Value]?, contextFields: [String: [String: Any]]) -> Bool {
        guard let contextField = constraint["context_field"] as? String,
              let operatorValue = constraint["operator"] as? String,
              let expectedValue = constraint["value"] else {
            return false
        }
        
        guard let userValue = context?[contextField] else {
            return false
        }
        
        switch operatorValue {
        case "eq":
            return evaluateEquals(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        case "neq":
            return evaluateNotEquals(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        case "in":
            return evaluateIn(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        case "ct":
            return evaluateContains(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        case "gt":
            return evaluateGreaterThan(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        case "lt":
            return evaluateLessThan(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        case "gte":
            return evaluateGreaterThanOrEqual(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        case "lte":
            return evaluateLessThanOrEqual(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextField, contextFields: contextFields)
        default:
            return false
        }
    }
    
    private func evaluateEquals(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        if let expectedNumber = expectedValue as? NSNumber {
            if CFNumberIsFloatType(expectedNumber) {
                if let userDouble = userValue.asDouble() {
                    return userDouble == expectedNumber.doubleValue
                }
            } else {
                if let userInteger = userValue.asInteger() {
                    return userInteger == expectedNumber.int64Value
                }
            }
        }
        
        if let expectedString = expectedValue as? String,
           let userString = userValue.asString() {
            if isContextFieldSemver(contextFieldName: contextFieldName, contextFields: contextFields) {
                if let result = SemverUtility.compareSemverStrings(userString: userString, expectedString: expectedString, comparator: { $0 == 0 }) {
                    return result
                }
                return false
            } else {
                return userString == expectedString
            }
        }
        
        if let expectedBool = expectedValue as? Bool,
           let userBool = userValue.asBoolean() {
            return userBool == expectedBool
        }
        
        return false
    }
    
    private func evaluateNotEquals(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        return !evaluateEquals(userValue: userValue, expectedValue: expectedValue, contextFieldName: contextFieldName, contextFields: contextFields)
    }
    
    private func evaluateIn(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        if let expectedArray = expectedValue as? [String] {
            return evaluateInWithArray(userValue: userValue, expectedArray: expectedArray, contextFieldName: contextFieldName, contextFields: contextFields)
        }
        
        if let expectedIntArray = expectedValue as? [Int] {
            let expectedStringArray = expectedIntArray.map { String($0) }
            return evaluateInWithArray(userValue: userValue, expectedArray: expectedStringArray, contextFieldName: contextFieldName, contextFields: contextFields)
        }
        
        return false
    }
    
    private func evaluateInWithArray(userValue: Value, expectedArray: [String], contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        if let userInt = userValue.asInteger() {
            let intArray = expectedArray.compactMap { Int64($0) }
            if intArray.count == expectedArray.count {
                return intArray.contains(userInt)
            }
            return false
        }
        
        if let userString = userValue.asString() {
            if isContextFieldSemver(contextFieldName: contextFieldName, contextFields: contextFields) {
                let allSemver = expectedArray.allSatisfy { SemverUtility.isSemver($0) }
                if allSemver && SemverUtility.isSemver(userString) {
                    return expectedArray.contains { SemverUtility.compareSemver(userVersion: userString, expectedVersion: $0) == 0 }
                }
                return false
            } else {
                return expectedArray.contains(userString)
            }
        }
        
        return false
    }
    
    private func evaluateContains(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        guard let userArray = userValue.asList() else {
            return false
        }
        
        if let expectedNumber = expectedValue as? NSNumber, !CFNumberIsFloatType(expectedNumber) {
            let userIntArray = userArray.compactMap { $0.asInteger() }
            if userIntArray.count == userArray.count {
                return userIntArray.contains(expectedNumber.int64Value)
            }
            return false
        }
        
        if let expectedInt = expectedValue as? Int {
            let userIntArray = userArray.compactMap { $0.asInteger() }
            if userIntArray.count == userArray.count {
                return userIntArray.contains(Int64(expectedInt))
            }
            return false
        }
        
        if let expectedString = expectedValue as? String {
            let userStringArray = userArray.compactMap { $0.asString() }
            if userStringArray.count != userArray.count {
                return false
            }
            
            if isContextFieldSemver(contextFieldName: contextFieldName, contextFields: contextFields) {
                guard SemverUtility.isSemver(expectedString) else {
                    return false
                }
                let allSemver = userStringArray.allSatisfy { SemverUtility.isSemver($0) }
                guard allSemver else {
                    return false
                }
                return userStringArray.contains { SemverUtility.compareSemver(userVersion: $0, expectedVersion: expectedString) == 0 }
            } else {
                return userStringArray.contains(expectedString)
            }
        }
        
        return false
    }
    
    private func evaluateGreaterThan(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        if let expectedNumber = expectedValue as? NSNumber {
            if CFNumberIsFloatType(expectedNumber) {
                if let userDouble = userValue.asDouble() {
                    return userDouble > expectedNumber.doubleValue
                }
            } else {
                if let userInteger = userValue.asInteger() {
                    return userInteger > expectedNumber.int64Value
                }
            }
        }
        
        if let expectedString = expectedValue as? String,
           let userString = userValue.asString() {
            if isContextFieldSemver(contextFieldName: contextFieldName, contextFields: contextFields) {
                if let result = SemverUtility.compareSemverStrings(userString: userString, expectedString: expectedString, comparator: { $0 > 0 }) {
                    return result
                }
                return false
            }
        }
        
        return false
    }
    
    private func evaluateLessThan(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        if let expectedNumber = expectedValue as? NSNumber {
            if CFNumberIsFloatType(expectedNumber) {
                if let userDouble = userValue.asDouble() {
                    return userDouble < expectedNumber.doubleValue
                }
            } else {
                if let userInteger = userValue.asInteger() {
                    return userInteger < expectedNumber.int64Value
                }
            }
        }
        
        if let expectedString = expectedValue as? String,
           let userString = userValue.asString() {
            if isContextFieldSemver(contextFieldName: contextFieldName, contextFields: contextFields) {
                if let result = SemverUtility.compareSemverStrings(userString: userString, expectedString: expectedString, comparator: { $0 < 0 }) {
                    return result
                }
                return false
            }
        }
        
        return false
    }
    
    private func evaluateGreaterThanOrEqual(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        if let expectedNumber = expectedValue as? NSNumber {
            if CFNumberIsFloatType(expectedNumber) {
                if let userDouble = userValue.asDouble() {
                    return userDouble >= expectedNumber.doubleValue
                }
            } else {
                if let userInteger = userValue.asInteger() {
                    return userInteger >= expectedNumber.int64Value
                }
            }
        }
        
        if let expectedString = expectedValue as? String,
           let userString = userValue.asString() {
            if isContextFieldSemver(contextFieldName: contextFieldName, contextFields: contextFields) {
                if let result = SemverUtility.compareSemverStrings(userString: userString, expectedString: expectedString, comparator: { $0 >= 0 }) {
                    return result
                }
                return false
            }
        }
        
        return false
    }
    
    private func evaluateLessThanOrEqual(userValue: Value, expectedValue: Any, contextFieldName: String, contextFields: [String: [String: Any]]) -> Bool {
        if let expectedNumber = expectedValue as? NSNumber {
            if CFNumberIsFloatType(expectedNumber) {
                if let userDouble = userValue.asDouble() {
                    return userDouble <= expectedNumber.doubleValue
                }
            } else {
                if let userInteger = userValue.asInteger() {
                    return userInteger <= expectedNumber.int64Value
                }
            }
        }
        
        if let expectedString = expectedValue as? String,
           let userString = userValue.asString() {
            if isContextFieldSemver(contextFieldName: contextFieldName, contextFields: contextFields) {
                if let result = SemverUtility.compareSemverStrings(userString: userString, expectedString: expectedString, comparator: { $0 <= 0 }) {
                    return result
                }
                return false
            }
        }
        
        return false
    }
    
    private func evaluateRuleAllocation<T>(rule: [String: Any], userHash: Int, feature: [String: Any], defaultValue: T) -> T? {
        guard let allocations = rule["allocations"] as? [[String: Any]] else {
            return nil
        }
        
        var cumulativePercentage = 0
        for allocation in allocations {
            guard let percentage = allocation["percentage"] as? Int,
                  let variantKey = allocation["variant_key"] as? String else {
                continue
            }
            
            cumulativePercentage += percentage
            if userHash < cumulativePercentage {
                return getVariantValue(feature: feature, variantKey: variantKey, defaultValue: defaultValue)
            }
        }
        
        return nil
    }
    
    public func evaluateDefaultRule<T>(
        key: String,
        feature: [String: Any],
        userHash: Int,
        targetingKey: String?,
        context: [String: Value]?,
        defaultValue: T
    ) -> T {
        guard let defaultRule = feature["default_rule"] as? [String: Any],
              let allocations = defaultRule["allocation"] as? [[String: Any]] else {
            return defaultValue
        }
        
        let defaultRuleName = defaultRule["name"] as? String ?? ""
        let combinedString = "\(key):\(defaultRuleName):\(targetingKey ?? "")"
        let defaultRuleHash = HashUtility.generateHashForFlag(combinedString: combinedString)
        
        var cumulativePercentage = 0
        for allocation in allocations {
            guard let percentage = allocation["percentage"] as? Int,
                  let variantKey = allocation["variant_key"] as? String else {
                continue
            }
            
            cumulativePercentage += percentage
            if defaultRuleHash < cumulativePercentage {
                return getVariantValue(feature: feature, variantKey: variantKey, defaultValue: defaultValue)
            }
        }
        
        return defaultValue
    }
    
    private func getVariantValue<T>(feature: [String: Any], variantKey: String, defaultValue: T) -> T {
        guard let variants = feature["variants"] as? [[String: Any]] else {
            return defaultValue
        }
        
        for variant in variants {
            guard let key = variant["key"] as? String,
                  let value = variant["value"] else {
                continue
            }
            
            if key == variantKey {
                if let stringValue = value as? String, defaultValue is String {
                    return stringValue as! T
                } else if let boolValue = value as? Bool, defaultValue is Bool {
                    return boolValue as! T
                } else if let numberValue = value as? NSNumber, defaultValue is Int64 {
                    return numberValue.int64Value as! T
                } else if let numberValue = value as? NSNumber, defaultValue is Double {
                    return numberValue.doubleValue as! T
                } else if let objectValue = value as? [String: Any] {
                    if defaultValue is Value {
                        let convertedValue = convertToValue(from: objectValue)
                        return convertedValue as! T
                    } else if defaultValue is [String: Any] {
                        return objectValue as! T
                    }
                } else if let stringValue = value as? String {
                    if let data = stringValue.data(using: .utf8),
                       let objectValue = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                        if defaultValue is Value {
                            let convertedValue = convertToValue(from: objectValue)
                            return convertedValue as! T
                        } else if defaultValue is [String: Any] {
                            return objectValue as! T
                        }
                    }
                }
                
                return defaultValue
            }
        }
        
        return defaultValue
    }
    
    private func convertToValue(from dict: [String: Any]) -> Value {
        var convertedDict: [String: Value] = [:]
        
        for (key, value) in dict {
            if let stringValue = value as? String {
                convertedDict[key] = Value.string(stringValue)
            } else if let boolValue = value as? Bool {
                convertedDict[key] = Value.boolean(boolValue)
            } else if let numberValue = value as? NSNumber {
                if CFNumberIsFloatType(numberValue) {
                    convertedDict[key] = Value.double(numberValue.doubleValue)
                } else {
                    convertedDict[key] = Value.integer(numberValue.int64Value)
                }
            } else if let nestedDict = value as? [String: Any] {
                convertedDict[key] = convertToValue(from: nestedDict)
            } else if let arrayValue = value as? [Any] {
                let convertedArray = arrayValue.compactMap { item in
                    if let stringItem = item as? String {
                        return Value.string(stringItem)
                    } else if let boolItem = item as? Bool {
                        return Value.boolean(boolItem)
                    } else if let numberItem = item as? NSNumber {
                        if CFNumberIsFloatType(numberItem) {
                            return Value.double(numberItem.doubleValue)
                        } else {
                            return Value.integer(numberItem.int64Value)
                        }
                    } else if let dictItem = item as? [String: Any] {
                        return convertToValue(from: dictItem)
                    }
                    return nil
                }
                convertedDict[key] = Value.list(convertedArray)
            }
        }
        
        return Value.structure(convertedDict)
    }
    
}
