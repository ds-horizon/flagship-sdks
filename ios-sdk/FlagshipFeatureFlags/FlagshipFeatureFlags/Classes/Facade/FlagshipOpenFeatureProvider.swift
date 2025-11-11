//
//  FlagshipOpenFeatureProvider.swift
//  Pods
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Combine
import Foundation
import OpenFeature

public struct FlagshipFeatureMetadata: ProviderMetadata {
    public var name: String? = "FlagshipFeature"
}

public final class FlagshipOpenFeatureProvider: FeatureProvider {
    
    let openFeatureClient: FlagshipFeatureClient

    private let subject = PassthroughSubject<ProviderEvent?, Never>()
    public func observe() -> AnyPublisher<ProviderEvent?, Never> {
        subject.eraseToAnyPublisher()
    }

    // MARK: FeatureProvider
    public var hooks: [any Hook] = []
    public var metadata: ProviderMetadata = FlagshipFeatureMetadata()

    public init(config: FlagshipFeatureConfig) {
        openFeatureClient = FlagshipFeatureClient(config: config)
    }
    


    // MARK: Required FeatureProvider Methods
    public func initialize(initialContext: EvaluationContext?) async throws {
        // Custom logic will be added later
    }

    public func onContextSet(
        oldContext: EvaluationContext?, newContext: EvaluationContext
    ) async throws {
        openFeatureClient.clearFlagCache()
    }

    public func getBooleanEvaluation(
        key: String,
        defaultValue: Bool,
        context: EvaluationContext?
    ) throws -> ProviderEvaluation<Bool> {
        let result = openFeatureClient.getBooleanEvaluation(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
        return ProviderEvaluation(
            value: result,
            flagMetadata: [:],
            variant: nil,
            reason: Reason.targetingMatch.rawValue
        )
    }

    public func getStringEvaluation(
        key: String,
        defaultValue: String,
        context: EvaluationContext?
    ) throws -> ProviderEvaluation<String> {
        let result = openFeatureClient.getStringEvaluation(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
        return ProviderEvaluation(
            value: result,
            flagMetadata: [:],
            variant: nil,
            reason: Reason.targetingMatch.rawValue
        )
    }

    public func getIntegerEvaluation(
        key: String,
        defaultValue: Int64,
        context: EvaluationContext?
    ) throws -> ProviderEvaluation<Int64> {
        let result = openFeatureClient.getIntegerEvaluation(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
        return ProviderEvaluation(
            value: result,
            flagMetadata: [:],
            variant: nil,
            reason: Reason.targetingMatch.rawValue
        )
    }

    public func getDoubleEvaluation(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?
    ) throws -> ProviderEvaluation<Double> {
        let result = openFeatureClient.getDoubleEvaluation(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
        return ProviderEvaluation(
            value: result,
            flagMetadata: [:],
            variant: nil,
            reason: Reason.targetingMatch.rawValue
        )
    }

    public func getObjectEvaluation(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?
    ) throws -> ProviderEvaluation<Value> {
        let result = openFeatureClient.getObjectEvaluation(
            key: key,
            defaultValue: defaultValue,
            context: context
        )
        return ProviderEvaluation(
            value: result,
            flagMetadata: [:],
            variant: nil,
            reason: Reason.targetingMatch.rawValue
        )
    }
}
