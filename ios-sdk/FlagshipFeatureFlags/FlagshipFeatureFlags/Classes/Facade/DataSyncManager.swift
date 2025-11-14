import Foundation

final class DataSyncManager {
    
    private let httpTransport: HttpTransport
    private let coreDataStore: CoreDataStore
    private let featureRepository: FeatureRepository
    private let flagEvaluator: FlagEvaluator
    
    init(
        httpTransport: HttpTransport,
        coreDataStore: CoreDataStore,
        featureRepository: FeatureRepository,
        flagEvaluator: FlagEvaluator
    ) {
        self.httpTransport = httpTransport
        self.coreDataStore = coreDataStore
        self.featureRepository = featureRepository
        self.flagEvaluator = flagEvaluator
    }
    
    func syncFlags() async {
        do {
            let storedTimestamp = TimestampUtility.getStoredTimestamp()
            
            if storedTimestamp == nil {
                try await syncWithFullConfig()
            } else if let timestamp = storedTimestamp {
                try await syncWithTimeOnlyCheck()
            }
        } catch {
            print("Flagship: Sync error - \(error.localizedDescription)")
        }
    }
    
    private func syncWithFullConfig() async throws {
        let flagsData = try await httpTransport.fetchConfig(type: "full")
        guard var responseDict = flagsData as? [String: Any] else {
            print("Flagship: Invalid response format")
            return
        }
        
        guard let headers = responseDict["_headers"] as? [AnyHashable: Any] else {
            print("Flagship: No headers found in response")
            return
        }
        
        responseDict.removeValue(forKey: "_headers")
        
        guard !responseDict.isEmpty else {
            print("Flagship: Empty response data")
            return
        }
        
        guard let newTimestamp = extractUpdatedAtFromHeaders(headers) else {
            print("Flagship: No timestamp found in response headers")
            return
        }
        
        print("Flagship: 🔄 CONFIG CHANGED")
        featureRepository.updateFeaturesMap(from: responseDict)
        featureRepository.updateContextFieldsMap(from: responseDict)
        flagEvaluator.clearCacheOnFeatureUpdate()
        
        await MainActor.run {
            coreDataStore.setFeatureFlagRulesConfig(responseDict)
            TimestampUtility.storeTimestamp(newTimestamp)
        }
    }
    
    private func syncWithTimeOnlyCheck() async throws {
        let timeOnlyData = try await httpTransport.fetchConfig(type: "time-only")
        guard var timeOnlyDict = timeOnlyData as? [String: Any],
              let headers = timeOnlyDict["_headers"] as? [AnyHashable: Any] else {
            print("Flagship: Invalid time-only response format")
            return
        }
        
        timeOnlyDict.removeValue(forKey: "_headers")
        
        guard let newTimestamp = extractUpdatedAtFromHeaders(headers) else {
            print("Flagship: No timestamp found in response headers")
            return
        }
        
        if TimestampUtility.hasTimestampChanged(newTimestamp: newTimestamp) {
            try await syncWithFullConfig()
        } else {
            print("Flagship: Configuration unchanged, skipping sync")
        }
    }
    
    private func extractUpdatedAtFromHeaders(_ headers: [AnyHashable: Any]) -> Int64? {
        guard let value = headers["updated-at"] as? String else {
            return nil
        }
        
        return Int64(value)
    }
}

