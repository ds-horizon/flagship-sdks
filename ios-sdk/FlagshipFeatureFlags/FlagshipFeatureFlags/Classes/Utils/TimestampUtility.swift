import Foundation

public class TimestampUtility {
    
    private static let userDefaults = UserDefaults.standard
    private static let timestampKey = "flagship_config_timestamp"
    
    public static func getStoredTimestamp() -> Int64? {
        let timestamp = userDefaults.object(forKey: timestampKey) as? Int64
        return timestamp
    }
    
    public static func storeTimestamp(_ timestamp: Int64) {
        userDefaults.set(timestamp, forKey: timestampKey)
        userDefaults.synchronize()
    }
    
    public static func hasTimestampChanged(newTimestamp: Int64) -> Bool {
        guard let storedTimestamp = getStoredTimestamp() else {
            return true
        }
        return newTimestamp != storedTimestamp
    }
    
    public static func clearTimestamp() {
        userDefaults.removeObject(forKey: timestampKey)
        userDefaults.synchronize()
    }
}
