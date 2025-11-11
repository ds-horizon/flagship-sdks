//
//  FlagshipEvaluationPlugin.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation
import OpenFeature

public final class FlagshipEvaluationPlugin: EvaluationProtocol {
    
    private let allocationEvaluator: FeatureAllocationEvaluator
    
    public init() {
        self.allocationEvaluator = FeatureAllocationEvaluator()
    }
    
    public func evaluateFlag<T>(
        key: String,
        feature: [String: Any],
        userHash: Int,
        targetingKey: String?,
        context: [String: Value]?,
        contextFields: [String: [String: Any]],
        defaultValue: T
    ) -> T {
        let rolloutPercentage = feature["rollout_percentage"] as? Int ?? 100
        let isInRollout = userHash < rolloutPercentage
        
        let enabled = feature["enabled"] as? Bool ?? false
        
        if enabled && isInRollout {
            return allocationEvaluator.evaluateFeatureAllocation(
                key: key,
                feature: feature,
                userHash: userHash,
                targetingKey: targetingKey,
                context: context,
                contextFields: contextFields,
                defaultValue: defaultValue
            )
        } else {
            return defaultValue
        }
    }
}
