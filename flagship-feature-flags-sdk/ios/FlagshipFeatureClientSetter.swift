import Foundation
import OpenFeature

@objc public class FlagshipFeatureClientSetter: NSObject {

  // Store the client passed from app native side
  public static var client: Client?

  // Static method to be called by the app native side
  public static func setClient(_ client: Client) {
    print("🟡 [RN Native] setClient called from app native side")
    FlagshipFeatureClientSetter.client = client
    print("✅ [RN Native] Client stored successfully")
  }
}
