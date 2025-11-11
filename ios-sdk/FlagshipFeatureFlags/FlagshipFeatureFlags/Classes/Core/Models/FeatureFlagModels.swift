// This file was generated from JSON Schema using quicktype, do not modify it directly.
// To parse the JSON, add this file to your project and do:
//
//   let featureFlagConfiguration = try FeatureFlagConfiguration(json)

import Foundation

// MARK: - FeatureFlagConfiguration
public struct FeatureFlagConfiguration: Codable {
    public let flags: [Flag]

    public init(flags: [Flag]) {
        self.flags = flags
    }
}

/// List of all feature flags
// MARK: - Flag
public struct Flag: Codable {
    public let enabled: Bool
    public let key: String
    public let rolloutPercentage: Int
    public let rules: [Rule]
    public let variants: [VariantElement]

    public init(enabled: Bool, key: String, rolloutPercentage: Int, rules: [Rule], variants: [VariantElement]) {
        self.enabled = enabled
        self.key = key
        self.rolloutPercentage = rolloutPercentage
        self.rules = rules
        self.variants = variants
    }
}

/// List of rules for targeting specific users
// MARK: - Rule
public struct Rule: Codable {
    public let allocations: [AllocationElement]
    public let constraints: [Constraint]

    public init(allocations: [AllocationElement], constraints: [Constraint]) {
        self.allocations = allocations
        self.constraints = constraints
    }
}

// MARK: - AllocationElement
public struct AllocationElement: Codable {
    public let percentage: Int
    public let variantKey: String

    public init(percentage: Int, variantKey: String) {
        self.percentage = percentage
        self.variantKey = variantKey
    }
}

// MARK: - Constraint
public struct Constraint: Codable {
    public let contextField: String
    public let constraintOperator: Operator
    public let value: ConstraintValue

    public init(contextField: String, constraintOperator: Operator, value: ConstraintValue) {
        self.contextField = contextField
        self.constraintOperator = constraintOperator
        self.value = value
    }
}

public enum Operator: String, Codable {
    case contains
    case equals
    case greaterThan
    case lessThan
}

public enum ConstraintValue: Codable {
    case anythingArray([String])
    case bool(Bool)
    case double(Double)
    case integer(Int)
    case string(String)
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        
        if let boolValue = try? container.decode(Bool.self) {
            self = .bool(boolValue)
        } else if let doubleValue = try? container.decode(Double.self) {
            self = .double(doubleValue)
        } else if let intValue = try? container.decode(Int.self) {
            self = .integer(intValue)
        } else if let stringValue = try? container.decode(String.self) {
            self = .string(stringValue)
        } else if let arrayValue = try? container.decode([String].self) {
            self = .anythingArray(arrayValue)
        } else {
            throw DecodingError.typeMismatch(ConstraintValue.self, DecodingError.Context(codingPath: decoder.codingPath, debugDescription: "Unable to decode ConstraintValue"))
        }
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        
        switch self {
        case .anythingArray(let value):
            try container.encode(value)
        case .bool(let value):
            try container.encode(value)
        case .double(let value):
            try container.encode(value)
        case .integer(let value):
            try container.encode(value)
        case .string(let value):
            try container.encode(value)
        }
    }
}

// MARK: - VariantElement
public struct VariantElement: Codable {
    public let key: String
    public let value: VariantValue

    public init(key: String, value: VariantValue) {
        self.key = key
        self.value = value
    }
}

public enum VariantValue: Codable {
    case anythingMap([String: String])
    case bool(Bool)
    case double(Double)
    case string(String)
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        
        if let boolValue = try? container.decode(Bool.self) {
            self = .bool(boolValue)
        } else if let doubleValue = try? container.decode(Double.self) {
            self = .double(doubleValue)
        } else if let stringValue = try? container.decode(String.self) {
            self = .string(stringValue)
        } else if let mapValue = try? container.decode([String: String].self) {
            self = .anythingMap(mapValue)
        } else {
            throw DecodingError.typeMismatch(VariantValue.self, DecodingError.Context(codingPath: decoder.codingPath, debugDescription: "Unable to decode VariantValue"))
        }
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        
        switch self {
        case .anythingMap(let value):
            try container.encode(value)
        case .bool(let value):
            try container.encode(value)
        case .double(let value):
            try container.encode(value)
        case .string(let value):
            try container.encode(value)
        }
    }
}
