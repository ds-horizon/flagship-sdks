import Foundation

public final class FeatureRepository {
    
    private var featuresMap: [String: [String: Any]] = [:]
    private var contextFieldsMap: [String: [String: Any]] = [:]
    
    public init() {}
    
    func getFeature(key: String) -> [String: Any]? {
        return featuresMap[key]
    }
    
    func getContextFieldsMap() -> [String: [String: Any]] {
        return contextFieldsMap
    }
    
    func getContextFieldType(contextFieldName: String) -> String? {
        guard let contextField = contextFieldsMap[contextFieldName],
              let type = contextField["type"] as? String else {
            return nil
        }
        return type
    }
    
    func loadFromCoreData(coreDataStore: CoreDataStore) {
        if let configData = coreDataStore.getFeatureFlagRulesConfig(for: "feature_flags") {
            updateFeaturesMap(from: configData)
            updateContextFieldsMap(from: configData)
        }
    }
    
    func updateFeaturesMap(from response: [String: Any]) {
        guard let featuresArray = response["features"] as? [[String: Any]]
        else {
            return
        }
        
        featuresMap.removeAll()
        
        for feature in featuresArray {
            guard let key = feature["key"] as? String else {
                continue
            }
            
            featuresMap[key] = feature
        }
    }
    
    func updateContextFieldsMap(from response: [String: Any]) {
        guard let contextFieldsArray = response["context_fields"] as? [[String: Any]]
        else {
            return
        }
        
        contextFieldsMap.removeAll()
        
        for contextField in contextFieldsArray {
            guard let name = contextField["name"] as? String else {
                continue
            }
            
            contextFieldsMap[name] = contextField
        }
    }
}

