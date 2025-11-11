//
//  EvaluationProtocol.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation
import OpenFeature

// MARK: - Evaluation Protocol

public protocol EvaluationProtocol {
    func evaluateFlag<T>(
        key: String,
        feature: [String: Any],
        userHash: Int,
        targetingKey: String?,
        context: [String: Value]?,
        contextFields: [String: [String: Any]],
        defaultValue: T
    ) -> T
}

public struct FlagshipEvaluationContext {
    public let key: String
    public let feature: [String: Any]
    public let userHash: Int
    public let context: [String: Value]?
    public let defaultValue: Any
    
    public init(
        key: String,
        feature: [String: Any],
        userHash: Int,
        context: [String: Value]?,
        defaultValue: Any
    ) {
        self.key = key
        self.feature = feature
        self.userHash = userHash
        self.context = context
        self.defaultValue = defaultValue
    }
}
