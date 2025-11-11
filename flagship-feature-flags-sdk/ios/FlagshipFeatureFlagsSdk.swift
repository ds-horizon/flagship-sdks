import Foundation
import React

let queue = DispatchQueue(label: "FlagshipFeatureFlagsSdk", qos: .userInitiated)

@objc(FlagshipFeatureFlagsSdk)
class FlagshipFeatureFlagsSdk: NSObject {

  @objc
  func multiply(
    _ a: Double,
    withB b: Double,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    queue.async {
      resolve(a * b)
    }
  }

  @objc
  func getBooleanValue(
    _ key: String,
    defaultValue: Bool,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    FlagshipFeatureClientMethods.getBooleanValue(key, defaultValue: defaultValue, resolver: resolve, rejecter: reject)
  }

  @objc
  func getStringValue(
    _ key: String,
    defaultValue: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    FlagshipFeatureClientMethods.getStringValue(key, defaultValue: defaultValue, resolver: resolve, rejecter: reject)
  }

  @objc
  func getIntegerValue(
    _ key: String,
    defaultValue: NSNumber,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    FlagshipFeatureClientMethods.getIntegerValue(key, defaultValue: defaultValue, resolver: resolve, rejecter: reject)
  }

  @objc
  func getDoubleValue(
    _ key: String,
    defaultValue: NSNumber,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    FlagshipFeatureClientMethods.getDoubleValue(key, defaultValue: defaultValue, resolver: resolve, rejecter: reject)
  }

  @objc
  func getObjectValue(
    _ key: String,
    defaultValue: NSDictionary,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    FlagshipFeatureClientMethods.getObjectValue(key, defaultValue: defaultValue, resolver: resolve, rejecter: reject)
  }
  

  @objc
  static func requiresMainQueueSetup() -> Bool {
    return false
  }
}
