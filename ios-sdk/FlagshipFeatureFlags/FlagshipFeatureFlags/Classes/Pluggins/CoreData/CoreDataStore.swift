import Foundation
import CoreData

public class CoreDataStore: Store {
    
    private let coreDataUtility: CoreDataUtility
    
    public init() {
        self.coreDataUtility = CoreDataUtility.shared
    }
    
    public func getFeatureFlagRulesConfig(for key: String) -> [String: Any]? {
        guard !key.isEmpty else {
            print("CoreDataStore: Invalid key for fetch")
            return nil
        }
        
        guard let entity = coreDataUtility.fetchEntity(for: key),
              let jsonData = entity.jsonData else {
            print("CoreDataStore: No data found for key: \(key)")
            return nil
        }
        
        do {
            let jsonObject = try JSONSerialization.jsonObject(with: jsonData, options: [])
            guard let result = jsonObject as? [String: Any] else {
                print("CoreDataStore: Invalid JSON format for key: \(key)")
                return nil
            }
            return result
        } catch {
            print("CoreDataStore: Decode error for key '\(key)' - \(error.localizedDescription)")
            return nil
        }
    }
    
    public func setFeatureFlagRulesConfig(_ configuration: [String: Any]) {
        guard !configuration.isEmpty else {
            print("CoreDataStore: Empty configuration, skipping save")
            return
        }
        
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: configuration, options: [])
            
            guard !jsonData.isEmpty else {
                print("CoreDataStore: Empty JSON data, skipping save")
                return
            }
            
            if let entity = coreDataUtility.createOrUpdateEntity(for: "feature_flags", jsonData: jsonData) {
                coreDataUtility.saveContext()
            } else {
                print("CoreDataStore: Entity creation failed")
            }
            
        } catch {
            print("CoreDataStore: Encode error - \(error.localizedDescription)")
        }
    }
    
    public func clearFeatureFlagRulesConfig() {
        coreDataUtility.clearAllData()
    }
}
