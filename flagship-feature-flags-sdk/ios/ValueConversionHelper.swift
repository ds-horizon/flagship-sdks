import Foundation
import OpenFeature

class ValueConversionHelper {
  
  // Convert NSDictionary to OpenFeature Value
  static func convertNSDictionaryToValue(_ dict: NSDictionary) -> Value {
    var convertedDict: [String: Value] = [:]
    
    for (key, value) in dict {
      guard let stringKey = key as? String else { continue }
      convertedDict[stringKey] = convertAnyToValue(value)
    }
    
    return Value.structure(convertedDict)
  }
  
  // Convert any value to OpenFeature Value
  static func convertAnyToValue(_ value: Any) -> Value {
    if let stringValue = value as? String {
      return Value.string(stringValue)
    } else if let boolValue = value as? Bool {
      return Value.boolean(boolValue)
    } else if let numberValue = value as? NSNumber {
      return CFNumberIsFloatType(numberValue) ? Value.double(numberValue.doubleValue) : Value.integer(numberValue.int64Value)
    } else if let dictValue = value as? NSDictionary {
      return convertNSDictionaryToValue(dictValue)
    } else if let arrayValue = value as? [Any] {
      return Value.list(arrayValue.map { convertAnyToValue($0) })
    } else {
      return Value.string(String(describing: value))
    }
  }
  
  // Convert OpenFeature Value to NSDictionary
  static func convertValueToNSDictionary(_ value: Value) -> NSDictionary {
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
  
  // Convert Value to Any (recursive)
  static func convertValueToAny(_ value: Value) -> Any {
    switch value {
    case .string(let stringValue): return stringValue
    case .boolean(let boolValue): return boolValue
    case .integer(let intValue): return intValue
    case .double(let doubleValue): return doubleValue
    case .structure(let dict):
      var resultDict: [String: Any] = [:]
      for (key, value) in dict {
        resultDict[key] = convertValueToAny(value)
      }
      return resultDict
    case .list(let array): return array.map { convertValueToAny($0) }
    default: return String(describing: value)
    }
  }
}
