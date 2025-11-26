import Foundation
import OpenFeature
import FlagshipFeatureFlags
import os.log

@objc public final class FlagshipSdk: NSObject {
    @objc public static let shared = FlagshipSdk()
    
    private let logger = Logger(subsystem: "com.flagshiprnsdk", category: "FlagshipSdk")
    
    private override init() {
        super.init()
    }
    
    @objc @discardableResult
    public func initialize(
        baseUrl: String,
        apiKey: String,
        refreshInterval: TimeInterval = 10.0
    ) -> Bool {
        if FlagshipState.shared.isInitialized {
            return true
        }
        
        do {
            let config = FlagshipFeatureConfig(
                baseURL: baseUrl,
                refreshInterval: refreshInterval,
                flagshipApiKey: apiKey
            )
            
            let provider = FlagshipOpenFeatureProvider(config: config)
            OpenFeatureAPI.shared.setProvider(provider: provider)
            
            FlagshipState.shared.markInitialized()
            logger.info("Initialized with baseUrl=\(baseUrl), refreshInterval=\(refreshInterval)s")
            return true
        } catch {
            logger.error("Initialization failed: \(error.localizedDescription)")
            return false
        }
    }
    
    @objc @discardableResult
    public func setContext(targetingKey: String, context: [String: Any] = [:]) -> Bool {
        do {
            var attributes: [String: Value] = [:]
            
            for (key, value) in context {
                attributes[key] = convertAnyToValue(value)
            }
            
            let evaluationContext = MutableContext(
                targetingKey: targetingKey,
                structure: MutableStructure(attributes: attributes)
            )
            
            OpenFeatureAPI.shared.setEvaluationContext(evaluationContext: evaluationContext)
            logger.debug("Context set for targetingKey=\(targetingKey)")
            return true
        } catch {
            logger.error("Failed to set context: \(error.localizedDescription)")
            return false
        }
    }
    
    @objc public func getBooleanValue(key: String, defaultValue: Bool) -> Bool {
        do {
            let client = OpenFeatureAPI.shared.getClient()
            return client.getBooleanValue(key: key, defaultValue: defaultValue)
        } catch {
            logger.error("Failed to get boolean value for key=\(key): \(error.localizedDescription)")
            return defaultValue
        }
    }
    
    @objc public func getStringValue(key: String, defaultValue: String) -> String {
        do {
            let client = OpenFeatureAPI.shared.getClient()
            return client.getStringValue(key: key, defaultValue: defaultValue)
        } catch {
            logger.error("Failed to get string value for key=\(key): \(error.localizedDescription)")
            return defaultValue
        }
    }
    
    @objc public func getIntegerValue(key: String, defaultValue: Int) -> Int {
        do {
            let client = OpenFeatureAPI.shared.getClient()
            return Int(client.getIntegerValue(key: key, defaultValue: Int64(defaultValue)))
        } catch {
            logger.error("Failed to get integer value for key=\(key): \(error.localizedDescription)")
            return defaultValue
        }
    }
    
    @objc public func getDoubleValue(key: String, defaultValue: Double) -> Double {
        do {
            let client = OpenFeatureAPI.shared.getClient()
            return client.getDoubleValue(key: key, defaultValue: defaultValue)
        } catch {
            logger.error("Failed to get double value for key=\(key): \(error.localizedDescription)")
            return defaultValue
        }
    }
    
    @objc public func getObjectValue(key: String, defaultValue: [String: Any] = [:]) -> [String: Any] {
        do {
            let client = OpenFeatureAPI.shared.getClient()
            let defaultOpenFeatureValue = convertDictionaryToValue(defaultValue)
            let result = client.getObjectValue(key: key, defaultValue: defaultOpenFeatureValue)
            return convertValueToDictionary(result)
        } catch {
            logger.error("Failed to get object value for key=\(key): \(error.localizedDescription)")
            return defaultValue
        }
    }
    
    @objc public func isInitialized() -> Bool {
        return FlagshipState.shared.isInitialized
    }
    
    private func convertAnyToValue(_ value: Any) -> Value {
        switch value {
        case let boolValue as Bool:
            return .boolean(boolValue)
        case let intValue as Int:
            return .integer(Int64(intValue))
        case let int64Value as Int64:
            return .integer(int64Value)
        case let doubleValue as Double:
            return .double(doubleValue)
        case let floatValue as Float:
            return .double(Double(floatValue))
        case let stringValue as String:
            return .string(stringValue)
        case let dictValue as [String: Any]:
            return convertDictionaryToValue(dictValue)
        case let arrayValue as [Any]:
            return .list(arrayValue.map { convertAnyToValue($0) })
        default:
            return .string(String(describing: value))
        }
    }
    
    private func convertDictionaryToValue(_ dict: [String: Any]) -> Value {
        var result: [String: Value] = [:]
        for (key, value) in dict {
            result[key] = convertAnyToValue(value)
        }
        return .structure(result)
    }
    
    private func convertValueToDictionary(_ value: Value) -> [String: Any] {
        switch value {
        case .structure(let dict):
            var result: [String: Any] = [:]
            for (key, v) in dict {
                result[key] = convertValueToAny(v)
            }
            return result
        default:
            return ["value": convertValueToAny(value)]
        }
    }
    
    private func convertValueToAny(_ value: Value) -> Any {
        switch value {
        case .boolean(let v): return v
        case .integer(let v): return v
        case .double(let v): return v
        case .string(let v): return v
        case .list(let arr): return arr.map { convertValueToAny($0) }
        case .structure(let dict):
            var result: [String: Any] = [:]
            for (k, v) in dict {
                result[k] = convertValueToAny(v)
            }
            return result
        default:
            return String(describing: value)
        }
    }
}

