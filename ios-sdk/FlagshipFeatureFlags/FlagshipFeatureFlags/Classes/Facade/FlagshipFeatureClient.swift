//
//  FlagshipFeatureClient.swift
//  Pods
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation
import OpenFeature

public final class FlagshipFeatureClient {

    private let config: FlagshipFeatureConfig
    private let evaluationManager: FlagshipEvaluationManager

    public init(config: FlagshipFeatureConfig) {
        
        self.config = config

        let httpTransport = HttpTransport(
            baseURL: config.baseURL, refreshInterval: config.refreshInterval)
        let coreDataStore = CoreDataStore()
        let featureRepository = FeatureRepository()

        self.evaluationManager = FlagshipEvaluationManager(
            httpTransport: httpTransport,
            coreDataStore: coreDataStore,
            config: config,
            featureRepository: featureRepository
        )

        FlagshipFeatureConfigManager.shared.setConfig(config)
        
        evaluationManager.startPolling()
    }

    // MARK: - Flag Evaluation Methods

    public func getStringEvaluation(
        key: String,
        defaultValue: String,
        context: EvaluationContext?
    ) -> String {
        return evaluationManager.evaluateStringFlag(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
    }

    public func getBooleanEvaluation(
        key: String,
        defaultValue: Bool,
        context:EvaluationContext?
    ) -> Bool {
        return evaluationManager.evaluateBooleanFlag(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
    }

    public func getIntegerEvaluation(
        key: String,
        defaultValue: Int64,
        context: EvaluationContext?
    ) -> Int64 {
        return evaluationManager.evaluateIntegerFlag(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
    }

    public func getDoubleEvaluation(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?
    ) -> Double {
        return evaluationManager.evaluateDoubleFlag(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
    }

    public func getObjectEvaluation(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?
    ) -> Value {
        return evaluationManager.evaluateObjectFlag(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
    }
    
    public func clearFlagCache() {
        evaluationManager.clearFlagCache()
    }

}
