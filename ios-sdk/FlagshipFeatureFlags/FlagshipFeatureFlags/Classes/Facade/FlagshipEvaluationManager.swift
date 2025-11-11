import Foundation
import OpenFeature

public final class FlagshipEvaluationManager {

    private let config: FlagshipFeatureConfig
    private let featureRepository: FeatureRepository
    private let flagEvaluator: FlagEvaluator
    private let dataSyncManager: DataSyncManager
    private var pollingManager: PollingManager?
    
    public init(
        httpTransport: HttpTransport, 
        coreDataStore: CoreDataStore,
        config: FlagshipFeatureConfig,
        featureRepository: FeatureRepository
    ) {
        self.config = config
        self.featureRepository = featureRepository
        
        featureRepository.loadFromCoreData(coreDataStore: coreDataStore)
        
        let plugin = FlagshipEvaluationPlugin()
        
        let evaluator = FlagEvaluator(
            evaluationPlugin: plugin,
            featureRepository: featureRepository
        )
        
        let syncManager = DataSyncManager(
            httpTransport: httpTransport,
            coreDataStore: coreDataStore,
            featureRepository: featureRepository,
            flagEvaluator: evaluator
        )
        
        self.flagEvaluator = evaluator
        self.dataSyncManager = syncManager
    }

    public func evaluateStringFlag(
        key: String,
        defaultValue: String,
        context: EvaluationContext?
    ) -> String {
        return flagEvaluator.evaluateStringFlag(
            key: key, defaultValue: defaultValue, context: context)
    }

    public func evaluateBooleanFlag(
        key: String,
        defaultValue: Bool,
        context: EvaluationContext?
    ) -> Bool {
        return flagEvaluator.evaluateBooleanFlag(
            key: key, defaultValue: defaultValue, context: context)
    }

    public func evaluateIntegerFlag(
        key: String,
        defaultValue: Int64,
        context: EvaluationContext?
    ) -> Int64 {
        return flagEvaluator.evaluateIntegerFlag(
            key: key, defaultValue: defaultValue, context: context)
    }

    public func evaluateDoubleFlag(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?
    ) -> Double {
        return flagEvaluator.evaluateDoubleFlag(
            key: key, defaultValue: defaultValue, context: context)
    }

    public func evaluateObjectFlag(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?
    ) -> Value {
        return flagEvaluator.evaluateObjectFlag(
            key: key, defaultValue: defaultValue, context: context)
    }

    public func startPolling() {
        let pollingConfig = PollingConfig(interval: config.refreshInterval)

        let pollingBlock: () async -> Void = { [weak self] in
            await self?.dataSyncManager.syncFlags()
        }

        pollingManager = PollingManager(
            config: pollingConfig, pollingBlock: pollingBlock)
        pollingManager?.start()
    }

    public func stopPolling() {
        pollingManager?.stop()
        pollingManager = nil
    }
    
    public func clearFlagCache() {
        flagEvaluator.clearFlagCache()
    }
}
