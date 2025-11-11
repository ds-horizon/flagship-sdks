import Foundation
import React
import OpenFeature
import FlagshipFeatureFlags

@objc(FlagshipRnSdk)
class FlagshipRnSdk: NSObject {
  
  // MARK: - Synchronous Method
  
  @objc
  func multiply(
    _ a: Double,
    b: Double
  ) -> NSNumber {
    return NSNumber(value: a * b)
  }
  
  // MARK: - Initialize SDK
  
  @objc
  func initialize(
    _ config: NSDictionary,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async { [weak self] in
      guard self != nil else {
        reject("INIT_ERROR", "Module deallocated", nil)
        return
      }
      
      // Extract config parameters with validation
      guard let baseUrl = config["baseUrl"] as? String, !baseUrl.isEmpty else {
        reject("CONFIG_ERROR", "baseUrl is required and must not be empty", nil)
        return
      }
      
      guard let tenantId = config["tenantId"] as? String, !tenantId.isEmpty else {
        reject("CONFIG_ERROR", "tenantId is required and must not be empty", nil)
        return
      }
      
      let refreshInterval = config["refreshInterval"] as? TimeInterval ?? 10.0
      
      
      do {
        // Create FlagshipFeatureConfig
        let flagshipConfig = FlagshipFeatureConfig(
          baseURL: baseUrl,
          refreshInterval: refreshInterval,
          tenantId: tenantId
        )
        
        // Set up OpenFeature provider
        let provider = FlagshipOpenFeatureProvider(config: flagshipConfig)
        OpenFeatureAPI.shared.setProvider(provider: provider)
        
        resolve(true)
        
      } catch let error {
        reject("INIT_ERROR", "Initialization failed: \(error.localizedDescription)", error)
      }
    }
  }
  
  // MARK: - Set Context
  
  @objc
  func setContext(
    _ context: NSDictionary,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      do {
        // Extract targeting key
        guard let targetingKey = context["targetingKey"] as? String else {
          reject("CONTEXT_ERROR", "targetingKey is required", nil)
          return
        }
        
        // Convert NSDictionary to OpenFeature context attributes
        var attributes: [String: Value] = [:]
        
        for (key, value) in context {
          guard let stringKey = key as? String else { continue }
          if stringKey == "targetingKey" { continue }
          
          if let convertedValue = self.convertToValue(value) {
            attributes[stringKey] = convertedValue
          }
        }
        
        // Create and set evaluation context using MutableStructure
        let evaluationContext = MutableContext(
          targetingKey: targetingKey,
          structure: MutableStructure(attributes: attributes)
        )
        
        OpenFeatureAPI.shared.setEvaluationContext(evaluationContext: evaluationContext)
        
        resolve(true)
        
      } catch {
        reject("SET_CONTEXT_ERROR", "Failed to set context: \(error.localizedDescription)", error)
      }
    }
  }
  
  // MARK: - Get Boolean Value
  
  @objc
  func getBooleanValue(
    _ key: String,
    defaultValue: Bool,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      do {
        let client = OpenFeatureAPI.shared.getClient()
        let result = client.getBooleanValue(key: key, defaultValue: defaultValue)
        resolve(result)
      } catch {
        reject("GET_BOOLEAN_ERROR", "Failed to get boolean value: \(error.localizedDescription)", error)
      }
    }
  }
  
  // MARK: - Get String Value
  
  @objc
  func getStringValue(
    _ key: String,
    defaultValue: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      do {
        let client = OpenFeatureAPI.shared.getClient()
        let result = client.getStringValue(key: key, defaultValue: defaultValue)
        resolve(result)
      } catch {
        reject("GET_STRING_ERROR", "Failed to get string value: \(error.localizedDescription)", error)
      }
    }
  }
  
  // MARK: - Get Integer Value
  
  @objc
  func getIntegerValue(
    _ key: String,
    defaultValue: NSNumber,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      do {
        let client = OpenFeatureAPI.shared.getClient()
        let result = client.getIntegerValue(key: key, defaultValue: defaultValue.int64Value)
        resolve(NSNumber(value: result))
      } catch {
        reject("GET_INTEGER_ERROR", "Failed to get integer value: \(error.localizedDescription)", error)
      }
    }
  }
  
  // MARK: - Get Double Value
  
  @objc
  func getDoubleValue(
    _ key: String,
    defaultValue: NSNumber,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      do {
        let client = OpenFeatureAPI.shared.getClient()
        let result = client.getDoubleValue(key: key, defaultValue: defaultValue.doubleValue)
        resolve(NSNumber(value: result))
      } catch {
        reject("GET_DOUBLE_ERROR", "Failed to get double value: \(error.localizedDescription)", error)
      }
    }
  }
  
  // MARK: - Get Object Value
  
  @objc
  func getObjectValue(
    _ key: String,
    defaultValue: NSDictionary,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      do {
        let client = OpenFeatureAPI.shared.getClient()
        
        // Convert NSDictionary to OpenFeature Value
        let defaultObject = self.convertNSDictionaryToValue(defaultValue)
        
        // Get value from OpenFeature
        let result = client.getObjectValue(key: key, defaultValue: defaultObject)
        
        // Convert back to NSDictionary
        let resultDict = self.convertValueToNSDictionary(result)
        
        resolve(resultDict)
      } catch {
        reject("GET_OBJECT_ERROR", "Failed to get object value: \(error.localizedDescription)", error)
      }
    }
  }
  
  // MARK: - Helper Methods: Type Conversion
  
  private func convertToValue(_ value: Any) -> Value? {
    if let stringValue = value as? String {
      return Value.string(stringValue)
    } else if let boolValue = value as? Bool {
      return Value.boolean(boolValue)
    } else if let intValue = value as? Int {
      return Value.integer(Int64(intValue))
    } else if let doubleValue = value as? Double {
      return Value.double(doubleValue)
    } else if let dictValue = value as? NSDictionary {
      return convertNSDictionaryToValue(dictValue)
    } else if let arrayValue = value as? [Any] {
      return convertArrayToValue(arrayValue)
    }
    return nil
  }
  
  private func convertNSDictionaryToValue(_ dict: NSDictionary) -> Value {
    var convertedDict: [String: Value] = [:]
    
    for (key, value) in dict {
      guard let stringKey = key as? String else { continue }
      let convertedValue = convertAnyToValue(value)
      convertedDict[stringKey] = convertedValue
    }
    
    return Value.structure(convertedDict)
  }
  
  private func convertAnyToValue(_ value: Any) -> Value {
    if let stringValue = value as? String {
      return Value.string(stringValue)
    } else if let boolValue = value as? Bool {
      return Value.boolean(boolValue)
    } else if let numberValue = value as? NSNumber {
      return CFNumberIsFloatType(numberValue)
        ? Value.double(numberValue.doubleValue)
        : Value.integer(numberValue.int64Value)
    } else if let dictValue = value as? NSDictionary {
      return convertNSDictionaryToValue(dictValue)
    } else if let arrayValue = value as? [Any] {
      return Value.list(arrayValue.map { convertAnyToValue($0) })
    } else {
      return Value.string(String(describing: value))
    }
  }
  
  private func convertArrayToValue(_ array: [Any]) -> Value {
    let convertedArray = array.compactMap { convertToValue($0) }
    return Value.list(convertedArray)
  }
  
  private func convertValueToNSDictionary(_ value: Value) -> NSDictionary {
    switch value {
    case .structure(let dict):
      var resultDict: [String: Any] = [:]
      for (key, value) in dict {
        resultDict[key] = convertValueToAny(value)
      }
      return resultDict as NSDictionary
    default:
      return ["value": convertValueToAny(value)]
    }
  }
  
  private func convertValueToAny(_ value: Value) -> Any {
    switch value {
    case .string(let stringValue):
      return stringValue
    case .boolean(let boolValue):
      return boolValue
    case .integer(let intValue):
      return intValue
    case .double(let doubleValue):
      return doubleValue
    case .structure(let dict):
      var resultDict: [String: Any] = [:]
      for (key, value) in dict {
        resultDict[key] = convertValueToAny(value)
      }
      return resultDict
    case .list(let array):
      return array.map { convertValueToAny($0) }
    default:
      return String(describing: value)
    }
  }
  
  // MARK: - Module Setup
  
  @objc
  static func requiresMainQueueSetup() -> Bool {
    return false
  }
}

