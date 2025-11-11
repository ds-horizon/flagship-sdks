import Foundation
import React
import OpenFeature


@objc(FlagshipFeatureClientMethods)
class FlagshipFeatureClientMethods: NSObject {

  @objc
  static func getBooleanValue(
    _ key: String,
    defaultValue: Bool,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    queue.async {
      DispatchQueue.main.async {
        guard let client = FlagshipFeatureClientSetter.client else {
          resolve(defaultValue)
          return
        }
        
        let result = client.getBooleanValue(key: key, defaultValue: defaultValue)
        resolve(result)
      }
    }
  }

  @objc
  static func getStringValue(
    _ key: String,
    defaultValue: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    queue.async {
      DispatchQueue.main.async {
        guard let client = FlagshipFeatureClientSetter.client else {
          resolve(defaultValue)
          return
        }
        
        let result = client.getStringValue(key: key, defaultValue: defaultValue)
        resolve(result)
      }
    }
  }

  @objc
  static func getIntegerValue(
    _ key: String,
    defaultValue: NSNumber,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    queue.async {
      DispatchQueue.main.async {
        guard let client = FlagshipFeatureClientSetter.client else {
          resolve(defaultValue)
          return
        }
        
        let result = client.getIntegerValue(key: key, defaultValue: Int64(defaultValue.intValue))
        resolve(NSNumber(value: result))
      }
    }
  }

  @objc
  static func getDoubleValue(
    _ key: String,
    defaultValue: NSNumber,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    queue.async {
      DispatchQueue.main.async {
        guard let client = FlagshipFeatureClientSetter.client else {
          resolve(defaultValue)
          return
        }
        
        let result = client.getDoubleValue(key: key, defaultValue: defaultValue.doubleValue)
        resolve(NSNumber(value: result))
      }
    }
  }

  @objc
  static func getObjectValue(
    _ key: String,
    defaultValue: NSDictionary,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    queue.async {
      DispatchQueue.main.async {
        guard let client = FlagshipFeatureClientSetter.client else {
          resolve(defaultValue)
          return
        }
        
        let defaultObject = ValueConversionHelper.convertNSDictionaryToValue(defaultValue)
        let result = client.getObjectValue(key: key, defaultValue: defaultObject)
        
        let resultDict = ValueConversionHelper.convertValueToNSDictionary(result)
        resolve(resultDict)
      }
    }
  }
}
