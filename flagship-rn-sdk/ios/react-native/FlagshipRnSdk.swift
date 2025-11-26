import Foundation
import React
import OpenFeature
import FlagshipFeatureFlags

@objc(FlagshipRnSdkImpl)
class FlagshipRnSdkImpl: NSObject {
  @objc static let shared = FlagshipRnSdkImpl()
  
  @objc
  func multiply(_ a: Double, b: Double) -> NSNumber {
    return NSNumber(value: a * b)
  }
  
  @objc(initialize:resolve:reject:)
  func initialize(
    _ config: NSDictionary,
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      if FlagshipState.shared.isInitialized {
        resolve(true)
        return
      }
      
      guard let baseUrl = config["baseUrl"] as? String, !baseUrl.isEmpty else {
        reject("CONFIG_ERROR", "baseUrl is required and must not be empty", nil)
        return
      }
      
      guard let flagshipApiKey = config["flagshipApiKey"] as? String, !flagshipApiKey.isEmpty else {
        reject("CONFIG_ERROR", "flagshipApiKey is required and must not be empty", nil)
        return
      }
      
      let refreshInterval = config["refreshInterval"] as? TimeInterval ?? 10.0
      
      do {
        let flagshipConfig = FlagshipFeatureConfig(
          baseURL: baseUrl,
          refreshInterval: refreshInterval,
          flagshipApiKey: flagshipApiKey
        )
        
        let provider = FlagshipOpenFeatureProvider(config: flagshipConfig)
        OpenFeatureAPI.shared.setProvider(provider: provider)
        
        FlagshipState.shared.markInitialized()
        resolve(true)
      } catch let error {
        reject("INIT_ERROR", "Initialization failed: \(error.localizedDescription)", error)
      }
    }
  }
  
  @objc
  func setContext(_ context: NSDictionary) -> NSNumber {
    guard let targetingKey = context["targetingKey"] as? String else {
      return NSNumber(value: false)
    }
    
    var attributes: [String: Value] = [:]
    
    for (key, value) in context {
      guard let stringKey = key as? String else { continue }
      if stringKey == "targetingKey" { continue }
      let convertedValue = convertAnyToValue(value)
      attributes[stringKey] = convertedValue
    }
    
    let evaluationContext = MutableContext(
      targetingKey: targetingKey,
      structure: MutableStructure(attributes: attributes)
    )
    
    OpenFeatureAPI.shared.setEvaluationContext(evaluationContext: evaluationContext)
    return NSNumber(value: true)
  }
  
  @objc
  func getBooleanValue(_ key: String, defaultValue: Bool) -> NSNumber {
    let client = OpenFeatureAPI.shared.getClient()
    let result = client.getBooleanValue(key: key, defaultValue: defaultValue)
    return NSNumber(value: result)
  }
  
  @objc
  func getStringValue(_ key: String, defaultValue: String) -> String {
    let client = OpenFeatureAPI.shared.getClient()
    return client.getStringValue(key: key, defaultValue: defaultValue)
  }
  
  @objc
  func getIntegerValue(_ key: String, defaultValue: Double) -> NSNumber {
    let client = OpenFeatureAPI.shared.getClient()
    let result = client.getIntegerValue(key: key, defaultValue: Int64(defaultValue))
    return NSNumber(value: result)
  }
  
  @objc
  func getDoubleValue(_ key: String, defaultValue: Double) -> NSNumber {
    let client = OpenFeatureAPI.shared.getClient()
    let result = client.getDoubleValue(key: key, defaultValue: defaultValue)
    return NSNumber(value: result)
  }
  
  @objc
  func getObjectValue(_ key: String, defaultValue: NSDictionary) -> NSDictionary {
    let client = OpenFeatureAPI.shared.getClient()
    let defaultObject = self.convertNSDictionaryToValue(defaultValue)
    let result = client.getObjectValue(key: key, defaultValue: defaultObject)
    let resultDict = self.convertValueToNSDictionary(result)
    return resultDict
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
      return .string(stringValue)
    } else if let boolValue = value as? Bool {
      return .boolean(boolValue)
    } else if let numberValue = value as? NSNumber {
      return CFNumberIsFloatType(numberValue) ? .double(numberValue.doubleValue) : .integer(numberValue.int64Value)
    } else if let dictValue = value as? NSDictionary {
      return convertNSDictionaryToValue(dictValue)
    } else if let arrayValue = value as? [Any] {
      return .list(arrayValue.map { convertAnyToValue($0) })
    } else {
      return .string(String(describing: value))
    }
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
    case .string(let v): return v
    case .boolean(let v): return v
    case .integer(let v): return v
    case .double(let v): return v
    case .structure(let dict):
      var result: [String: Any] = [:]
      for (k, v) in dict { result[k] = convertValueToAny(v) }
      return result
    case .list(let arr): return arr.map { convertValueToAny($0) }
    default: return String(describing: value)
    }
  }
}
