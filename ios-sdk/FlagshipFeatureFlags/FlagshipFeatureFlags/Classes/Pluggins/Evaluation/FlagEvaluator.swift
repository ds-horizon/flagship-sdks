import Foundation
import OpenFeature

final class FlagEvaluator {
    
    private let evaluationPlugin: EvaluationProtocol
    private let featureRepository: FeatureRepository
    private var flagCache: [String: Any] = [:]
    
    init(evaluationPlugin: EvaluationProtocol, featureRepository: FeatureRepository) {
        self.evaluationPlugin = evaluationPlugin
        self.featureRepository = featureRepository
    }
    
    func evaluateStringFlag(
        key: String,
        defaultValue: String,
        context: EvaluationContext?
    ) -> String {
        return evaluateFlag(key: key, defaultValue: defaultValue, context: context)
    }
    
    func evaluateBooleanFlag(
        key: String,
        defaultValue: Bool,
        context: EvaluationContext?
    ) -> Bool {
        return evaluateFlag(key: key, defaultValue: defaultValue, context: context)
    }
    
    func evaluateIntegerFlag(
        key: String,
        defaultValue: Int64,
        context: EvaluationContext?
    ) -> Int64 {
        return evaluateFlag(key: key, defaultValue: defaultValue, context: context)
    }
    
    func evaluateDoubleFlag(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?
    ) -> Double {
        return evaluateFlag(key: key, defaultValue: defaultValue, context: context)
    }
    
    func evaluateObjectFlag(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?
    ) -> Value {
        return evaluateFlag(key: key, defaultValue: defaultValue, context: context)
    }
    
    func clearFlagCache() {
        flagCache.removeAll()
    }
    
    func clearCacheOnFeatureUpdate() {
        flagCache.removeAll()
    }
    
    private func evaluateFlag<T>(
        key: String,
        defaultValue: T,
        context: EvaluationContext?
    ) -> T {
        if let cachedValue = flagCache[key] as? T {
            return cachedValue
        }
        
        guard let featureDict = featureRepository.getFeature(key: key) else {
            return defaultValue
        }
        
        let targetingKey = context?.getTargetingKey()
        guard let targetingKey = targetingKey, !targetingKey.isEmpty else {
            return defaultValue
        }
        let combinedString = "\(key):\(targetingKey)"
        let userHashValue = HashUtility.generateHashForFlag(combinedString: combinedString)
        
        let contextFieldsMap = featureRepository.getContextFieldsMap()
        
        let evaluatedValue = evaluationPlugin.evaluateFlag(
            key: key,
            feature: featureDict,
            userHash: userHashValue,
            targetingKey: targetingKey,
            context: context?.asMap(),
            contextFields: contextFieldsMap,
            defaultValue: defaultValue
        )
        
        flagCache[key] = evaluatedValue
        return evaluatedValue
    }
}

